#!/bin/sh

TEST_RESULTS=$(/howami-e2e/bin/howami-e2e)
TEST_RUN_RESULT=$?

docker kill -s SIGINT $(docker ps -qf "name=howami_mongo") $(docker ps -qf "name=howami_howami-service")

echo ================
echo "$TEST_RESULTS"
echo ================
echo test run result: $TEST_RUN_RESULT