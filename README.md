# Crawler Web Service

Server application providing the functionality for crawling PDS4 products. 
It has to be used with other components, such as RabbitMQ message broker, Harvest Server and Harvest Client 
to enable performant ingestion of large data sets into PDS Registry.

## Build
This is a Java application. You need Java 11 JDK and Maven to build it.
To create a binary distribution (ZIP and TGZ archives) run the following maven command:

```
mvn package
``` 

## Documentation
Documentation is available in https://github.com/NASA-PDS/registry-harvest-service project.

