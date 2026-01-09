CREATE TABLE playerdiseases
(
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charid     INT NOT NULL,
    disease    INT NOT NULL,
    mobskillid INT NOT NULL,
    mobskilllv INT NOT NULL,
    length     INT NOT NULL DEFAULT '1'
);

CREATE TABLE buddies
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid INT NOT NULL,
    buddyid     INT NOT NULL,
    pending     INT NOT NULL DEFAULT '0',
    "group"       VARCHAR(17)  DEFAULT '0'
);

CREATE TYPE LOCATIONTYPE AS ENUM ('FREE_MARKET','WORLDTOUR','FLORINA','INTRO','SUNDAY_MARKET','MIRROR','EVENT','BOSSPQ','HAPPYVILLE','DEVELOPER','MONSTER_CARNIVAL');

CREATE TABLE savedlocations
(
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid  INT NOT NULL,
    locationtype LOCATIONTYPE NOT NULL,
    map          INT NOT NULL,
    portal       INT NOT NULL
);

CREATE TABLE famelog
(
    famelogid      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid    INT       NOT NULL DEFAULT '0',
    characterid_to INT       NOT NULL DEFAULT '0',
    "when"           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (characterid) REFERENCES characters (id) ON DELETE CASCADE
);

CREATE INDEX famelog_characterid_index ON famelog (characterid);

CREATE TABLE trocklocations
(
    trockid     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid INT NOT NULL,
    mapid       INT NOT NULL,
    vip         INT NOT NULL
);

CREATE TABLE characterexplogs
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    world_exp_rate INT,
    exp_coupon     INT,
    gained_exp     BIGINT,
    current_exp    INT,
    exp_gain_time  TIMESTAMP,
    charid         INT NOT NULL,
    FOREIGN KEY (charid) REFERENCES characters (id) ON DELETE CASCADE
)