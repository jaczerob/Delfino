CREATE TABLE storages
(
    storageid INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    accountid INT NOT NULL DEFAULT '0',
    world     INT NOT NULL,
    slots     INT NOT NULL DEFAULT '0',
    meso      INT NOT NULL DEFAULT '0'
);

CREATE TABLE fredstorage
(
    id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cid       INT       NOT NULL UNIQUE,
    daynotes  INT       NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);