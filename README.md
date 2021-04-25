# bubble

Create SMS and email based listservs.

## First time setup

### Clojure 
You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

### Docker + SQL

To reduce the risk of cross-project conflicts, this project uses docker and docker-compose to isolate any database dependencies. You'll need both docker and docker-compose installed to run the app.

Additionally, this project uses [dbmate](https://github.com/amacneil/dbmate) to manage database initialization and migrations. You'll need to have the `dbmate` binary command installed on your machine as well.

## Running

First, kick off postgresql and redis servers via docker-compose:

``` sh
docker-compose up -d postgres redis
```

To start a web server for the application, run:

``` sh
lein ring server
```
