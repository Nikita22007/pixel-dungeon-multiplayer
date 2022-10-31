package com.watabou.pixeldungeon.network.scanners;

import com.watabou.pixeldungeon.network.ServerAddress;
import com.watabou.pixeldungeon.network.scanners.ServerInfo;

import java.net.InetAddress;

public class DirectServerInfo extends ServerInfo {
    private InetAddress IP;
    private int port;

    public DirectServerInfo(String name, InetAddress ip, int port, int players, int maxPlayers, boolean haveChallenges) {
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.IP = ip;
        this.port = port;
        this.haveChallenges = haveChallenges;
    }

    @Override
    public ServerAddress getAddress() {
        ServerAddress address = new ServerAddress();
        address.host = IP.getHostAddress();
        address.port = port;
        return address;
    }
}
