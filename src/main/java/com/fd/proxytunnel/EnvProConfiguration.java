package com.fd.proxytunnel;

import com.fd.proxytunnel.mapping.MappingHolder;

public class EnvProConfiguration implements Configuration {

    public EnvProConfiguration() {
    }

    /**
     * Get Value for key from os system env or jvm properties, jvm priority is higher than os env
     * @param key key
     * @return value or {@code defaultValue} if pro is empty
     */
    public static String getFromPropertyOrEnv(String key, String defaultValue) {
        String pro = System.getProperty(key);
        if (isNullOrEmpty(pro)) {
            pro = System.getenv(key);
        }
        return isNullOrEmpty(pro) ? defaultValue : pro;
    }

    @Override
    public boolean openNettyLoggingHandler() {
        return Integer.parseInt(getFromPropertyOrEnv("openNettyLoggingHandler", "0")) == 1;
    }

    @Override
    public boolean channelAutoRead() {
        return false;
    }

    @Override
    public int connectionTimeoutToRemoteServer() {
        return Integer.parseInt(getFromPropertyOrEnv("connectionTimeoutToProxyServer", "10000"));
    }

    @Override
    public int mainEventGroupNumber() {
        return 1;
    }

    @Override
    public int workerEventGroupNumber() {
        return Integer.parseInt(getFromPropertyOrEnv("workerEventGroupNumber", "" + Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public int maxConnectionBacklog() {
        return Integer.parseInt(getFromPropertyOrEnv("maxConnectionBacklog", "1000"));
    }

    @Override
    public String tunnelMappingFilePath() {
        return getFromPropertyOrEnv("mappingFile", "mapping.txt");
    }

    @Override
    public MappingHolder mappingHolder() {
        // build each call
        try {
            return MappingHolder.buildMappingHolderFromFile(tunnelMappingFilePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int idleTimeoutForClient() {
        return Integer.parseInt(getFromPropertyOrEnv("idleTimeoutForClient", "60000"));
    }

    @Override
    public int idleTimeoutForRemoteServer() {
        return 0;
    }

    @Override
    public String sslProtocol() {
        return getFromPropertyOrEnv("sslProtocol", Constants.DEFAULT_SSL_PROTOCOL);
    }

    @Override
    public String keyCertChainFile() {
        return getFromPropertyOrEnv("keyCertChainFile", null);
    }

    @Override
    public String keyFile() {
        return getFromPropertyOrEnv("keyFile", null);
    }

    @Override
    public String keyPassword() {
        return getFromPropertyOrEnv("keyPassword", null);
    }

    @Override
    public String trustCertFile() {
        return getFromPropertyOrEnv("trustCertFile", null);
    }

    private static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
