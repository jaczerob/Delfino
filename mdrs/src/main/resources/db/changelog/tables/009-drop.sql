CREATE TABLE drop_data
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dropperid        INT NOT NULL,
    itemid           INT NOT NULL DEFAULT '0',
    minimum_quantity INT NOT NULL DEFAULT '1',
    maximum_quantity INT NOT NULL DEFAULT '1',
    questid          INT NOT NULL DEFAULT '0',
    chance           INT NOT NULL DEFAULT '0'
);

CREATE INDEX drop_data_mobid ON drop_data (dropperid);
CREATE INDEX drop_data_dropper_itemd ON drop_data (dropperid, itemid);

CREATE TABLE drop_data_global
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    continent        INT NOT NULL DEFAULT '-1',
    itemid           INT NOT NULL DEFAULT '0',
    minimum_quantity INT NOT NULL DEFAULT '1',
    maximum_quantity INT NOT NULL DEFAULT '1',
    questid          INT NOT NULL DEFAULT '0',
    chance           INT NOT NULL DEFAULT '0',
    comments         VARCHAR(45)  DEFAULT NULL
);

CREATE INDEX drop_data_global_continent ON drop_data_global (continent);

CREATE TABLE reactordrops
(
    reactordropid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reactorid     INT NOT NULL,
    itemid        INT NOT NULL,
    chance        INT NOT NULL,
    questid       INT NOT NULL DEFAULT '-1'
);

CREATE INDEX reactordrops_reactorid ON reactordrops (reactorid);