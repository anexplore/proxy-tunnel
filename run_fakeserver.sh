#!/bin/bash
DOCKER_MODE=$DOCKER_MODE
if [[ ""$JVM_ARGS == "" ]]; then
    JVM_ARGS="-Xmx512m "
fi

if [[ $DOCKER_MODE == "" && ""$PROS == "" ]]; then
    PROS="-DserverBindLocalAddress=0.0.0.0 -DserverBindLocalPort=8080"
    PROS=$PROS" -DsslEndPointHost=127.0.0.1 -DsslEndPointPort=8081"
    PROS=$PROS" -DkeyCertChainFile=certs/users/client.crt -DkeyFile=certs/users/client.pk8 -DkeyPassword=123456 -DtrustCertFile=certs/ca/ca.crt"
fi
java $JVM_ARGS $PROS -cp .:* com.fd.proxytunnel.Launcher "fakeserver"
