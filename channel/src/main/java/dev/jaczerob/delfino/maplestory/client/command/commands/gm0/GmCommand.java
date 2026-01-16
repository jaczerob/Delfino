/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
   @Author: Arthur L - Refactored command content into modules
*/
package dev.jaczerob.delfino.maplestory.client.command.commands.gm0;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.command.Command;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GmCommand extends Command {
    {
        setDescription("Send a message to the game masters.");
    }

    private static final Logger log = LoggerFactory.getLogger(GmCommand.class);

    @Override
    public void execute(Client c, String[] params) {
        String[] tips = {
                "Please only use @gm in emergencies or to report somebody.",
                "To report a bug or make a suggestion, use the forum.",
                "Please do not use @gm to ask if a GM is online.",
                "Do not ask if you can receive help, just state your issue.",
                "Do not say 'I have a bug to report', just state it.",
        };
        Character player = c.getPlayer();
        if (params.length < 1 || params[0].length() < 3) { // #goodbye 'hi'
            player.dropMessage(5, "Your message was too short. Please provide as much detail as possible.");
            return;
        }
        String message = player.getLastCommandMessage();
        Server.getInstance().broadcastGMMessage(c.getWorld(), ChannelPacketCreator.getInstance().sendYellowTip("[GM Message]:" + Character.makeMapleReadable(player.getName()) + ": " + message));
        Server.getInstance().broadcastGMMessage(c.getWorld(), ChannelPacketCreator.getInstance().serverNotice(1, message));
        log.info("{}: {}", Character.makeMapleReadable(player.getName()), message);
        player.dropMessage(5, "Your message '" + message + "' was sent to GMs.");
        player.dropMessage(5, tips[Randomizer.nextInt(tips.length)]);
    }
}
