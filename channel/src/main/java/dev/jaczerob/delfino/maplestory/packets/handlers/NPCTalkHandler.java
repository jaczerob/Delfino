package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.processor.npc.DueyProcessor;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.id.NpcId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.npc.NPCScriptManager;
import dev.jaczerob.delfino.maplestory.server.life.NPC;
import dev.jaczerob.delfino.maplestory.server.life.PlayerNPC;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public final class NPCTalkHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(NPCTalkHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.NPC_TALK;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!client.getPlayer().isAlive()) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        if (currentServerTime() - client.getPlayer().getNpcCooldown() < YamlConfig.config.server.BLOCK_NPC_RACE_CONDT) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        }


        int oid = packet.readInt();
        MapObject obj = client.getPlayer().getMap().getMapObject(oid);
        if (obj instanceof NPC npc) {
            final var lastNPCId = MDC.get("player.npc.current.id");
            final var lastNPCName = MDC.get("player.npc.current.name");

            MDC.put("player.npc.last.id", lastNPCId == null ? "null" : lastNPCId);
            MDC.put("player.npc.last.name", lastNPCName == null ? "null" : lastNPCName);
            MDC.put("player.npc.current.id", String.valueOf(npc.getId()));
            MDC.put("player.npc.current.name", npc.getName());

            log.debug("Player is talking to an NPC");

            if (YamlConfig.config.server.USE_DEBUG) {
                client.getPlayer().dropMessage(5, "Talking to NPC " + npc.getId());
            }

            if (npc.getId() == NpcId.DUEY) {
                DueyProcessor.dueySendTalk(client, false);
            } else {
                if (client.getCM() != null || client.getQM() != null) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                // Custom handling to reduce the amount of scripts needed.
                if (npc.getId() >= NpcId.GACHAPON_MIN && npc.getId() <= NpcId.GACHAPON_MAX) {
                    NPCScriptManager.getInstance().start(client, npc.getId(), "gachapon", null);
                } else if (npc.getName().endsWith("Maple TV")) {
                    NPCScriptManager.getInstance().start(client, npc.getId(), "mapleTV", null);
                } else {
                    boolean hasNpcScript = NPCScriptManager.getInstance().start(client, npc.getId(), oid, null);
                    if (!hasNpcScript) {
                        if (!npc.hasShop()) {
                            log.warn("NPC {} ({}) is not coded", npc.getName(), npc.getId());
                            return;
                        } else if (client.getPlayer().getShop() != null) {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
                            return;
                        }

                        npc.sendShop(client);
                    }
                }
            }
        } else if (obj instanceof PlayerNPC pnpc) {
            NPCScriptManager nsm = NPCScriptManager.getInstance();

            if (pnpc.getScriptId() < NpcId.CUSTOM_DEV && !nsm.isNpcScriptAvailable(client, "" + pnpc.getScriptId())) {
                nsm.start(client, pnpc.getScriptId(), "rank_user", null);
            } else {
                log.debug("Getting npc script for client for: {}", pnpc.getScriptId());
                nsm.start(client, pnpc.getScriptId(), null);
            }
        }
    }
}
