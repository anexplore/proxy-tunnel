package com.fd.proxytunnel;

public interface Configuration {

    /**
     * @return true if add {@code LoggingHandler} to channel pipeline
     */
    boolean openNettyLoggingHandler();

    /**
     * @return true if set channel option AUTO_READ=true
     */
    @Deprecated
    boolean channelAutoRead();

    /**
     * @return ms, idle timeout for client when no read/write occurs
     */
    int idleTimeoutForClient();

    /**
     * @return ms, idle timeout for remote server when no read/write occurs
     */
    int idleTimeoutForRemoteServer();

    /**
     * @return ms, connect timeout to  proxy server
     */
    int connectionTimeoutToRemoteServer();

    /**
     * @return true if open tcp fast open else false
     */
    boolean openTcpFastOpen();

    /**
     * @return tcp_fastopen max pending queue size
     */
    int tcpFastOpenBacklog();

    /**
     * @return true if open tcp fast open connect else false
     */
    boolean openTcpFastOpenConnect();

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
     * @return ssl protocol version ,eg TLSv1.3 TLSv1.2
     */
    String sslProtocol();

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
