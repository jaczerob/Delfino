CREATE TABLE gifts
(
    id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "to"    INT         NOT NULL,
    "from"  VARCHAR(13) NOT NULL,
    message TEXT        NOT NULL,
    sn      INT         NOT NULL,
    ringid  INT         NOT NULL
);

CREATE TABLE notes
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "to"      VARCHAR(13) NOT NULL DEFAULT '',
    "from"    VARCHAR(13) NOT NULL DEFAULT '',
    message   TEXT        NOT NULL,
    timestamp BIGINT      NOT NULL,
    fame      INT         NOT NULL DEFAULT '0',
    deleted   INT         NOT NULL DEFAULT '0'
);

CREATE TABLE newyear
(
    id              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    senderid        INT    NOT NULL DEFAULT '-1',
    sendername      VARCHAR(13)     DEFAULT '',
    receiverid      INT    NOT NULL DEFAULT '-1',
    receivername    VARCHAR(13)     DEFAULT '',
    message         VARCHAR(120)    DEFAULT '',
    senderdiscard   INT    NOT NULL DEFAULT '0',
    receiverdiscard INT    NOT NULL DEFAULT '0',
    received        INT    NOT NULL DEFAULT '0',
    timesent        BIGINT NOT NULL,
    timereceived    BIGINT NOT NULL
);