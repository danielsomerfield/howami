.PHONY: integration build stop dependencies

STAMP=`date +'%Y-%m-%d_%H%M%S'`
VERSION := 1.0-SNAPSHOT
COMPOSE := docker-compose -f docker-compose.yml

export VERSION

build:
	./gradlew buildAll

integration: stop
	$(COMPOSE) -f docker-compose-integration.yml up --force-recreate --build -d
	$(COMPOSE) -f docker-compose-integration.yml logs -f integration-tests > integration-test.log
	bin/wait_for_tests.py

dependencies:
	$(COMPOSE) up --force-recreate --build -d

stop:
	$(COMPOSE) -f docker-compose-integration.yml down

clean:
	./gradlew clean