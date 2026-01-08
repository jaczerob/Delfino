/*
    This file is part of the HeavenMS MapleStory Server
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
package dev.jaczerob.delfino.login.net.server.coordinator.login;

import dev.jaczerob.delfino.login.config.YamlConfig;
import dev.jaczerob.delfino.login.net.server.Server;
import dev.jaczerob.delfino.login.net.server.coordinator.session.Hwid;
import dev.jaczerob.delfino.login.tools.Pair;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Ronan
 */
public class LoginBypassCoordinator {
    private final static LoginBypassCoordinator instance = new LoginBypassCoordinator();

    public static LoginBypassCoordinator getInstance() {
        return instance;
    }

    private final ConcurrentHashMap<Pair<Hwid, Integer>, Pair<Boolean, Long>> loginBypass = new ConcurrentHashMap<>();   // optimized PIN & PIC check

    public boolean canLoginBypass(Hwid hwid, int accId, boolean pic) {
        return false;
    }

    public void registerLoginBypassEntry(Hwid hwid, int accId, boolean pic) {
        long expireTime = (pic ? YamlConfig.config.server.BYPASS_PIC_EXPIRATION : YamlConfig.config.server.BYPASS_PIN_EXPIRATION);
        if (expireTime > 0) {
            Pair<Hwid, Integer> entry = new Pair<>(hwid, accId);
            expireTime = Server.getInstance().getCurrentTime() + MINUTES.toMillis(expireTime);
            try {
                pic |= loginBypass.get(entry).getLeft();
                expireTime = Math.max(loginBypass.get(entry).getRight(), expireTime);
            } catch (NullPointerException npe) {
            }

            loginBypass.put(entry, new Pair<>(pic, expireTime));
        }
    }

    public void runUpdateLoginBypass() {
    }
}
