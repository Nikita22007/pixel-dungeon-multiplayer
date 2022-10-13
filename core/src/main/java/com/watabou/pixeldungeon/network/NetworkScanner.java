package com.watabou.pixeldungeon.network;

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Settings;

import org.jetbrains.annotations.NotNull;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkScanner {
    protected static ServicesListener listener;
    protected static RelaySD relayServer = null;

    public static boolean start(@NotNull ServicesListener listener) {
        boolean res = NSD.start(listener);
        NetworkScanner.listener = listener;
        if (PixelDungeon.onlineMode()) {
            relayServer = new RelaySD();
            relayServer.listener = listener;
            relayServer.start();
        }
        return res;
    }

    public static boolean stop() {
        boolean res = NSD.stop();
        if (relayServer != null) {
            relayServer.stopRelaySD();
            relayServer = null;
        }
        return res;
    }

    public static List<ServerInfo> getServerList() {
        List<ServerInfo> result = new ArrayList<ServerInfo>(NSD.getServerList());
        if (relayServer !=  null) {
            result.addAll(relayServer.getServerList());
        }
        return result;
    }

    public interface ServicesListener {
        public void OnServerConnected(ServerInfo info);
    }

    public static int getPortForServerID(int id) {
        if (relayServer == null){
            return 0;
        }
        return relayServer.getPortForServerID(id);
    }
}