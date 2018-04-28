
# How am I

A mobile application for keeping track of how you are doing at regular intervals throughout the day.

# Features

* Configurable per user:
    * How often to prompt?

# User journeys

## Basic MVP

1. User runs application for the first time and is prompted to register
2. User confirms email address
3. User sets notification preference
4. User is prompted regularly on scale of 1 - 5 how they are doing
5. User can pull up the results page that shows all entries


## Features

* Event annotation to allow user to put in notes about relevant things that have happened
* Graphs of results
    * By time of day
    * By day of the week
    * ... 
* "How is your life going?" - periodic question for comparison
* Delete my account
* Settings
    * Change cadance
* Offline sync

# TODO

## IAC
* Create VPN
* Create task
* Create a service for the task


## Path to production

### Main Workflow
* After unit / integration tests run, build containers and push tests and smoke tests to dockerhub
* Run expected version of service in kube on aws
* Run smoke test against it
    * Find public host of load balancer

### Issues
* Kafka consumers failing to reach 
* Push the version file separately from the main tar so it doesn't have to completely rebuild all docker layers


## Cleanup
- remove old images from dockerhub
- remove dependency ordering so services don't count on it


## Primary Use Cases
* Account Confirmation
    * Service that sends email and transitions queued -> sent (in process)
    * "Send a new confirmation code"
        * Old code is invalidated, expiration is refreshed
        * Confirmation sent again

## Operational
* Enable caching in circle CI
* Run smoke tests against production

## Security
* Enable authorization on admin endpoints

## Testing
* Add "test" header to message format so that destructive external operations can be disabled
 per-message
 * Clean up data after smoke test

## Performance
* Cache headers on resources
* Add indexes to all the searchable fields

## Reliability
* Retry / back-offs on cross-service operations
* Handle bad messages on the kafka streams (done?)
* Configure mongo replica sets and add a second instance
* Failure to connect to mongo should not kill service, it should continue to retry
* Kafka heartbeat: write and read heartbeats to make sure messages are getting to kafka

## Kubernetes runtime
- Change local deployment to dynamically build descriptor with python
- Print version on startup

## Known Issues
- If you don't clean before building, the generated startup script points to the wrong version