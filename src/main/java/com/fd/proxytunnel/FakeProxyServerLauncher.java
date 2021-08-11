package com.fd.proxytunnel;

public class FakeProxyServerLauncher {

    public void run() {
        FakeProxyServer server = new FakeProxyServer(new EnvProConfiguration());
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.startup();
    }

    public static void main(String[] args) {
        new FakeProxyServerLauncher().run();
    }
}
