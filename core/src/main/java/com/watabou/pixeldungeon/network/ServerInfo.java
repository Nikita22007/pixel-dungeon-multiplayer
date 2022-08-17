package com.watabou.pixeldungeon.network;

public abstract class ServerInfo {
    public String name = "no-name";
    public int players = 0;
    public int maxPlayers = 0;
    public boolean haveChallenges = false;

    public abstract ServerAddress getAddress();
}
