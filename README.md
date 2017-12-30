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

## Operational
* Enable caching in circle CI
* Run smoke tests against production

## Security
* Disable confirmation after a time period
* Enable authorization on admin endpoints