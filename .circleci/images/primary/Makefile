.PHONY: build push

STAMP=`date +'%Y-%m-%d_%H%M%S'`
REGISTRY_USERNAME=danielsomerfield
IMAGE=${REGISTRY_USERNAME}/howami-docker-primary

all: build push

build:
	docker build --tag "${IMAGE}:latest" --tag "${IMAGE}:$STAMP" .

push:
	docker push ${IMAGE}