package com.watabou.pixeldungeon.network.scanners;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.ArrayList;
import java.util.List;


public class NSD implements ServiceDiscovery {
    protected static final String SERVICE_TYPE = "_mppd._tcp."; // _name._protocol //mppd=MultiPlayerPixelDungeon
    protected static final int MAX_WAIT_TIME = 3000; // ms
    protected static final int SLEEP_TIME = 100; // ms
    protected List<ServerInfo> serverList = new ArrayList<>();

    //NSD
    protected static enum ListenerState {STARTED, STOPPED, START_FAIL, STOP_FAIL, NULL}

    protected ListenerState state;
    protected NsdManager.DiscoveryListener discoveryListener;
    protected NsdManager nsdManager;

    protected static ServiceDiscoveryListener servicesListener;

    public static boolean isWifiConnected() {
        WifiManager wifiManager = (WifiManager) Game.instance.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public List<ServerInfo> getServerList() {
        return serverList;
    }

    @Override
    public boolean startDiscovery(ServiceDiscoveryListener listener) {

        state = ListenerState.NULL;
        servicesListener = listener;
        initializeNSDManager();
        initializeDiscoveryListener();
        serverList = new ArrayList<>();
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        int sleep_time = MAX_WAIT_TIME;
        while ((state == ListenerState.NULL) && (sleep_time > 0)) {
            try {
                //noinspection BusyWait
                Thread.sleep(SLEEP_TIME);
                sleep_time -= SLEEP_TIME;
            } catch (InterruptedException e) {
                break;
            }
        }
        return state == ListenerState.STARTED;
    }

    @Override
    public boolean stopDiscovery() {
        if (nsdManager != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
        }
        return true;
    }

    //NSD
    private void initializeNSDManager() {
        if (nsdManager == null) {
            nsdManager = (NsdManager) Game.instance.getSystemService(Context.NSD_SERVICE);
        }
    }

    public NsdManager.ResolveListener createResolveListener() {
        NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {

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
                    servicesListener.onServiceFound(server);
                }
            }
        };
        return resolveListener;
    }

    public void initializeDiscoveryListener() {
        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                //GLog.p("Service discovery success" + service);
                if (service.getServiceType().equals(SERVICE_TYPE)) {
                    nsdManager.resolveService(service, createResolveListener());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                GLog.n("service lost: " + service);
                //todo add server deleting
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
