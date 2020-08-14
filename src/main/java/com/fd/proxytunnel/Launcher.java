package com.fd.proxytunnel;

public class Launcher {
    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "fakeserver":
                    new FakeProxyServerLauncher().run();
                    break;
                case "sslendpoint":
                    new SslEndPointServerLauncher().run();
                    break;
                default:
                    throw new RuntimeException("unknown args, must be fakeserver or sslendpoint");
            }
        } else {
            new FakeProxyServerLauncher().run();
        }
    }
}
