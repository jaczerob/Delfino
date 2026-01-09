CREATE TABLE namechanges
(
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid    INT         NOT NULL,
    old            VARCHAR(13) NOT NULL,
    new            VARCHAR(13) NOT NULL,
    requestTime    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completionTime TIMESTAMP NULL
);

CREATE TABLE worldtransfers
(
    id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid    INT       NOT NULL,
    "from"           INT       NOT NULL,
    "to"             INT       NOT NULL,
    requestTime    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completionTime TIMESTAMP NULL
);