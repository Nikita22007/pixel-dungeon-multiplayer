package com.watabou.pixeldungeon.network.scanners;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DirectServerParser {
    public static UserServerInfo fromAddress(InetSocketAddress address){
        try {
        Socket socket = new Socket();
        socket.connect(address);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        JSONObject object = new JSONObject(reader.readLine());
        socket.close();
        return new UserServerInfo(object, address, true);

        } catch (Exception ignored){

        }
        return null;
    }
}
