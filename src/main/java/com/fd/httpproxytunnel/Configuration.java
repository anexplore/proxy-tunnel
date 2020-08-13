package com.fd.httpproxytunnel;

public interface Configuration {

    /**
     * @return true if add {@code LoggingHandler} to channel pipeline
     */
    boolean openNettyLoggingHandler();

    /**
     * @return true if set channel option AUTO_READ=true
     */
    boolean channelAutoRead();

    /**
     * @return ms, io timeout to proxy server
     */
    int timeoutToProxyServer();

    /**
     * @return ms, connect timeout to  proxy server
     */
    int connectionTimeoutToProxyServer();

    /**
     * @return main event group number to accept client request
     */
    int mainEventGroupNumber();

    /**
     * @return worker event group number
     */
    int workerEventGroupNumber();

    /**
     * @return max connect backlog for tcp connect
     */
    int maxConnectionBacklog();

    /**
     * @return ms, idle timeout for client when no read/write occurs
     */
    int idleTimeoutForClient();

    /**
     * @return server bind local address, eg: 0.0.0.0
     */
    String serverBindLocalAddress();

    /**
     * @return server bind local port, eg: 1080
     */
    int serverBindLocalPort();

    /**
     * @return proxy host
     */
    String proxyHost();

    /**
     * @return proxy port
     */
    int proxyPort();

    /**
     * @return ssl end point host
     */
    String sslEndPointHost();

    /**
     * @return ssl endpoint port
     */
    int sslEndPointPort();

    /**
     * @return key cert chain file
     */
    String keyCertChainFile();

    /**
     * @return key file
     */
    String keyFile();

    /**
     * @return key file password
     */
    String keyPassword();

    /**
     * @return trust cert file
     */
    String trustCertFile();
}
