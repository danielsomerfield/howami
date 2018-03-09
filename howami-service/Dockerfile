FROM alpine:3.5

RUN apk update
RUN apk add openjdk8 bash

ARG version

ARG APP_NAME="howami-service-${version}"

ADD docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ADD build/distributions/${APP_NAME}.tar /
RUN ln -s $APP_NAME /howami-service
ADD howami-service.yml /howami-service/howami-service.yml

# TODO: remove this and the bash installation when kafka stuff is working
RUN wget http://apache.claz.org/kafka/1.0.1/kafka_2.11-1.0.1.tgz
RUN tar xvfz kafka_2.11-1.0.1.tgz


ENTRYPOINT ["/docker-entrypoint.sh"]
