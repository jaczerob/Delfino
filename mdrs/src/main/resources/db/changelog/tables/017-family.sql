CREATE TABLE family_character
(
    cid             INT    NOT NULL,
    familyid        INT    NOT NULL,
    seniorid        INT    NOT NULL,
    reputation      INT    NOT NULL DEFAULT '0',
    todaysrep       INT    NOT NULL DEFAULT '0',
    totalreputation INT    NOT NULL DEFAULT '0',
    reptosenior     INT    NOT NULL DEFAULT '0',
    precepts        VARCHAR(200)    DEFAULT NULL,
    lastresettime   BIGINT NOT NULL DEFAULT '0',
    FOREIGN KEY (cid) REFERENCES characters (id) ON DELETE CASCADE
);

CREATE TABLE family_entitlement
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charid        INT    NOT NULL,
    entitlementid INT    NOT NULL,
    timestamp     BIGINT NOT NULL DEFAULT '0'
);