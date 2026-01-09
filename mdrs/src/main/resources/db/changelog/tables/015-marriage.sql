CREATE TABLE marriages
(
    marriageid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    husbandid  INT NOT NULL DEFAULT '0',
    wifeid     INT NOT NULL DEFAULT '0'
);

CREATE TABLE rings
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partnerRingId INT          NOT NULL DEFAULT '0',
    partnerChrId  INT          NOT NULL DEFAULT '0',
    itemid        INT          NOT NULL DEFAULT '0',
    partnername   VARCHAR(255) NOT NULL
);