## Victims Java Service

Victims Java Service is a microservice that provides Java hash information to the Victims REST project. 

### How does it work?

the `hash` endpoint is called passing some files to hash. This service uses the victims-lib-java project to generate a hash a jar file and files compressed within it. Those hashes are then returned to the caller as JSON.

### API Documentation

#### POST /hash 

```
$ curl -X POST -F "library2=@src/test/resources/camel-snakeyaml-2.17.4.jar" http://localhost:8080/hash

[ {
  "hash" : "3cfc3c06a141ba3a43c6c0a01567dbb7268839f75e6367ae0346bab90507ea09c9ecd829ecec3f030ed727c0beaa09da8c08835a8ddc27054a03f800fa049a0a",
  "name" : "camel-snakeyaml-2.17.4.jar",
  "format" : "SHA512",
  "files" : [ {
    "name" : "org/apache/camel/component/snakeyaml/SnakeYAMLDataFormat",
    "hash" : "cb1e80599bd7de814b63ad699849360b6c5d6dd33b7b7a2da6df753197eee137541c6bfde704c5ab8521e6b7dfb436d57f102f369fc0af36738668e4d1d0ff55"
  } ]
} ]
```


#### GET /health

Checks the service is up and can access the database

```
$ curl -v http://localhost:8080/healthz

> GET /healthz HTTP/1.1
> Host: localhost:8080
> Accept: */*
> 
< HTTP/1.1 200 OK
< 

```

### Building this service

Use maven to build the service:
`mvn clean package`

Run the Integration Tests:
`mvn clean verify`

Run a local service:
`java -jar target/victims-java-service-1.0-SNAPSHOT-fat.jar`

### Running with Docker

Package the service as a Docker image using S2I:
`s2i build . redhat-openjdk-18/openjdk18-openshift victims-java`

Run the image passing MongoDB environment variables:
`docker run -d -p 8080:8080 victims-java`
