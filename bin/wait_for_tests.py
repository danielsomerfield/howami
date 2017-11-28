#!/usr/bin/env python
import re
import datetime
import time
import sys

TIMEOUT_SECONDS = 30


def get_test_results(line):
    m = re.search("test run result: ([0-9]*)", line)
    if m:
        return int(m.group(1))
    else:
        return None


def run():
    start_time = datetime.datetime.now()
    log_file = open("./integration-test.log", "r")

    while (datetime.datetime.now() - start_time).seconds < TIMEOUT_SECONDS:
        file_location = log_file.tell()
        line = log_file.readline()
        if not line:
            time.sleep(1)
            log_file.seek(file_location)
        else:
            test_results = get_test_results(line)
            if test_results is not None:
                print("Test results: " + str(test_results))
                sys.exit(test_results)

    print("Timed out")
    sys.exit(1)


run()
