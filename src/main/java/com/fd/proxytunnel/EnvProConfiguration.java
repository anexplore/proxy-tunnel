package com.fd.proxytunnel;

public class EnvProConfiguration implements Configuration {

    public EnvProConfiguration() {
    }

    /**
     * Get Value for key from os system env or jvm properties, jvm priority is higher than os env
     * @param key key
     * @return value or {@code defaultValue} if pro is empty
     */
    public static String getFromEnvOrPro(String key, String defaultValue) {
        String pro = System.getProperty(key);
        if (isNullOrEmpty(pro)) {
            pro = System.getenv(key);
        }
        return isNullOrEmpty(pro) ? defaultValue : pro;
    }

    @Override
    public boolean openNettyLoggingHandler() {
        return Integer.parseInt(getFromEnvOrPro("openNettyLoggingHandler", "0")) == 1;
    }

    @Override
    public boolean channelAutoRead() {
        return Integer.parseInt(getFromEnvOrPro("channelAutoRead", "0")) == 1;
    }

    @Override
    public int timeoutToProxyServer() {
        return Integer.parseInt(getFromEnvOrPro("timeoutToProxyServer", "30000"));
    }

    @Override
    public int connectionTimeoutToProxyServer() {
        return Integer.parseInt(getFromEnvOrPro("connectionTimeoutToProxyServer", "10000"));
    }

    @Override
    public int mainEventGroupNumber() {
        return 1;
    }

    @Override
    public int workerEventGroupNumber() {
        return Integer.parseInt(getFromEnvOrPro("workerEventGroupNumber", "" + Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public int maxConnectionBacklog() {
        return Integer.parseInt(getFromEnvOrPro("maxConnectionBacklog", "1000"));
    }

    @Override
    public int idleTimeoutForClient() {
        return Integer.parseInt(getFromEnvOrPro("idleTimeoutForClient", "60000"));
    }

    @Override
    public String serverBindLocalAddress() {
        return getFromEnvOrPro("serverBindLocalAddress", "0.0.0.0");
    }

    @Override
    public int serverBindLocalPort() {
        return Integer.parseInt(getFromEnvOrPro("serverBindLocalPort", "80"));
    }

    @Override
    public String proxyHost() {
        return getFromEnvOrPro("proxyHost", null);
    }

    @Override
    public int proxyPort() {
        return Integer.parseInt(getFromEnvOrPro("proxyPort", "8080"));
    }

    @Override
    public String sslEndPointHost() {
        return getFromEnvOrPro("sslEndPointHost", null);
    }

    @Override
    public int sslEndPointPort() {
        return Integer.parseInt(getFromEnvOrPro("sslEndPointPort", "4443"));
    }

    @Override
    public String keyCertChainFile() {
        return getFromEnvOrPro("keyCertChainFile", null);
    }

    @Override
    public String keyFile() {
        return getFromEnvOrPro("keyFile", null);
    }

    @Override
    public String keyPassword() {
        return getFromEnvOrPro("keyPassword", null);
    }

    @Override
    public String trustCertFile() {
        return getFromEnvOrPro("trustCertFile", null);
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
