package com.watabou.pixeldungeon.network.scanners;

import com.watabou.pixeldungeon.network.NetworkScanner;
import com.watabou.pixeldungeon.network.ServerAddress;

public class RelayServerInfo extends ServerInfo {
    private int id;
    public RelayServerInfo(int id, String name, int players, int maxPlayers, boolean haveChallenges) {
        this.id = id;
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.haveChallenges = haveChallenges;
    }

    @Override
    public ServerAddress getAddress() {
        int port = NetworkScanner.getPortForServerID(id);
        if (port == 0){
            return null;
        }
        ServerAddress address = new ServerAddress();
        address.host = RelaySD.getRelayAddress();
        address.port = port;
        return address;
    }
}
