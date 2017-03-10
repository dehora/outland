FROM registry.opensource.zalan.do/stups/openjdk:8-54

VOLUME /outland/conf
RUN mkdir -p /outland

COPY ./outland-feature-server-0.0.2.jar /outland
COPY ./outland_service_start /outland

EXPOSE 8180
EXPOSE 8181

WORKDIR /outland

ENTRYPOINT ./outland_service_start


