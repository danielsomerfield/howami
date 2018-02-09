.PHONY: integration build stop dependencies ps logs logsf

STAMP=`date +'%Y-%m-%d_%H%M%S'`
CIRCLE_BUILD_NUM ?= 1.0-dev
VERSION=$(CIRCLE_BUILD_NUM)
COMPOSE := docker-compose -f docker-compose.yml
REGISTRY_NAME=danielsomerfield

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

push:
	docker login -u $DOCKER_USER -p $DOCKER_PASS
	docker push $(REGISTRY_NAME)/howami-comms-service:$(VERSION)
	docker push $(REGISTRY_NAME)/howami-service:$(VERSION)
	docker push $(REGISTRY_NAME)/e2e-tests:$(VERSION)