CREATE TABLE keymap
(
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    characterid INT NOT NULL DEFAULT '0',
    key         INT NOT NULL DEFAULT '0',
    type        INT NOT NULL DEFAULT '0',
    action      INT NOT NULL DEFAULT '0'
);

CREATE TABLE quickslotkeymapped
(
    accountid INT    NOT NULL,
    keymap    BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (accountid) REFERENCES accounts (id) ON DELETE CASCADE
);