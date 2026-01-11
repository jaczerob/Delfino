package dev.jaczerob.delfino.login.user;

import dev.jaczerob.delfino.grpc.proto.character.Character;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

@AllArgsConstructor
@Builder
public class LoginUser {
    private final int gmLevel;
    private final int accountId;
    private final String username;
    private final int characterSlots;
    private final String pin;
    private final String pic;
    private final List<Character> characters;

    private Character selectedCharacter;
}
