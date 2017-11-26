FROM alpine:3.5

ARG version
ARG APP_NAME="howami-e2e-${version}"

RUN apk update
RUN apk add openjdk8
RUN apk add docker

ADD docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ADD build/distributions/${APP_NAME}.tar /
RUN ln -s $APP_NAME /howami-e2e
VOLUME ["/var/run/docker.sock"]

ENTRYPOINT ["/docker-entrypoint.sh"]
