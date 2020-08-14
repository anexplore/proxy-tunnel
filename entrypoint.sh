#!/bin/bash
CMD=$1

case $CMD in 
    fakeserver)
        ./run_fakeserver.sh 
    ;;
    
    sslendpoint)
        ./run_sslendpoint.sh
    ;;
    *)
        exec $@
    ;;
esac
