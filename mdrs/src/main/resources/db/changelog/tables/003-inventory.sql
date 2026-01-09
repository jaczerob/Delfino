CREATE TABLE inventoryitems
(
    inventoryitemid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type            INT         NOT NULL,
    characterid     INT                  DEFAULT NULL,
    accountid       INT                  DEFAULT NULL,
    itemid          INT         NOT NULL DEFAULT '0',
    inventorytype   INT         NOT NULL DEFAULT '0',
    position        INT         NOT NULL DEFAULT '0',
    quantity        INT         NOT NULL DEFAULT '0',
    owner           TEXT        NOT NULL,
    petid           INT         NOT NULL DEFAULT '-1',
    flag            INT         NOT NULL,
    expiration      BIGINT      NOT NULL DEFAULT '-1',
    giftFrom        VARCHAR(26) NOT NULL
);

CREATE INDEX inventoryitems_characterid_index ON inventoryitems (characterid);

CREATE TABLE inventoryequipment
(
    inventoryequipmentid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inventoryitemid      INT NOT NULL DEFAULT '0',
    upgradeslots         INT NOT NULL DEFAULT '0',
    level                INT NOT NULL DEFAULT '0',
    str                  INT NOT NULL DEFAULT '0',
    dex                  INT NOT NULL DEFAULT '0',
    int                  INT NOT NULL DEFAULT '0',
    luk                  INT NOT NULL DEFAULT '0',
    hp                   INT NOT NULL DEFAULT '0',
    mp                   INT NOT NULL DEFAULT '0',
    watk                 INT NOT NULL DEFAULT '0',
    matk                 INT NOT NULL DEFAULT '0',
    wdef                 INT NOT NULL DEFAULT '0',
    mdef                 INT NOT NULL DEFAULT '0',
    acc                  INT NOT NULL DEFAULT '0',
    avoid                INT NOT NULL DEFAULT '0',
    hands                INT NOT NULL DEFAULT '0',
    speed                INT NOT NULL DEFAULT '0',
    jump                 INT NOT NULL DEFAULT '0',
    locked               INT NOT NULL DEFAULT '0',
    vicious              INT NOT NULL DEFAULT '0',
    itemlevel            INT NOT NULL DEFAULT '1',
    itemexp              INT NOT NULL DEFAULT '0',
    ringid               INT NOT NULL DEFAULT '-1'
);

CREATE INDEX inventoryequipment_itemid_index ON inventoryequipment (inventoryitemid);

CREATE TABLE inventorymerchant
(
    inventorymerchantid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inventoryitemid     INT NOT NULL DEFAULT '0',
    characterid         INT          DEFAULT NULL,
    bundles             INT NOT NULL DEFAULT '0'
);

CREATE INDEX inventorymerchant_itemid_index ON inventorymerchant (inventoryitemid);
