from fastapi import FastAPI, Depends, HTTPException
from sqlalchemy import create_engine, Column, Integer, String, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from pydantic import BaseModel
from datetime import datetime
import os

DATABASE_URL = os.getenv("DATABASE_URL")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

class GameResult(Base):
    __tablename__ = "game_results"

    id = Column(Integer, primary_key=True, index=True)
    play_date = Column(String)
    play_time = Column(String)
    duration = Column(Integer)
    correct_count = Column(Integer)
    created_at = Column(DateTime, default=datetime.utcnow)

Base.metadata.create_all(bind=engine)

class GameResultCreate(BaseModel):
    play_date: str
    play_time: str
    duration: int
    correct_count: int

app = FastAPI()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@app.post("/sync_game_results")
def sync_game_results(game_results: list[GameResultCreate], db: Session = Depends(get_db)):
    for result in game_results:
        db_result = GameResult(**result.dict())
        db.add(db_result)
    db.commit()
    return {"message": "Game results synced successfully"}

@app.get("/game_results")
def get_game_results(db: Session = Depends(get_db)):
    results = db.query(GameResult).order_by(GameResult.created_at.desc()).all()
    return results

@app.get("/top_scores")
def get_top_scores(db: Session = Depends(get_db)):
    top_scores = db.query(GameResult).order_by(GameResult.correct_count.desc(), GameResult.duration).limit(10).all()
    return top_scores

@app.get("/")
def read_root():
    return {"message": "Hello World"}