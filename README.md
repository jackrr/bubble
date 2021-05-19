# bubble

Create SMS and email based listservs.

## First time setup

### Clojure 
You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

### Secrets

There are a few secrets you'll need to get started. Create a .env file and add the necessary values to it. 

### Docker + SQL

To reduce the risk of cross-project conflicts, this project uses docker and docker-compose to isolate any database dependencies. You'll need both docker and docker-compose installed to run the app.

Additionally, this project uses [dbmate](https://github.com/amacneil/dbmate) to manage database initialization and migrations. You'll need to have the `dbmate` binary command installed on your machine as well.

## Running

First, kick off a postgresql server via docker-compose:

``` sh
docker-compose up -d postgres
```

To start a web server for the application, run:

``` sh
lein ring server
```

## Roadmap

### Pages

- Home page
  - TODO Edit name form
  - DONE See own bubbles
  - DONE Button to create a new bubble (link to new bubble page)
  - DONE Redirect to login if not logged in
  - DONE New bubble form
- Bubble page
  - TODO: DAVID -- Show add member (signup) link (to "Bubble join page")
  - TODO Unenroll
  - DONE List all members
  - (future) some kind of democratic management of membership (requires multi opt in to remove a member)
- DONE Login form to capture phone
- TODO: DAVID -- Bubble join page
  - "You've been invited to join _name_, enter your phone..."
  - Form for phone number and user name
    - Submitting form adds user as member

### Threading

- TODO: Jack -- Twilio phone number creation (ensure user has dedicated phone # for bubble membership)
  - Only buy new number when cannot achieve above w/ current capacity
- TODO: Jack -- Welcome message w/ name of bubble on join
- TODO: Jack -- Broadcast messages on inbound

# General
- Replace user namespace with member

