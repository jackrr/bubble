# bubble

Create SMS and email based listservs.

## First time setup

### Clojure 
You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

### Secrets

There are a few secrets you'll need to get started. Create a .env file and add the following to it:

DATABASE_URL=""
HOST=""
TWILIO_PHONE_NUMBER=""
TWILIO_ACCOUNT_SID=""
TWILIO_AUTH_TOKEN=""

(Values must come from a maintainer of a live deployment, or you'll need to create your own)

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
