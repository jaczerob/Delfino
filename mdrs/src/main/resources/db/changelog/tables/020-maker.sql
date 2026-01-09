CREATE TABLE makercreatedata
(
    id              INT      NOT NULL,
    itemid          INT      NOT NULL,
    req_level       INT      NOT NULL,
    req_maker_level INT      NOT NULL,
    req_meso        INT      NOT NULL,
    req_item        INT      NOT NULL,
    req_equip       INT      NOT NULL,
    catalyst        INT      NOT NULL,
    quantity        SMALLINT NOT NULL,
    tuc             INT      NOT NULL
);

CREATE TABLE makerrecipedata
(
    itemid   INT      NOT NULL,
    req_item INT      NOT NULL,
    count    SMALLINT NOT NULL
);

CREATE TABLE makerrewarddata
(
    itemid   INT      NOT NULL,
    rewardid INT      NOT NULL,
    quantity SMALLINT NOT NULL,
    prob     INT      NOT NULL DEFAULT '100'
);

CREATE TABLE makerreagentdata
(
    itemid INT         NOT NULL,
    stat   VARCHAR(20) NOT NULL,
    value  SMALLINT    NOT NULL
);