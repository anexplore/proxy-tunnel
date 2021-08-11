#!/bin/bash
CMD=$1

case $CMD in 
    fakeserver)
      exec  ./run_fakeserver.sh
    ;;
    
    sslendpoint)
      exec ./run_sslendpoint.sh
    ;;
    *)
        exec $@
    ;;
esac
