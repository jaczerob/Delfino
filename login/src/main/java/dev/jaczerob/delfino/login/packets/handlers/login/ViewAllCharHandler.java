/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public final class ViewAllCharHandler extends AbstractPacketHandler {
    private static final int CHARACTER_LIMIT = 60;

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.VIEW_ALL_CHAR;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client) {
        final var worldCharacters = new TreeMap<Integer, List<Character>>();
        worldCharacters.put(0, client.getCharacters());

        final var worldCharactersFormatted = limitTotalChrs(worldCharacters, CHARACTER_LIMIT);
        padChrsIfNeeded(worldCharactersFormatted);

        final var totalWorlds = worldCharactersFormatted.size();
        final var totalChrs = countTotalChrs(worldCharactersFormatted);
        client.sendPacket(LoginPacketCreator.getInstance().showAllCharacter(totalWorlds, totalChrs));

        worldCharactersFormatted.forEach((worldId, chrs) ->
                client.sendPacket(LoginPacketCreator.getInstance().showAllCharacterInfo(worldId, chrs, false))
        );
    }

    private static SortedMap<Integer, List<Character>> limitTotalChrs(final SortedMap<Integer, List<Character>> worldChrs, final int limit) {
        if (countTotalChrs(worldChrs) <= limit) {
            return worldChrs;
        } else {
            ;
            return cutAfterChrLimit(worldChrs, limit);
        }
    }

    private static int countTotalChrs(Map<Integer, List<Character>> worldChrs) {
        return worldChrs.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    private static SortedMap<Integer, List<Character>> cutAfterChrLimit(SortedMap<Integer, List<Character>> worldChrs,
                                                                        int limit) {
        SortedMap<Integer, List<Character>> cappedCopy = new TreeMap<>();
        int runningChrTotal = 0;
        for (Map.Entry<Integer, List<Character>> entry : worldChrs.entrySet()) {
            int worldId = entry.getKey();
            List<Character> chrs = entry.getValue();
            if (runningChrTotal + chrs.size() <= limit) { // Limit not reached, move them all
                runningChrTotal += chrs.size();
                cappedCopy.put(worldId, chrs);
            } else { // Limit would be reached if all chrs were moved. Move just enough to fit within limit.
                int remainingSlots = limit - runningChrTotal;
                List<Character> lastChrs = chrs.subList(0, remainingSlots);
                cappedCopy.put(worldId, lastChrs);
                break;
            }
        }

        return cappedCopy;
    }

    private static void padChrsIfNeeded(SortedMap<Integer, List<Character>> worldChrs) {
        while (shouldPadLastRow(countTotalChrs(worldChrs))) {
            final List<Character> lastWorldChrs = getLastWorldChrs(worldChrs);
            final Character lastChrForPadding = getLastItem(lastWorldChrs);
            lastWorldChrs.add(lastChrForPadding);
        }
    }

    private static boolean shouldPadLastRow(int totalChrs) {
        boolean shouldScroll = totalChrs > 9;
        boolean isLastRowFilled = totalChrs % 3 == 0;
        return shouldScroll && !isLastRowFilled;
    }

    private static List<Character> getLastWorldChrs(SortedMap<Integer, List<Character>> worldChrs) {
        return worldChrs.get(worldChrs.lastKey());
    }

    private static <T> T getLastItem(List<T> list) {
        Objects.requireNonNull(list);
        return list.get(list.size() - 1);
    }
}
