CREATE TABLE wishlists
(
    id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    charid INT NOT NULL,
    sn     INT NOT NULL
);

CREATE TABLE specialcashitems
(
    id       INT NOT NULL,
    sn       INT NOT NULL,
    modifier INT NOT NULL,
    info     INT NOT NULL
);

CREATE TABLE nxcode
(
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       VARCHAR(17) NOT NULL UNIQUE,
    retriever  VARCHAR(13)          DEFAULT NULL,
    expiration BIGINT      NOT NULL DEFAULT '0'
);

CREATE TABLE nxcode_items
(
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codeid   INT NOT NULL,
    type     INT NOT NULL DEFAULT '5',
    item     INT NOT NULL DEFAULT '4000000',
    quantity INT NOT NULL DEFAULT '1'
);

CREATE TABLE nxcoupons
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    couponid  INT NOT NULL DEFAULT '0',
    rate      INT NOT NULL DEFAULT '0',
    activeday INT NOT NULL DEFAULT '0',
    starthour INT NOT NULL DEFAULT '0',
    endhour   INT NOT NULL DEFAULT '0'
)