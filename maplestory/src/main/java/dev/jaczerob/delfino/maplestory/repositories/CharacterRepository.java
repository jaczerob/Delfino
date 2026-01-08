package dev.jaczerob.delfino.maplestory.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Integer> {
    List<CharacterEntity> findByAccountid(int accountid);
}
