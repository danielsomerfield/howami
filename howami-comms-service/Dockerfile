FROM alpine:3.5

RUN apk update
RUN apk add openjdk8

ARG version

ARG APP_NAME="howami-comms-service-${version}"

ADD build/distributions/${APP_NAME}.tar /
RUN ln -s $APP_NAME /howami-comms-service
ADD howami-comms-service.yml /howami-comms-service/howami-comms-service.yml

ENTRYPOINT ["/howami-comms-service/bin/howami-comms-service", "server", "/howami-comms-service/howami-comms-service.yml"]
