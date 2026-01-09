CREATE TABLE questactions
(
    questactionid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    questid       INT  NOT NULL DEFAULT '0',
    status        INT  NOT NULL DEFAULT '0',
    data          TEXT NOT NULL
);

CREATE TABLE questprogress
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid   INT         NOT NULL,
    queststatusid INT         NOT NULL DEFAULT '0',
    progressid    INT         NOT NULL DEFAULT '0',
    progress      VARCHAR(15) NOT NULL DEFAULT ''
);

CREATE TABLE questrequirements
(
    questrequirementid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    questid            INT  NOT NULL DEFAULT '0',
    status             INT  NOT NULL DEFAULT '0',
    data               TEXT NOT NULL
);

CREATE TABLE queststatus
(
    queststatusid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid   INT    NOT NULL DEFAULT '0',
    quest         INT    NOT NULL DEFAULT '0',
    status        INT    NOT NULL DEFAULT '0',
    time          INT    NOT NULL DEFAULT '0',
    expires       BIGINT NOT NULL DEFAULT '0',
    forfeited     INT    NOT NULL DEFAULT '0',
    completed     INT    NOT NULL DEFAULT '0',
    info          INT    NOT NULL DEFAULT '0'
);

CREATE TABLE area_info
(
    id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charid INT          NOT NULL,
    area   INT          NOT NULL,
    info   VARCHAR(200) NOT NULL
);

CREATE TABLE eventstats
(
    characterid INT         NOT NULL,
    name        VARCHAR(11) NOT NULL DEFAULT '0',
    info        INT         NOT NULL
);

CREATE TABLE medalmaps
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid   INT NOT NULL,
    queststatusid INT NOT NULL,
    mapid         INT NOT NULL
);

CREATE INDEX medalmaps_queststatusid_index ON medalmaps (queststatusid);