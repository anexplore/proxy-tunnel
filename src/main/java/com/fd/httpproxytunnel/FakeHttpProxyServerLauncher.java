package com.fd.httpproxytunnel;

public class FakeHttpProxyServerLauncher {

    public void run() {
        FakeHttpProxyServer server = new FakeHttpProxyServer(new EnvProConfiguration());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.shutdown();
            }
        });
        server.startup();
    }

    public static void main(String[] args) {
        new FakeHttpProxyServerLauncher().run();
    }
}
