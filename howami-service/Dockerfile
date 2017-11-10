FROM alpine:3.5

RUN apk update
RUN apk add openjdk8

ARG APP_NAME="howami-service-${VERSION}"

ADD build/distributions/${APP_NAME}.tar /
RUN tar xvf $APP_NAME.tar
RUN ln -s $APP_NAME /howami-service

ENTRYPOINT ["/howami-service/bin/howami-service"]
