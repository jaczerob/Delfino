package dev.jaczerob.delfino.common.cache.login;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "delfino.cache.login", havingValue = "true")
public interface LoggedInUserRepository extends CrudRepository<LoggedInUser, Integer> {
}
