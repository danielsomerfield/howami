.PHONY: run build stop

STAMP=`date +'%Y-%m-%d_%H%M%S'`
VERSION := 1.0-SNAPSHOT

export VERSION

build:
	./gradlew buildAll

run: stop
	docker-compose up --force-recreate --build -d
	docker-compose logs -f integration-tests > integration-test.log
	bin/wait_for_tests.py
	cat .test_result
	docker-compose down

stop:
	docker-compose down

clean:
	./gradlew clean