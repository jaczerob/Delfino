CREATE TABLE hwidaccounts
(
    accountid INT         NOT NULL DEFAULT '0',
    hwid      VARCHAR(40) NOT NULL DEFAULT '',
    relevance INT         NOT NULL DEFAULT '0',
    expiresat TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hwidbans
(
    hwidbanid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hwid      VARCHAR(30) UNIQUE NOT NULL
);

CREATE TABLE ipbans
(
    ipbanid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ip      VARCHAR(40) NOT NULL DEFAULT '',
    aid     VARCHAR(40)          DEFAULT NULL
);

CREATE TABLE macbans
(
    macbanid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mac      VARCHAR(30) NOT NULL UNIQUE,
    aid      VARCHAR(40) DEFAULT NULL
);

CREATE TABLE macfilters
(
    macfilterid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filter      VARCHAR(30) NOT NULL
);

CREATE TABLE reports
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reporttime  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reporterid  INT       NOT NULL,
    victimid    INT       NOT NULL,
    reason      INT       NOT NULL,
    chatlog     TEXT      NOT NULL,
    description TEXT      NOT NULL
);