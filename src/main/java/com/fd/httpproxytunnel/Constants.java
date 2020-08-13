package com.fd.httpproxytunnel;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Constants
 */
public final class Constants {
    public static final String NETTY_LOGGING_HANDLER_NAME = "netty-logger";
    public static final String HTTP_REQUEST_ENCODER_NAME = "request-encoder";
    public static final String HTTP_REQUEST_DECODER_NAME = "request-decoder";
    public static final String HTTP_RESPONSE_ENCODER_NAME = "response-encoder";
    public static final String HTTP_RESPONSE_DECODER_NAME = "response-decoder";
    public static final String ENCODER_NAME = "encoder";
    public static final String DECODER_NAME = "decoder";
    public static final String SERVER_NAME = "server";
    public static final String MAIN_HANDLER = "main-handler";
    public static final String SSL_HANDLER = "ssl-handler";

    public static final LoggingHandler DEBUG_LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);

    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final int DEFAULT_HTTP_PORT = 80;

}
