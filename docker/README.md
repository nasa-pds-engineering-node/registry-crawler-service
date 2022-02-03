# 🪐 Docker Image and Container for Registry Crawler Service

## 🏃 Steps to build the docker image of the Registry Crawler Service

#### 1. Update (if required) the following version in the `Dockerfile` with a compatible Registry Crawler Service version.

| Variable                        | Description |
| ------------------------------- | ------------|
| registry_crawler_service_version | The version of the Registry Crawler Service release to be included in the docker image|

```    
# Set the following argument with a compatible Registry Crawler Service version
ARG registry_crawler_service_version=1.0.0-SNAPSHOT
```

#### 2. Open a terminal and change the current working directory to `registry-crawler-service/docker`.

#### 3. Build the docker image as follows.

```
docker image build --tag nasapds/registry-crawler-service .
```

#### 4. As an optional step, push the docker image to a container image library.

For example, follow the below steps to push the newly built image to the Docker Hub.

* Execute the following command to log into the Docker Hub with a username and password (use the username and password of https://hub.docker.com/u/nasapds).
```
docker login
```
* Push the docker image to the Docker Hub.
```
docker image push nasapds/registry-crawler-service
```
* Visit the Docker Hub (https://hub.docker.com/u/nasapds) and make sure that the `nasapds/registry-crawler-service` image is available, so that it can be reused by other users without building it.


## 🏃 Steps to run a docker container of the Registry Crawler Service

#### 1. Update the Registry Crawler Service configuration file.

* Get a copy of the `harvest-client.cfg` file from https://github.com/NASA-PDS/registry-crawler-service/blob/main/src/main/resources/conf/crawler-server.cfg and
keep it in a local file location such as `/tmp/cfg/crawler-server.cfg`.
* Update the properties such as `rmq.host`, `rmq.user` and `rmq.password` to match with your deployment environment.

#### 3. Update the following environment variables in the `run.sh`.

| Variable                   | Description |
| -------------------------- | ----------- |
| CRAWLER_SERVICE_CONFIG_FILE | Absolute path of the Registry Crawler Service configuration file in the host machine (`E.g.: /tmp/cfg/crawler-server.cfg`) |
| HARVEST_DATA_DIR           | Absolute path of the Harvest data directory in the host machine (E.g.: `/tmp/registry-harvest-data`). If the Registry Harvest CLI is executed with the option to download test data, then this directory will be cleaned-up and populated with test data |

```    
# Update the following environment variables before executing this script

# Absolute path of the Registry Crawler Service configuration file in the host machine (E.g.: /tmp/cfg/crawler-server.cfg)
CRAWLER_SERVICE_CONFIG_FILE=/tmp/cfg/crawler-server.cfg

# Absolute path of the Harvest data directory in the host machine (E.g.: `/tmp/registry-harvest-data`).
# If the Registry Harvest CLI is executed with the option to download test data, then this directory will be
# cleaned-up and populated with test data. Make sure to have the same `HARVEST_DATA_DIR` value set in the
# environment variables of the Registry Harvest Service, Registry Crawler Service and Registry Harvest CLI.
# Also, this `HARVEST_DATA_DIR` location should be accessible from the docker containers of the Registry Harvest Service,
# Registry Crawler Service and Registry Harvest CLI.
HARVEST_DATA_DIR=/tmp/registry-harvest-data
```

Note:

Make sure to have the same `HARVEST_DATA_DIR` value set in the environment variables of the Registry Harvest Service,
Registry Crawler Service and Registry Harvest CLI. Also, this `HARVEST_DATA_DIR` location should be accessible from the
docker containers of the Registry Harvest Service, Registry Crawler Service and Registry Harvest CLI.


#### 4. Open a terminal and change the current working directory to `registry-crawler-service/docker`.

#### 5. If executing for the first time, change the execution permissions of `run.sh` file as follows.

```
chmod u+x run.sh
```

#### 6. Execute the `run.sh` as follows.

```
./run.sh
```

Above steps will run a docker container of the Registry Crawler Service.
