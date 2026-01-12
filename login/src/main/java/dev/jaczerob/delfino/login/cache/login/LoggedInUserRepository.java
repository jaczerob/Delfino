package dev.jaczerob.delfino.login.cache.login;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggedInUserRepository extends CrudRepository<LoggedInUser, Integer> {
}
