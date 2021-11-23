package com.fd.proxytunnel;

import com.fd.proxytunnel.mapping.MappingHolder;

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
     * @return tunnel mapping file path
     */
    String tunnelMappingFilePath();

    /**
     * @return instance of {@link MappingHolder} which has each side's host and port
     */
    MappingHolder mappingHolder();

    /**
     * @return ssl protocol version ,eg TLSv1.3 TLSv1.2
     */
    String sslProtocol();

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
