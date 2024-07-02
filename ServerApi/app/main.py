import logging
from functools import lru_cache
from wsgiref.validate import validator
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from fastapi import FastAPI, Depends, HTTPException, Query, Request, Header
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from sqlalchemy import create_engine, Column, Integer, String, DateTime, func
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from pydantic import BaseModel, Field
from datetime import datetime, date
from typing import List, Optional
import os
from starlette.middleware.cors import CORSMiddleware

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

DATABASE_URL = os.getenv("DATABASE_URL")
API_KEY = os.getenv("API_KEY")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


class GameResultCreate(BaseModel):
    player_name: str = Field(..., min_length=1, max_length=50)
    play_date: str
    play_time: str
    duration: int = Field(..., gt=0)
    correct_count: int = Field(..., ge=0, le=10)

    @validator('play_date')
    def validate_play_date(cls, v):
        try:
            datetime.strptime(v, '%Y-%m-%d')
        except ValueError:
            raise ValueError('Invalid date format, should be YYYY-MM-DD')
        return v

    @validator('play_time')
    def validate_play_time(cls, v):
        try:
            datetime.strptime(v, '%H:%M:%S')
        except ValueError:
            raise ValueError('Invalid time format, should be HH:MM:SS')
        return v


Base.metadata.create_all(bind=engine)


class GameResult(BaseModel):
    player_name: str = Field(..., min_length=1, max_length=50)
    play_date: str
    play_time: str
    duration: int = Field(..., gt=0)
    correct_count: int = Field(..., ge=0, le=10)


class GameResultResponse(GameResultCreate):
    id: int
    created_at: datetime

    class Config:
        orm_mode = True


app = FastAPI(title="Math Game API", version="1.0.0")

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)
limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Mount static files
app.mount("/static", StaticFiles(directory="static"), name="static")

# Set up Jinja2 templates
templates = Jinja2Templates(directory="templates")


def get_db():
    db = SessionLocal()
    try:
        yield db
    except Exception as e:
        logger.error(f"Database error: {str(e)}")
        raise HTTPException(status_code=500, detail="Database error")
    finally:
        db.close()


def api_key_auth(api_key: str = Header(...)):
    if api_key != API_KEY:
        raise HTTPException(status_code=403, detail="Invalid API Key")


@lru_cache(maxsize=100)
def get_cached_top_scores(db: Session):
    return db.query(GameResult).order_by(GameResult.correct_count.desc(), GameResult.duration).limit(10).all()


@app.post("/sync_game_results", response_model=List[GameResultResponse], dependencies=[Depends(api_key_auth)])
def sync_game_results(game_results: List[GameResultCreate], db: Session = Depends(get_db)):
    db_results = []
    for result in game_results:
        db_result = GameResult(**result.dict())
        db.add(db_result)
        db_results.append(db_result)
    db.commit()
    for result in db_results:
        db.refresh(result)
    logger.info(f"Synced {len(db_results)} game results")
    return db_results


@app.get("/game_results", response_model=List[GameResultResponse])
def get_game_results(
        skip: int = 0,
        limit: int = Query(default=100, le=100),
        db: Session = Depends(get_db)
):
    results = db.query(GameResult).order_by(GameResult.created_at.desc()).offset(skip).limit(limit).all()
    return results


@app.get("/top_scores", response_model=List[GameResultResponse])
@limiter.limit("10/minute")
def get_top_scores(request: Request, db: Session = Depends(get_db)):
    return get_cached_top_scores(db)


@app.get("/player_stats/{player_name}", response_model=dict)
def get_player_stats(player_name: str, db: Session = Depends(get_db)):
    stats = db.query(
        func.count(GameResult.id).label("total_games"),
        func.avg(GameResult.correct_count).label("avg_score"),
        func.avg(GameResult.duration).label("avg_duration"),
        func.max(GameResult.correct_count).label("best_score"),
        func.min(GameResult.duration).label("best_time")
    ).filter(GameResult.player_name == player_name).first()

    if not stats or stats.total_games == 0:
        raise HTTPException(status_code=404, detail="Player not found or has no games")

    return {
        "player_name": player_name,
        "total_games": stats.total_games,
        "avg_score": round(stats.avg_score, 2),
        "avg_duration": round(stats.avg_duration, 2),
        "best_score": stats.best_score,
        "best_time": stats.best_time
    }


@app.get("/daily_leaderboard/{date}", response_model=List[GameResultResponse])
def get_daily_leaderboard(date: date, db: Session = Depends(get_db)):
    leaderboard = db.query(GameResult).filter(GameResult.play_date == str(date)).order_by(
        GameResult.correct_count.desc(),
        GameResult.duration
    ).limit(10).all()
    return leaderboard


@app.delete("/delete_game_result/{result_id}", dependencies=[Depends(api_key_auth)])
def delete_game_result(result_id: int, db: Session = Depends(get_db)):
    result = db.query(GameResult).filter(GameResult.id == result_id).first()
    if not result:
        raise HTTPException(status_code=404, detail="Game result not found")
    db.delete(result)
    db.commit()
    logger.info(f"Deleted game result with id {result_id}")
    return {"message": "Game result deleted successfully"}


@app.get("/")
def read_root():
    return {
        "message": "Welcome to the Math Game API",
        "version": "1.0.0",
        "endpoints": [
            "/sync_game_results",
            "/game_results",
            "/top_scores",
            "/player_stats/{player_name}",
            "/daily_leaderboard/{date}",
            "/delete_game_result/{result_id}"
        ]
    }


# Dashboard routes
@app.get("/")
async def root(request: Request):
    return templates.TemplateResponse("dashboard.html", {"request": request})


@app.get("/api_info")
def api_info():
    return {
        "message": "Welcome to the Math Game API",
        "version": "1.0.0",
        "endpoints": [
            "/sync_game_results",
            "/game_results",
            "/top_scores",
            "/player_stats/{player_name}",
            "/daily_leaderboard/{date}",
            "/delete_game_result/{result_id}"
        ]
    }


@app.get("/health")
def health_check():
    return {"status": "healthy"}
