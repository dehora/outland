FROM registry.opensource.zalan.do/stups/openjdk:8-54

RUN mkdir -p /outland

COPY ./outland-feature-server-0.0.8.jar /outland
COPY ./configuration-docker.yaml /outland

EXPOSE 8180
EXPOSE 8181

WORKDIR /outland

#
# using shell mode to allow env subsitution
#
CMD ["sh", "-c", "java -jar $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom outland-feature-server-*.jar server configuration-docker.yaml"]


