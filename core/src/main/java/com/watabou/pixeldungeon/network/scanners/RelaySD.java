package com.watabou.pixeldungeon.network.scanners;

import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Settings;
import com.watabou.pixeldungeon.network.NetworkScanner;
import com.watabou.pixeldungeon.network.scanners.RelayServerInfo;
import com.watabou.pixeldungeon.network.scanners.ServerInfo;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class RelaySD extends Thread implements ServiceDiscovery{

    private static final String CHARSET = "UTF-8";
    private static final int DELAY = 3000;
    protected OutputStreamWriter writeStream;
    protected BufferedWriter writer;
    protected InputStreamReader readStream;
    private BufferedReader reader;
    protected Socket relaySocket;

    private List<ServerInfo> servers = new ArrayList<>();
    ServiceDiscoveryListener listener;

    private int getRelayPort(){
        if (!PixelDungeon.useCustomRelay()){
            return Settings.defaultRelayServerPort;
        }
        int port = PixelDungeon.customRelayPort();
        return (port != 0)? port: Settings.defaultRelayServerPort;
    }

    static String getRelayAddress(){
        if (!PixelDungeon.useCustomRelay()){
            return Settings.defaultRelayServerAddress;
        }
        String address = PixelDungeon.customRelayAddress();
        return (!"".equals(address))? address : Settings.defaultRelayServerAddress;
    }

    public void run() {
        if (listener == null){
            return;
        }
        while (!Thread.currentThread().isInterrupted()) {
            Socket socket = null;
            try {
                socket = new Socket(getRelayAddress(), getRelayPort());
            } catch (IOException e) {
                e.printStackTrace();
                GLog.h("relay thread stopped, no restart");
                return;
            }
            this.relaySocket = socket;
            try {
                writeStream = new OutputStreamWriter(
                        relaySocket.getOutputStream(),
                        Charset.forName(CHARSET).newEncoder()
                );
                readStream = new InputStreamReader(
                        relaySocket.getInputStream(),
                        Charset.forName(CHARSET).newDecoder()
                );
                reader = new BufferedReader(readStream);
                writer = new BufferedWriter(writeStream, 16384);

                while (true) {
                    try {
                        Thread.sleep(DELAY);
                        synchronized (writer) {
                            JSONObject get_request = new JSONObject();
                            get_request.put("action", "get");
                            writer.write(get_request.toString());
                            writer.write('\n');
                            writer.flush();
                            String json = reader.readLine();
                            if (json == null) {
                                GLog.h("relay thread stopped, restarting");
                                socket.close();
                                break;
                            }
                            JSONObject servers_obj = new JSONObject(json);
                            updateServers(servers_obj);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
                GLog.h("relay thread stopped,restarting");

            }
            catch(InterruptedException e) {
                e.printStackTrace();
                GLog.h("relay thread stopped, no restart");
                return;
            }
        }
        GLog.h("relay thread stopped, no restart");
    }

    private void updateServers(JSONObject servers_obj) throws JSONException {
        JSONArray arr = servers_obj.getJSONArray("servers");
        List<ServerInfo> serverAddresses = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i += 1) {
            JSONObject infoObj = arr.optJSONObject(i);
            if (infoObj == null) {
                break;
            }
            String name = infoObj.getString("name");
            int id = infoObj.getInt("id");
            RelayServerInfo info = new RelayServerInfo(
                    id,
                    name,
                    0,
                    0,
                    false
            );
            serverAddresses.add(info);
        }
        servers = serverAddresses;
        listener.onServiceFound(null);
    }

    public boolean started() {
        return !Thread.currentThread().isInterrupted();
    }

    protected void stopRelaySD() {
        this.interrupt();
        try {
            if (relaySocket != null) {
                relaySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<ServerInfo> getServerList() {
        return servers;
    }

    public int getPortForServerID(int id) {
        if (relaySocket == null){
            return 0;
        }
        if (!relaySocket.isConnected()){
            return 0;
        }
        try {
            synchronized (writer) {
                JSONObject get_request = new JSONObject();
                get_request.put("action", "connect");
                get_request.put("server", id);
                writer.write(get_request.toString());
                writer.write('\n');
                writer.flush();
                String json = reader.readLine();
                if (json == null) {
                    return 0;
                }
                JSONObject port_obj = new JSONObject(json);
                return port_obj.getInt("port");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean startDiscovery(ServiceDiscoveryListener listener) {
        this.listener = listener;
        start();
        return true;
    }

    @Override
    public boolean stopDiscovery(){
        stopRelaySD();
        return true;
    }
}
