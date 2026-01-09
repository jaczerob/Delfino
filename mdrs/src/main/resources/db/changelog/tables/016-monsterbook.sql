CREATE TABLE monsterbook
(
    charid INT NOT NULL,
    cardid INT NOT NULL,
    level  INT NOT NULL DEFAULT '1',
    PRIMARY KEY (charid, cardid),
    FOREIGN KEY (charid) REFERENCES characters (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE monstercarddata
(
    id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cardid INT NOT NULL DEFAULT '0',
    mobid  INT NOT NULL DEFAULT '0'
);