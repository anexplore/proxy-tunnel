#!/bin/bash
DOCKER_MODE=$DOCKER_MODE
if [[ ""$JVM_ARGS == "" ]]; then
    JVM_ARGS="-Xmx512m "
fi

if [[ $DOCKER_MODE == "" && ""$PROS == "" ]]; then
    PROS="-DmappingFile=mapping.txt"
    PROS=$PROS" -DkeyCertChainFile=certs/users/client.crt -DkeyFile=certs/users/client.pk8 -DkeyPassword=123456 -DtrustCertFile=certs/ca/ca.crt"
fi
java $JVM_ARGS $PROS -cp .:* com.fd.proxytunnel.Launcher "fakeserver"
