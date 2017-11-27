.PHONY: run build stop

STAMP=`date +'%Y-%m-%d_%H%M%S'`
VERSION := 1.0-SNAPSHOT

export VERSION

build:
	./gradlew buildAll

run: stop
	docker-compose up --force-recreate --build | tee .test_result
	cat .test_result | grep "test run result: 0" #if the tests fail, so will the process

stop:
	docker-compose down

clean:
	./gradlew clean