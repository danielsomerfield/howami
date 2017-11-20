FROM alpine:3.5

RUN apk update
RUN apk add openjdk8

ARG version

ARG APP_NAME="howami-service-${version}"

ADD build/distributions/${APP_NAME}.tar /
RUN ln -s $APP_NAME /howami-service

ENTRYPOINT ["/howami-service/bin/howami-service"]
