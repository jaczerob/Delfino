package dev.jaczerob.delfino.login.cache.login;

import dev.jaczerob.delfino.login.client.LoginStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("logged_in_user")
@Getter
@Setter
public class LoggedInUser implements Serializable {
    private int id;
    private LoginStatus status;
}
