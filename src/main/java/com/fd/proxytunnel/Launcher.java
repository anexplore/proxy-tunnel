package com.fd.proxytunnel;

public class Launcher {
	public static final String FAKE_SERVER = "fakeserver";
	public static final String SSL_ENDPOINT = "sslendpoint";

    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
				case FAKE_SERVER:
                    new FakeProxyServerLauncher().run();
                    break;
				case SSL_ENDPOINT:
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
