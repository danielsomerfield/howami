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
## Cleanup
- replace links with user defined networks in compose
- remove dependency ordering so services don't count on it
- clean docker images

## Primary Use Cases
* Account Confirmation
    * Service that sends email and transitions queued -> sent (in process)
    * Disable confirmation after a time period* 

## Operational
* Enable caching in circle CI
* Run smoke tests against production

## Security
* Enable authorization on admin endpoints

## Testing
* Add "test" header to message format so that destructive external operations can be disabled
 per-message

## Performance
* Cached headers on resources
* Add indexes to all the searchable fields

## Reliability
* Retry / back-offs on cross-service operations
* Handle bad messages on the kafka streams
* Configure mongo replica sets and add a second instance