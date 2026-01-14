package dev.jaczerob.delfino.common.cache.login;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggedInUserRepository extends CrudRepository<LoggedInUser, Integer> {
}
