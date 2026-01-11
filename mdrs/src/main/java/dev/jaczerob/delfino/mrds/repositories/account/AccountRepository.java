package dev.jaczerob.delfino.mrds.repositories.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    @Query(
            value = """
                    SELECT a.id, a.name, a.password, a.pic, a.pin, c.gm
                    FROM accounts a
                    JOIN (
                        SELECT accountid, MAX(gm) AS gm
                        FROM characters
                        GROUP BY accountid
                    ) c ON a.id = c.accountid
                    WHERE a.name = ?1
                    LIMIT 1
                    """,
            nativeQuery = true
    )
    Optional<AccountEntity> findByUsername(String username);
}
