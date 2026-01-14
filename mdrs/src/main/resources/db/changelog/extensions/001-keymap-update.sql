ALTER TABLE quickslotkeymapped
    ADD CONSTRAINT uq_accountid_keymap
        UNIQUE (accountid, keymap);