package dev.jaczerob.delfino.maplestory.scripts;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.tools.HexTool;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import moe.maple.api.script.logic.ScriptAPI;
import moe.maple.api.script.model.BaseScript;
import moe.maple.api.script.model.FieldScript;
import moe.maple.api.script.model.NpcScript;
import moe.maple.api.script.model.PortalScript;
import moe.maple.api.script.model.QuestScript;
import moe.maple.api.script.model.ReactorScript;
import moe.maple.api.script.model.object.user.UserObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScriptManager {
    private static ScriptManager INSTANCE = null;

    private final Map<String, NpcScript> npcScripts;
    private final Map<String, FieldScript> fieldScripts;
    private final Map<String, PortalScript> portalScripts;
    private final Map<String, QuestScript> questScripts;
    private final Map<String, ReactorScript> reactorScripts;

    public ScriptManager(final List<BaseScript> scripts) {
        this.npcScripts = this.loadScripts(scripts, NpcScript.class);
        this.fieldScripts = this.loadScripts(scripts, FieldScript.class);
        this.portalScripts = this.loadScripts(scripts, PortalScript.class);
        this.questScripts = this.loadScripts(scripts, QuestScript.class);
        this.reactorScripts = this.loadScripts(scripts, ReactorScript.class);

        INSTANCE = this;
    }

    public static ScriptManager getInstance() {
        return INSTANCE;
    }

    @PostConstruct
    public void setUp() {
        this.setUpMessengers();
    }

    public void runScript(final String scriptName, final ScriptType scriptType) {
        final BaseScript script = switch (scriptType) {
            case NPC -> this.npcScripts.get(scriptName);
            case FIELD -> this.fieldScripts.get(scriptName);
            case PORTAL -> this.portalScripts.get(scriptName);
            case QUEST -> this.questScripts.get(scriptName);
            case REACTOR -> this.reactorScripts.get(scriptName);
        };

        if (script == null) {
            log.warn("Script not found: {} of type {}", scriptName, scriptType);
            return;
        }

        script.start();
    }

    private <T extends BaseScript> Map<String, T> loadScripts(final List<BaseScript> scripts, final Class<T> clazz) {
        return scripts.stream()
                .filter(clazz::isInstance)
                .map(script -> (T) script)
                .collect(Collectors.toMap(
                        BaseScript::name,
                        script -> script
                ));
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
