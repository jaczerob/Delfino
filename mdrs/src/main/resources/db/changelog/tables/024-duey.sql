CREATE TABLE dueypackages
(
    PackageId  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ReceiverId INT         NOT NULL,
    SenderName VARCHAR(13) NOT NULL,
    Mesos      INT                  DEFAULT '0',
    TimeStamp  TIMESTAMP   NOT NULL DEFAULT '2015-01-01 05:00:00',
    Message    VARCHAR(200) NULL,
    Checked    INT                  DEFAULT '1',
    Type       INT                  DEFAULT '0'
);

CREATE TABLE dueyitems
(
    id              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    PackageId       INT NOT NULL DEFAULT '0',
    inventoryitemid INT NOT NULL DEFAULT '0'
);