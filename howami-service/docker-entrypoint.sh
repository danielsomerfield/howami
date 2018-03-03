#!/bin/sh

export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005

env

/howami-service/bin/howami-service server /howami-service/howami-service.yml