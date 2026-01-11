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
package dev.jaczerob.delfino.login.net.server.coordinator.session;

import dev.jaczerob.delfino.login.client.Client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ronan
 */
public class SessionCoordinator {
    private static final SessionCoordinator instance = new SessionCoordinator();

    public static SessionCoordinator getInstance() {
        return instance;
    }

    private final Map<Integer, Client> onlineClients = new HashMap<>();

    public static String getSessionRemoteHost(final Client client) {
        final var hwid = client.getHwid();

        if (hwid != null) {
            return client.getRemoteAddress() + "-" + hwid.hwid();
        } else {
            return client.getRemoteAddress();
        }
    }

    public void updateOnlineClient(final Client client) {
        if (client == null) {
            return;
        }

        final var accountId = client.getAccID();
        disconnectClientIfOnline(accountId);
        this.onlineClients.put(accountId, client);
    }

    private void disconnectClientIfOnline(int accountId) {
        Client ingameClient = onlineClients.get(accountId);
        if (ingameClient != null) {     // thanks MedicOP for finding out a loss of loggedin account uniqueness when using the CMS "Unstuck" feature
            ingameClient.forceDisconnect();
        }
    }

    public void closeLoginSession(final Client client) {
        if (client == null) {
            return;
        }

        final var nibbleHwid = client.getHwid();
        client.setHwid(null);
        if (nibbleHwid != null) {
            Client loggedClient = onlineClients.get(client.getAccID());
            if (loggedClient != null && loggedClient.getSessionId() == client.getSessionId()) {
                onlineClients.remove(client.getAccID());
            }
        }
    }

    public void closeSession(Client client, Boolean immediately) {
        if (client == null) {
            return;
        }

        final Hwid hwid = client.getHwid();
        client.setHwid(null);

        final boolean isGameSession = hwid != null;
        if (isGameSession) {
            onlineClients.remove(client.getAccID());
        } else {
            Client loggedClient = onlineClients.get(client.getAccID());

            if (loggedClient != null && loggedClient.getSessionId() == client.getSessionId()) {
                onlineClients.remove(client.getAccID());
            }
        }

        if (immediately != null && immediately) {
            client.closeSession();
        }
    }
}