CREATE TABLE game_results (
    id SERIAL PRIMARY KEY,
    play_date VARCHAR(100),
    play_time VARCHAR(100),
    duration INTEGER,
    correct_count INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);