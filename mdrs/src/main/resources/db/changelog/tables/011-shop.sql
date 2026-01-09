CREATE TABLE shops
(
    shopid INT PRIMARY KEY,
    npcid  INT NOT NULL DEFAULT '0'
);

CREATE TABLE shopitems
(
    shopitemid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shopid     INT NOT NULL,
    itemid     INT NOT NULL,
    price      INT NOT NULL,
    pitch      INT NOT NULL DEFAULT '0',
    position   INT NOT NULL
)