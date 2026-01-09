package dev.jaczerob.delfino.mrds.repositories.characters;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Integer> {
    List<CharacterEntity> findByAccountID(int accountID);
}
