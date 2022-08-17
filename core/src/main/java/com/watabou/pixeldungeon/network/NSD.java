package com.watabou.pixeldungeon.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.watabou.pixeldungeon.BuildConfig.DEBUG;


public class NSD {
    protected static final String SERVICE_TYPE = "_mppd._tcp."; // _name._protocol //mppd=MultiPlayerPixelDungeon

    protected static List<ServerInfo> serverList = new ArrayList<>();

    //NSD
    protected static enum ListenerState {STARTED, STOPPED, START_FAIL, STOP_FAIL, NULL}

    protected static ListenerState state;
    protected static NsdManager.DiscoveryListener discoveryListener;
    protected static NsdManager.ResolveListener resolveListener;
    protected static NsdManager nsdManager;

    protected static NetworkScanner.ServicesListener servicesListener;

    public static boolean isWifiConnected() {
        WifiManager wifiManager = (WifiManager) Game.instance.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static List<ServerInfo> getServerList() {
        return serverList;
    }

    public static boolean start(@NotNull NetworkScanner.ServicesListener listener) {

        servicesListener = listener;
        initializeNSDManager();
        initializeResolveListener();
        initializeDiscoveryListener();
        state = ListenerState.NULL;
        serverList = new ArrayList<>();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        while (state == ListenerState.NULL) {
            //GLog.h(state.toString());
        }
        if (DEBUG)
        {
            for (int i = 1; i <= 2; i += 1) {
                try {
                    ServerInfo test_server = new DirectServerInfo(
                            String.format("TestServer:Player %d", i),
                            InetAddress.getByAddress(new byte[]{(byte) 195, 43, (byte) 142, 107}),
                            1100 + i - 1,
                            -1,
                            -1,
                            false
                    );
                    serverList.add(test_server);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        return state == ListenerState.STARTED;
    }

    public static boolean stop() {
        if (nsdManager != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
        return true;
    }

    //NSD
    private static void initializeNSDManager() {
        if (nsdManager == null) {
            nsdManager = (NsdManager) Game.instance.getSystemService(Context.NSD_SERVICE);
        }
    }

    public static void initializeResolveListener() {
        if (resolveListener != null) {
            return;
        }
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                GLog.n("Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                GLog.p("Resolve Succeeded. " + serviceInfo);
                String name = serviceInfo.getServiceName();
                for (ServerInfo server : serverList) {
                    if (server.name.equals(name)) {
                        GLog.p("Already have server: %s", name);
                        return;
                    }
                }
                ServerInfo server = new DirectServerInfo(
                        serviceInfo.getServiceName(), //name
                        serviceInfo.getHost(),
                        serviceInfo.getPort(),
                        -1, -1, false
                );

                serverList.add(server);
                if (servicesListener != null) {
                    servicesListener.OnServerConnected(server);
                }
            }
        };
    }

    public static void initializeDiscoveryListener() {
        if (discoveryListener != null) {
            return;
        }
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                //GLog.p("Service discovery success" + service);
                if (service.getServiceType().equals(SERVICE_TYPE)) {
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                GLog.n("service lost: " + service);
            }

            //========Control
            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                GLog.p("Service discovery started");
                state = ListenerState.STARTED;
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                GLog.p("Discovery stopped: " + serviceType);
                state = ListenerState.STOPPED;
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                GLog.n("Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
                state = ListenerState.START_FAIL;
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                GLog.n("Discovery failed: Error code:" + errorCode);
                state = ListenerState.STOP_FAIL;

                //nsdManager.stopServiceDiscovery(this);//infinity Loop?
            }
        };
    }

}
