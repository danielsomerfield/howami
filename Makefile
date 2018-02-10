.PHONY: integration build stop dependencies ps logs logsf

STAMP=`date +'%Y-%m-%d_%H%M%S'`
CIRCLE_BUILD_NUM ?= dev
VERSION=1.0-$(CIRCLE_BUILD_NUM)
COMPOSE_DEPENDENCIES := docker-compose -f docker-compose-dependencies.yml
COMPOSE_SERVICES := $(COMPOSE_DEPENDENCIES) -f docker-compose-services.yml
COMPOSE_E2E := $(COMPOSE_SERVICES) -f docker-compose-e2e.yml

REGISTRY_NAME=danielsomerfield

export VERSION

build:
	./gradlew buildAll

integration: build
	./gradlew integration

e2e: stop
	$(COMPOSE_E2E) up --force-recreate --build -d
	$(COMPOSE_E2E) logs -f > all.log &
	$(COMPOSE_E2E) logs -f e2e-tests > e2e-test.log
	bin/wait_for_tests.py

dependencies: stop
	$(COMPOSE_E2E) up --force-recreate --build -d

stop:
	$(COMPOSE_E2E) -f docker-compose-e2e.yml down

clean:
	./gradlew clean

ps:
	$(COMPOSE_E2E) ps

logs:
	$(COMPOSE_E2E) logs

logsf:
	$(COMPOSE_E2E) logs -f

push:
	docker login -u $(DOCKER_USER) -p $(DOCKER_PASS)
	docker push $(REGISTRY_NAME)/howami-comms-service:$(VERSION)
	docker push $(REGISTRY_NAME)/howami-service:$(VERSION)
	docker push $(REGISTRY_NAME)/e2e-tests:$(VERSION)

