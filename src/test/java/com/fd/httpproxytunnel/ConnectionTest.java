package com.fd.httpproxytunnel;

import com.fd.proxytunnel.Configuration;
import com.fd.proxytunnel.ConnectionFromClient;
import com.fd.proxytunnel.ConnectionToProxy;
import com.fd.proxytunnel.EnvProConfiguration;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThrows;

public class ConnectionTest {

    @Test
    public void testConnectionFromClientNullParams() {
        Configuration config = new EnvProConfiguration();
        Channel channel = new NioSocketChannel();
        List<Object> pendingMessage = new LinkedList<>();
        assertThrows("channel null should throw null pointer exception",
                NullPointerException.class, ()-> new ConnectionFromClient(null, pendingMessage, config));
        assertThrows("pending message null should throw null pointer exception",
                NullPointerException.class, ()-> new ConnectionFromClient(channel, null, config));
        assertThrows("configuration null should throw null pointer exception",
                NullPointerException.class, ()-> new ConnectionFromClient(channel, pendingMessage, null));
    }

    @Test
    public void testConnectionToProxyNullParam() {
        Configuration config = new EnvProConfiguration();
        Channel channel = new NioSocketChannel();
        List<Object> pendingMessage = new LinkedList<>();
        ConnectionFromClient cc = new ConnectionFromClient(channel, pendingMessage, config);
        assertThrows("connection from client null should throw null pointer exception",
                NullPointerException.class, ()-> new ConnectionToProxy(null, config));
        assertThrows("configuration null should throw null pointer exception",
                NullPointerException.class, ()-> new ConnectionToProxy(cc, null));

    }
}
