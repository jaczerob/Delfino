package dev.jaczerob.delfino.mrds.repositories.account;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity

@Getter
@Setter
public class AccountEntity {
    @Id
    private int id;
    private String username;
    private String password;
    private String pic;
    private String pin;
    private int gm;
}
