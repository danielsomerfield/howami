FROM alpine:3.5

RUN apk update
RUN apk add openjdk8

ARG version

ARG APP_NAME="howami-e2e-${version}"

ADD build/distributions/${APP_NAME}.tar /
RUN ln -s $APP_NAME /howami-e2e

ENTRYPOINT ["/howami-e2e/bin/howami-e2e"]
