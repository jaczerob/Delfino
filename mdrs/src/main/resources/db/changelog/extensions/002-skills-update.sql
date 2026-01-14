ALTER TABLE skills
    ADD CONSTRAINT uq_characterid_skillid
        UNIQUE (characterid, skillid);