FROM openjdk:8
LABEL MAINTAINER anexplore@github.com

ADD run_fakeserver.sh  run_sslendpoint.sh entrypoint.sh logback.xml proxytunnel-jar-with-dependencies.jar /home/proxytunnel/
ADD certs /home/proxytunnel/certs/
WORKDIR /home/proxytunnel
RUN chmod 755 run_fakeserver.sh run_sslendpoint.sh entrypoint.sh
ENTRYPOINT ["./entrypoint.sh"]
CMD ["fakeserver"]
