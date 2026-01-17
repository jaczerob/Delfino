package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResult;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.net.server.world.Messenger;
import dev.jaczerob.delfino.maplestory.net.server.world.MessengerCharacter;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class MessengerHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MESSENGER;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (client.tryacquireClient()) {
            try {
                String input;
                byte mode = packet.readByte();
                Character player = client.getPlayer();
                World world = client.getWorldServer();
                Messenger messenger = player.getMessenger();
                switch (mode) {
                    case 0x00:
                        int messengerid = packet.readInt();
                        if (messenger == null) {
                            if (messengerid == 0) {
                                InviteCoordinator.removeInvite(InviteType.MESSENGER, player.getId());

                                MessengerCharacter messengerplayer = new MessengerCharacter(player, 0);
                                messenger = world.createMessenger(messengerplayer);
                                player.setMessenger(messenger);
                                player.setMessengerPosition(0);
                            } else {
                                messenger = world.getMessenger(messengerid);
                                if (messenger != null) {
                                    InviteResult inviteRes = InviteCoordinator.answerInvite(InviteType.MESSENGER, player.getId(), messengerid, true);
                                    InviteResultType res = inviteRes.result;
                                    if (res == InviteResultType.ACCEPTED) {
                                        int position = messenger.getLowestPosition();
                                        MessengerCharacter messengerplayer = new MessengerCharacter(player, position);
                                        if (messenger.getMembers().size() < 3) {
                                            player.setMessenger(messenger);
                                            player.setMessengerPosition(position);
                                            world.joinMessenger(messenger.getId(), messengerplayer, player.getName(), messengerplayer.getChannel());
                                        }
                                    } else {
                                        player.message("Could not verify your Maple Messenger accept since the invitation rescinded.");
                                    }
                                }
                            }
                        } else {
                            InviteCoordinator.answerInvite(InviteType.MESSENGER, player.getId(), messengerid, false);
                        }
                        break;
                    case 0x02:
                        player.closePlayerMessenger();
                        break;
                    case 0x03:
                        if (messenger == null) {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().messengerChat(player.getName() + " : This Maple Messenger is currently unavailable. Please quit this chat."));
                        } else if (messenger.getMembers().size() < 3) {
                            input = packet.readString();
                            Character target = client.getChannelServer().getPlayerStorage().getCharacterByName(input);
                            if (target != null) {
                                if (target.getMessenger() == null) {
                                    if (InviteCoordinator.createInvite(InviteType.MESSENGER, client.getPlayer(), messenger.getId(), target.getId())) {
                                        target.sendPacket(ChannelPacketCreator.getInstance().messengerInvite(client.getPlayer().getName(), messenger.getId()));
                                        context.writeAndFlush(ChannelPacketCreator.getInstance().messengerNote(input, 4, 1));
                                    } else {
                                        context.writeAndFlush(ChannelPacketCreator.getInstance().messengerChat(player.getName() + " : " + input + " is already managing a Maple Messenger invitation"));
                                    }
                                } else {
                                    context.writeAndFlush(ChannelPacketCreator.getInstance().messengerChat(player.getName() + " : " + input + " is already using Maple Messenger"));
                                }
                            } else {
                                if (world.find(input) > -1) {
                                    world.messengerInvite(client.getPlayer().getName(), messenger.getId(), input, client.getChannel());
                                } else {
                                    context.writeAndFlush(ChannelPacketCreator.getInstance().messengerNote(input, 4, 0));
                                }
                            }
                        } else {
                            context.writeAndFlush(ChannelPacketCreator.getInstance().messengerChat(player.getName() + " : You cannot have more than 3 people in the Maple Messenger"));
                        }
                        break;
                    case 0x05:
                        String targeted = packet.readString();
                        world.declineChat(targeted, player);
                        break;
                    case 0x06:
                        if (messenger != null) {
                            MessengerCharacter messengerplayer = new MessengerCharacter(player, player.getMessengerPosition());
                            input = packet.readString();
                            world.messengerChat(messenger, input, messengerplayer.getName());
                        }
                        break;
                }
            } finally {
                client.releaseClient();
            }
        }
    }
}
