#!/bin/sh

env


TEST_RESULTS=$(/howami-e2e/bin/howami-e2e)
TEST_RUN_RESULT=$?

echo ================
echo "$TEST_RESULTS"
echo ================
echo test run result: $TEST_RUN_RESULT