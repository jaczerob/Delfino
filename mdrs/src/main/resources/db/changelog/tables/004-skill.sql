CREATE TABLE skills
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    skillid     INT    NOT NULL DEFAULT '0',
    characterid INT    NOT NULL DEFAULT '0',
    skilllevel  INT    NOT NULL DEFAULT '0',
    masterlevel INT    NOT NULL DEFAULT '0',
    expiration  BIGINT NOT NULL DEFAULT '-1',
    FOREIGN KEY (characterid) REFERENCES characters (id) ON DELETE CASCADE
);

CREATE INDEX skills_pair_index ON skills (skillid, characterid);

CREATE TABLE cooldowns
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charid    INT    NOT NULL,
    SkillID   INT    NOT NULL,
    length    BIGINT NOT NULL,
    StartTime BIGINT NOT NULL
);

CREATE TABLE skillmacros
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid INT NOT NULL DEFAULT '0',
    position    INT NOT NULL DEFAULT '0',
    skill1      INT NOT NULL DEFAULT '0',
    skill2      INT NOT NULL DEFAULT '0',
    skill3      INT NOT NULL DEFAULT '0',
    name        VARCHAR(13)  DEFAULT NULL,
    shout       INT NOT NULL DEFAULT '0'
);