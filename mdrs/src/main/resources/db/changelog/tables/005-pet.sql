CREATE TABLE pets
(
    petid     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name      VARCHAR(13)  DEFAULT NULL,
    level     INT NOT NULL,
    closeness INT NOT NULL,
    fullness  INT NOT NULL,
    summoned  INT NOT NULL DEFAULT '0',
    flag      INT NOT NULL DEFAULT '0'
);

CREATE TABLE petignores
(
    id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    petid  INT NOT NULL,
    itemid INT NOT NULL,
    CONSTRAINT fk_petignorepetid FOREIGN KEY (petid) REFERENCES pets (petid) ON DELETE CASCADE
);