CREATE TABLE accounts
(
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(13) UNIQUE,
    password       VARCHAR(128) NOT NULL DEFAULT '',
    pin            VARCHAR(10)  NOT NULL DEFAULT '',
    pic            VARCHAR(26)  NOT NULL DEFAULT '',
    loggedin       INT          NOT NULL DEFAULT '0',
    lastlogin      TIMESTAMP NULL     DEFAULT NULL,
    createdat      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    birthday       DATE         NOT NULL DEFAULT '2005-05-11',
    banned         INT          NOT NULL DEFAULT '0',
    banreason      TEXT,
    macs           TEXT,
    nxCredit       INT                   DEFAULT NULL,
    maplePoint     INT                   DEFAULT NULL,
    nxPrepaid      INT                   DEFAULT NULL,
    characterslots INT          NOT NULL DEFAULT '3',
    gender         INT          NOT NULL DEFAULT '10',
    tempban        TIMESTAMP    NOT NULL DEFAULT '2005-05-11 00:00:00',
    greason        INT          NOT NULL DEFAULT '0',
    tos            INT          NOT NULL DEFAULT '0',
    sitelogged     TEXT,
    webadmin       INT                   DEFAULT '0',
    nick           VARCHAR(20)           DEFAULT NULL,
    mute           INT                   DEFAULT '0',
    email          VARCHAR(45)           DEFAULT NULL,
    ip             TEXT,
    rewardpoints   INT          NOT NULL DEFAULT '0',
    votepoints     INT          NOT NULL DEFAULT '0',
    hwid           VARCHAR(12)  NOT NULL DEFAULT '',
    language       INT          NOT NULL DEFAULT '2'
);

CREATE INDEX account_banned_index ON accounts (id, banned);
CREATE INDEX account_name_index ON accounts (id, name);
CREATE INDEX account_nx_index ON accounts (id, nxCredit, maplePoint, nxPrepaid);
