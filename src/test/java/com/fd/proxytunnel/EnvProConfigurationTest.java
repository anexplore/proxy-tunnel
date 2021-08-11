package com.fd.proxytunnel;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnvProConfigurationTest {

    @Test
    public void testGetFromEnvOrPro() {
        System.setProperty("PTEST", "1");
        assertEquals("get property from system properties",
                "1", EnvProConfiguration.getFromPropertyOrEnv("PTEST", null));
        assertNull("do not exist property must return null",
                EnvProConfiguration.getFromPropertyOrEnv("DONOT", null));
        assertEquals("do not exist must use default value",
                "1", EnvProConfiguration.getFromPropertyOrEnv("DONOT", "1"));
    }

    @Test
    public void testDefaultValues() {
        EnvProConfiguration config = new EnvProConfiguration();
        assertFalse("channel auto read should default to false", config.channelAutoRead());
        assertFalse("netty logging should default to false", config.openNettyLoggingHandler());
        assertFalse("channel auto read should be false", config.channelAutoRead());
    }
}
