#TODO: fix these
.PHONY: integration build stop dependencies ps logs logsf run-local

ifndef CONTAINER_VERSION
ifdef CIRCLE_BUILD_NUM
    VERSION := $(CIRCLE_BUILD_NUM)
    CONTAINER_VERSION := $(CIRCLE_BUILD_NUM)
else
    STAMP := $(shell date +'%s')
    VERSION := dev
    CONTAINER_VERSION := dev-$(STAMP)
endif
endif

REGISTRY_NAME := danielsomerfield
HOWAMI_SERVICE_NAME := howami-service
HOWAMI_COMMS_SERVICE_NAME := howami-comms-service
E2E_TESTS_NAME := howami-e2e

export VERSION

build:
	./gradlew buildAll

integration: build
	./gradlew integration

build-e2e-image:
	docker build --build-arg version=$(VERSION) --tag $(REGISTRY_NAME)/$(E2E_TESTS_NAME):$(CONTAINER_VERSION) $(E2E_TESTS_NAME)

e2e: build-e2e-image
	kubernetes/bin/run-smoke.py --version $(CONTAINER_VERSION)
	kubectl logs `kubectl get pods -l "app=smoke" --output=jsonpath={.items..metadata.name}` -f > smoke-test.log &
	bin/wait_for_tests.py

dependencies: stop
    #TODO: run just the dependencies
	echo NYI

run: stop build-images
	kubectl create -f kubernetes/howami.yml

run-local: run
	kubectl set image deployment/$(HOWAMI_SERVICE_NAME) $(HOWAMI_SERVICE_NAME)=$(REGISTRY_NAME)/$(HOWAMI_SERVICE_NAME):$(CONTAINER_VERSION)
	kubectl set image deployment/$(HOWAMI_COMMS_SERVICE_NAME) $(HOWAMI_COMMS_SERVICE_NAME)=$(REGISTRY_NAME)/$(HOWAMI_COMMS_SERVICE_NAME):$(CONTAINER_VERSION)

stop:
	kubectl delete deployments -l group=howami-all
	kubectl delete services -l group=howami-all
	kubectl delete jobs -l group=howami-all
	kubectl delete pods -l group=howami-all

clean:
	./gradlew clean

build-images: build
	docker build --build-arg version=$(VERSION) --tag $(REGISTRY_NAME)/$(HOWAMI_SERVICE_NAME):$(CONTAINER_VERSION) $(HOWAMI_SERVICE_NAME)
	docker build --build-arg version=$(VERSION) --tag $(REGISTRY_NAME)/$(HOWAMI_COMMS_SERVICE_NAME):$(CONTAINER_VERSION) $(HOWAMI_COMMS_SERVICE_NAME)

clean-images: stop
	docker ps -aq -f status=exited | xargs docker rm 2>&1 > /dev/null
	docker images -q danielsomerfield/howami-service | xargs docker rmi -f 2>&1 > /dev/null
	docker images -q danielsomerfield/howami-comms-service | xargs docker rmi -f  2>&1 > /dev/null
	docker images -q danielsomerfield/howami-e2e | xargs docker rmi -f  2>&1 > /dev/null
	docker images -q danielsomerfield/howami-docker-primary | xargs docker rmi -f 2>&1 > /dev/null
	docker images -f dangling=true -q | xargs docker rmi 2>&1 > /dev/null

push: build-images
	docker login -u $(DOCKER_USER) -p $(DOCKER_PASS)
	docker push $(REGISTRY_NAME)/howami-comms-service:$(CONTAINER_VERSION)
	docker push $(REGISTRY_NAME)/howami-service:$(CONTAINER_VERSION)
	docker push $(REGISTRY_NAME)/e2e-tests:$(CONTAINER_VERSION)