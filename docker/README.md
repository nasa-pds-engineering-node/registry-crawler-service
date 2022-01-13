# 🪐 Docker Image and Container for Big Data Crawler Server

## 🏃 Steps to build the docker image of the Big Data Crawler Server

#### 1. Update (if required) the following version in the `Dockerfile` with a compatible Big Data Crawler Server version.

| Variable                        | Description |
| ------------------------------- | ------------|
| big_data_crawler_server_version | The version of the Big Data Crawler Server release to be included in the docker image|

```    
# Set the following argument with a compatible Big Data Crawler Server version
ARG big_data_crawler_server_version=1.0.0-SNAPSHOT
```

#### 2. Open a terminal and change the current working directory to `big-data-crawler-server/docker`.

#### 3. Build the docker image as follows.

```
docker image build --tag nasapds/big-data-crawler-server .
```

## 🏃 Steps to run a docker container of the Big Data Crawler Server

#### 1. Update the Big Data Crawler Server configuration file.

* Get a copy of the `harvest-client.cfg` file from https://github.com/NASA-PDS/big-data-crawler-server/blob/main/src/main/resources/conf/crawler-server.cfg and
keep it in a local file location such as `/tmp/cfg/crawler-server.cfg`.
* Update the properties such as `rmq.host`, `rmq.user` and `rmq.password` to match with your deployment environment.

#### 3. Update the following environment variables in the `run.sh`.

| Variable                   | Description |
| -------------------------- | ----------- |
| CRAWLER_SERVER_CONFIG_FILE | # Absolute path for the Big Data Crawler Server configuration file in the host machine (`E.g.: /tmp/cfg/crawler-server.cfg`) |
| HARVEST_DATA_DIR           | Absolute path for the Harvest data directory in the host machine (E.g.: `/tmp/data/urn-nasa-pds-insight_rad`) |

```    
# Update the following environment variables before executing this script

# Absolute path for the Big Data Crawler Server configuration file in the host machine (E.g.: /tmp/cfg/crawler-server.cfg)
CRAWLER_SERVER_CONFIG_FILE=/tmp/cfg/crawler-server.cfg

# Absolute path for the Harvest data directory in the host machine (E.g.: /tmp/data/urn-nasa-pds-insight_rad)
HARVEST_DATA_DIR=/tmp/data
```

#### 4. Open a terminal and change the current working directory to `big-data-crawler-server/docker`.

#### 5. If executing for the first time, change the execution permissions of `run.sh` file as follows.

```
chmod u+x run.sh
```

#### 6. Execute the `run.sh` as follows.

```
./run.sh
```

Above steps will run a docker container of the Big Data Crawler Server.
