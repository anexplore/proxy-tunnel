package com.fd.proxytunnel.mapping;

public class TunnelMap {
    public final Address localAddress;
    public final Address remoteAddress;

    public TunnelMap(Address localAddress, Address remoteAddress) {
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public String toString() {
        return "TunnelMap{" +
                "localAddress=" + localAddress +
                ", remoteAddress=" + remoteAddress +
                '}';
    }
}
