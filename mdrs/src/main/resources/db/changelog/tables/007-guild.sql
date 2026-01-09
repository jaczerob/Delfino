CREATE TABLE guilds
(
    guildid     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    leader      INT         NOT NULL DEFAULT '0',
    GP          INT         NOT NULL DEFAULT '0',
    logo        INT                  DEFAULT NULL,
    logoColor   SMALLINT    NOT NULL DEFAULT '0',
    name        VARCHAR(45) NOT NULL,
    rank1title  VARCHAR(45) NOT NULL DEFAULT 'Master',
    rank2title  VARCHAR(45) NOT NULL DEFAULT 'Jr. Master',
    rank3title  VARCHAR(45) NOT NULL DEFAULT 'Member',
    rank4title  VARCHAR(45) NOT NULL DEFAULT 'Member',
    rank5title  VARCHAR(45) NOT NULL DEFAULT 'Member',
    capacity    INT         NOT NULL DEFAULT '10',
    logoBG      INT                  DEFAULT NULL,
    logoBGColor SMALLINT    NOT NULL DEFAULT '0',
    notice      VARCHAR(101)         DEFAULT NULL,
    signature   INT         NOT NULL DEFAULT '0',
    allianceId  INT         NOT NULL DEFAULT '0'
);

CREATE INDEX guilds_name_index ON guilds (guildid, name);

CREATE TABLE bbs_replies
(
    replyid   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    threadid  INT         NOT NULL,
    postercid INT         NOT NULL,
    timestamp BIGINT      NOT NULL,
    content   VARCHAR(26) NOT NULL DEFAULT ''
);

CREATE TABLE bbs_threads
(
    threadid      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    postercid     INT         NOT NULL,
    name          VARCHAR(26) NOT NULL DEFAULT '',
    timestamp     BIGINT      NOT NULL,
    icon          SMALLINT    NOT NULL,
    replycount    SMALLINT    NOT NULL DEFAULT '0',
    startpost     TEXT        NOT NULL,
    guildid       INT         NOT NULL,
    localthreadid INT         NOT NULL
);

CREATE TABLE alliance
(
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR(13) NOT NULL,
    capacity INT         NOT NULL DEFAULT '2',
    notice   VARCHAR(20) NOT NULL DEFAULT '',
    rank1    VARCHAR(11) NOT NULL DEFAULT 'Master',
    rank2    VARCHAR(11) NOT NULL DEFAULT 'Jr. Master',
    rank3    VARCHAR(11) NOT NULL DEFAULT 'Member',
    rank4    VARCHAR(11) NOT NULL DEFAULT 'Member',
    rank5    VARCHAR(11) NOT NULL DEFAULT 'Member'
);

CREATE INDEX alliance_name_index ON alliance (name);

CREATE TABLE allianceguilds
(
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    allianceid INT NOT NULL DEFAULT '-1',
    guildid    INT NOT NULL DEFAULT '-1'
)