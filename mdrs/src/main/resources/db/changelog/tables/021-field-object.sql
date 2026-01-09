CREATE TABLE playernpcs
(
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         VARCHAR(13) NOT NULL,
    hair         INT         NOT NULL,
    face         INT         NOT NULL,
    skin         INT         NOT NULL,
    gender       INT         NOT NULL DEFAULT '0',
    x            INT         NOT NULL,
    cy           INT         NOT NULL DEFAULT '0',
    world        INT         NOT NULL DEFAULT '0',
    map          INT         NOT NULL DEFAULT '0',
    dir          INT         NOT NULL DEFAULT '0',
    scriptid     INT         NOT NULL DEFAULT '0',
    fh           INT         NOT NULL DEFAULT '0',
    rx0          INT         NOT NULL DEFAULT '0',
    rx1          INT         NOT NULL DEFAULT '0',
    worldrank    INT         NOT NULL DEFAULT '0',
    overallrank  INT         NOT NULL DEFAULT '0',
    worldjobrank INT         NOT NULL DEFAULT '0',
    job          INT         NOT NULL DEFAULT '0'
);

CREATE TABLE playernpcs_equip
(
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    npcid    INT NOT NULL DEFAULT '0',
    equipid  INT NOT NULL,
    type     INT NOT NULL DEFAULT '0',
    equippos INT NOT NULL
);

CREATE TABLE playernpcs_field
(
    id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    world  INT      NOT NULL,
    map    INT      NOT NULL,
    step   INT      NOT NULL DEFAULT '0',
    podium SMALLINT NOT NULL DEFAULT '0'
);

CREATE TABLE plife
(
    id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    world   INT        NOT NULL DEFAULT '-1',
    map     INT        NOT NULL DEFAULT '0',
    life    INT        NOT NULL DEFAULT '0',
    type    VARCHAR(1) NOT NULL DEFAULT 'n',
    cy      INT        NOT NULL DEFAULT '0',
    f       INT        NOT NULL DEFAULT '0',
    fh      INT        NOT NULL DEFAULT '0',
    rx0     INT        NOT NULL DEFAULT '0',
    rx1     INT        NOT NULL DEFAULT '0',
    x       INT        NOT NULL DEFAULT '0',
    y       INT        NOT NULL DEFAULT '0',
    hide    INT        NOT NULL DEFAULT '0',
    mobtime INT        NOT NULL DEFAULT '0',
    team    INT        NOT NULL DEFAULT '0'
);