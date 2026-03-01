DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS ai_fight_participation CASCADE;
DROP TABLE IF EXISTS pvp_fights CASCADE;
DROP TABLE IF EXISTS ai_fights CASCADE;
DROP TABLE IF EXISTS bidjus CASCADE;
DROP TABLE IF EXISTS spell_knowledge CASCADE;
DROP TABLE IF EXISTS private_messages CASCADE;
DROP TABLE IF EXISTS friend_requesst CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS spells CASCADE;
DROP TABLE IF EXISTS game_characters CASCADE;
DROP TABLE IF EXISTS character_appearances CASCADE;
DROP TABLE IF EXISTS statistics CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE roles (
    role VARCHAR(5) PRIMARY KEY
);

CREATE TABLE statistics (
    id SERIAL PRIMARY KEY,
    rating INT NOT NULL,
    fights INT NOT NULL,
    wins INT NOT NULL,
    losses INT NOT NULL,
    deaths INT NOT NULL,
    lvl INT NOT NULL,
    experience INT NOT NULL,
    upgrade_points INT NOT NULL
);

CREATE TABLE character_appearances (
    id SERIAL PRIMARY KEY,
    gender VARCHAR(6) NOT NULL,
    skin_colour VARCHAR(5) NOT NULL,
    hair_colour VARCHAR(6) NOT NULL,
    clothes_colour VARCHAR(5)
);

CREATE TABLE game_characters (
    id SERIAL PRIMARY KEY,
    creation_date TIMESTAMP,
    animal_race VARCHAR(255),
    max_chakra_amount INT NOT NULL,
    max_hp INT NOT NULL,
    physical_damage INT NOT NULL,
    resistance REAL NOT NULL,
    appearance_id INT,
    FOREIGN KEY (appearance_id) REFERENCES character_appearances(id)
);

CREATE TABLE spells (
    name VARCHAR(20) PRIMARY KEY,
    base_damage INT NOT NULL,
    damage_per_level INT NOT NULL,
    base_chakra_consumption INT NOT NULL,
    chakra_consumption_per_level INT NOT NULL,
    req_level INT NOT NULL
);

CREATE TABLE users (
    login VARCHAR(30) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    vk_id INT,
    google_id VARCHAR(255) UNIQUE,
    character_id INT,
    stats_id INT,
    FOREIGN KEY (character_id) REFERENCES game_characters(id),
    FOREIGN KEY (stats_id) REFERENCES statistics(id)
);

-- Add unique constraint for character_id and stats_id to enforce OneToOne
ALTER TABLE users
ADD CONSTRAINT fk_character_unique UNIQUE (character_id);
ALTER TABLE users
ADD CONSTRAINT fk_stats_unique UNIQUE (stats_id);

CREATE TABLE private_messages (
    message_id SERIAL PRIMARY KEY,
    receiver VARCHAR(30) NOT NULL,
    sender VARCHAR(30) NOT NULL,
    sending_time TIMESTAMP NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL,
    FOREIGN KEY (receiver) REFERENCES users(login),
    FOREIGN KEY (sender) REFERENCES users(login)
);

CREATE TABLE friend_requests (
    request_id SERIAL PRIMARY KEY,
    friend_user VARCHAR(30) NOT NULL,
    requesting_user VARCHAR(30) NOT NULL,
    FOREIGN KEY (friend_user) REFERENCES users(login),
    FOREIGN KEY (requesting_user) REFERENCES users(login)
);

CREATE TABLE spell_knowledge (
    handling_id SERIAL PRIMARY KEY,
    character_id INT NOT NULL,
    spell VARCHAR(20) NOT NULL,
    spell_level INT NOT NULL,
    FOREIGN KEY (character_id) REFERENCES game_characters(id),
    FOREIGN KEY (spell) REFERENCES spells(name)
);

CREATE TABLE bidjus (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    number_of_tails INT NOT NULL,
    max_chakra_amount INT NOT NULL,
    max_hp INT NOT NULL,
    physical_damage INT NOT NULL,
    current_hp INT NOT NULL,
    current_chakra INT NOT NULL
);

CREATE TABLE ai_fights (
    id SERIAL PRIMARY KEY,
    fight_date TIMESTAMP NOT NULL,
    boss INT NOT NULL,
    time_left BIGINT,
    current_name VARCHAR(255),
    fighter1_login VARCHAR(30),
    fighter2_login VARCHAR(30),
    -- Add columns for animals1 and animals2 if they are persisted directly in FightVsAI
    -- Otherwise, a separate join table or embedded JSON might be needed
    FOREIGN KEY (boss) REFERENCES bidjus(id),
    FOREIGN KEY (fighter1_login) REFERENCES users(login),
    FOREIGN KEY (fighter2_login) REFERENCES users(login)
);

CREATE TABLE ai_fight_participation (
    fidentity SERIAL PRIMARY KEY,
    fight_id INT NOT NULL,
    id INT NOT NULL, -- This corresponds to GameCharacter ID
    fresult VARCHAR(4) NOT NULL,
    experience_gain INT NOT NULL,
    FOREIGN KEY (fight_id) REFERENCES ai_fights(id),
    FOREIGN KEY (id) REFERENCES game_characters(id)
);

CREATE TABLE pvp_fights (
    pvp_id SERIAL PRIMARY KEY,
    first_fighter INT NOT NULL,
    second_fighter INT NOT NULL,
    fight_date TIMESTAMP NOT NULL,
    first_won BOOLEAN NOT NULL,
    rating_change INT NOT NULL,
    time_left BIGINT,
    current_name VARCHAR(255),
    fighter1_login VARCHAR(30),
    fighter2_login VARCHAR(30),
    -- Add columns for animals1 and animals2 if they are persisted directly in FightPVP
    FOREIGN KEY (first_fighter) REFERENCES game_characters(id),
    FOREIGN KEY (second_fighter) REFERENCES game_characters(id),
    FOREIGN KEY (fighter1_login) REFERENCES users(login),
    FOREIGN KEY (fighter2_login) REFERENCES users(login)
);

CREATE TABLE friends (
    user1 VARCHAR(30) NOT NULL,
    user2 VARCHAR(30) NOT NULL,
    PRIMARY KEY (user1, user2),
    FOREIGN KEY (user1) REFERENCES users(login),
    FOREIGN KEY (user2) REFERENCES users(login)
);

CREATE TABLE user_roles (
    login VARCHAR(30) NOT NULL,
    role VARCHAR(5) NOT NULL,
    PRIMARY KEY (login, role),
    FOREIGN KEY (login) REFERENCES users(login),
    FOREIGN KEY (role) REFERENCES roles(role)
);

-- Initial Data for roles
INSERT INTO roles (role) VALUES ('USER');
INSERT INTO roles (role) VALUES ('ADMIN');
