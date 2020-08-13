package com.fd.httpproxytunnel;

public class SslEndPointServerLauncher {
    public void run() {
        SslEndPointServer server = new SslEndPointServer(new EnvProConfiguration());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.shutdown();
            }
        });
        server.startup();
    }

    public static void main(String[] args) {
        new SslEndPointServerLauncher().run();
    }
}
