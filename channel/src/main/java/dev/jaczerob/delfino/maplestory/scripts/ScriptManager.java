package dev.jaczerob.delfino.maplestory.scripts;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.tools.HexTool;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import moe.maple.api.script.logic.ScriptAPI;
import moe.maple.api.script.model.*;
import moe.maple.api.script.model.object.user.UserObject;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScriptManager {
    private static ScriptManager INSTANCE = null;

    private final ApplicationContext applicationContext;

    public ScriptManager(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        INSTANCE = this;
    }

    public static ScriptManager getInstance() {
        return INSTANCE;
    }

    @PostConstruct
    public void setUp() {
        this.setUpMessengers();
    }

    public void runScript(final Client client, final String scriptName, final ScriptType scriptType) {
        final BaseScript script = switch (scriptType) {
            case NPC -> this.loadScript(scriptName, NpcScript.class);
            case FIELD -> this.loadScript(scriptName, FieldScript.class);
            case PORTAL -> this.loadScript(scriptName, PortalScript.class);
            case QUEST -> this.loadScript(scriptName, QuestScript.class);
            case REACTOR -> this.loadScript(scriptName, ReactorScript.class);
        };

        if (script == null) {
            log.warn("Script not found: {} of type {}", scriptName, scriptType);
            return;
        }

        script.setUserObject(new ScriptUserObject(client.getPlayer()));
        script.start();
    }

    private <T extends BaseScript> T loadScript(final String scriptName, final Class<T> clazz) {
        return this.applicationContext.getBean(scriptName, clazz);
    }

    private Packet createNPCTalkPacket(
            final int npc,
            final byte msgType,
            final String talk,
            final String endBytes,
            final byte speaker
    ) {
        return OutPacket.create(SendOpcode.NPC_TALK)
                .writeByte(4)
                .writeInt(npc)
                .writeByte(msgType)
                .writeByte(speaker)
                .writeString(talk)
                .writeBytes(HexTool.toBytes(endBytes));
    }

    private void setUpMessengers() {
        ScriptAPI.INSTANCE.setMessengerAskAccept((userObject, speakerType, speakerTemplateId, param, message) -> {
            final var client = this.getClient(userObject);
        });

        ScriptAPI.INSTANCE.setMessengerAskAvatar((userObject, speakerType, speakerTemplateId, param, message, options) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskBoxText((userObject, speakerTemplateId, speakerType, param, message, defaultText, column, row) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskMemberShopAvatar((userObject, speakerType, speakerTemplateId, param, message, options) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskMenu((userObject, speakerType, speakerTemplateId, param, message) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskNumber((userObject, speakerType, speakerTemplateId, param, message, defaultNumber, min, max) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskQuiz((userObject, speakerType, speakerTemplateId, param, title, problemText, hintText, min, max, remainInitialQuiz) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskSlideMenu((userObject, speakerType, speakerTemplateId, slideDlgEX, index, message) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskSpeedQuiz((userObject, speakerType, speakerTemplateId, param, type, answer, correct, remaining, remainInitialQuiz, title, problemText, hintText, min, max) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskText((userObject, speakerType, speakerTemplateId, param, message, defaultText, min, max) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAskYesNo((userObject, speakerType, speakerTemplateId, param, message) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerSayImage((userObject, speakerType, speakerTemplateId, param, imagePath) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerSay((userObject, speakerType, speakerTemplateId, replaceTemplateId, param, message, previous, next) -> {
            final var client = this.getClient(userObject);
            if (next) {
                client.sendPacket(this.createNPCTalkPacket(speakerTemplateId, (byte) 0, message, "00 01", (byte) 0));
            } else if (previous) {
                client.sendPacket(this.createNPCTalkPacket(speakerTemplateId, (byte) 0, message, "01 01", (byte) 0));
            }
        });

        ScriptAPI.INSTANCE.setMessengerMessage((userObject, type, message) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerBalloon((userObject, message, width, timeoutInSeconds) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerProgress((userObject, message) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerStatChanged((userObject, exclRequest) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerFieldObject((userObject, path) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerFieldScreen((userObject, path) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerFieldSound((userObject, path) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerFieldTremble((userObject, type, delay) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerAvatarOriented((userObject, path, durationInSeconds) -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerPlayPortalSE(userObject -> {
            final var client = this.getClient(userObject);

        });

        ScriptAPI.INSTANCE.setMessengerReservedEffect((userObject, path) -> {
            final var client = this.getClient(userObject);

        });
    }

    private Client getClient(final UserObject<?> userObject) {
        if (userObject.get() instanceof Client client) {
            return client;
        }

        throw new RuntimeException("client not found in script");
    }
}
