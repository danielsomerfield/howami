.PHONY: integration build stop dependencies ps logs logsf

STAMP=`date +'%Y-%m-%d_%H%M%S'`
VERSION := 1.0-SNAPSHOT
COMPOSE := docker-compose -f docker-compose.yml

export VERSION

build:
	./gradlew buildAll

integration: build
	./gradlew integration

e2e: stop
	$(COMPOSE) -f docker-compose-e2e.yml up --force-recreate --build -d
	$(COMPOSE) -f docker-compose-e2e.yml logs -f > all.log &
	$(COMPOSE) -f docker-compose-e2e.yml logs -f e2e-tests > e2e-test.log
	bin/wait_for_tests.py

dependencies: stop
	$(COMPOSE) up --force-recreate --build -d

stop:
	$(COMPOSE) -f docker-compose-e2e.yml down

clean:
	./gradlew clean

ps:
	$(COMPOSE) -f docker-compose-e2e.yml ps

logs:
	$(COMPOSE) -f docker-compose-e2e.yml logs

logsf:
	$(COMPOSE) -f docker-compose-e2e.yml logs -f
