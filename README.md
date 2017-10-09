## Victims Java Service

Victims Java Service is a microservice that provides Java hash information to the Victims REST project. 

### How does it work?

the `hash` endpoint is called passing some files to hash. This service uses the victims-lib-java project to generate a hash a jar file and files compressed within it. Those hashes are then returned to the caller as JSON.

### API Documentation

#### POST /hash 

```
curl -X POST "library=@src/test/resources/struts2-core-2.5.12.jar" http://localhost:8080/hash
```


#### GET /health

Checks the service is up and can access the database

```
[jshepher@localhost victims-api]$ curl -v http://localhost:8080/healthz

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
`java -jar java -jar target/victims-java-hash-1.0-SNAPSHOT-fat.jar`

### Running with Docker

Package the service as a Docker image using S2I:
`s2i build . redhat-openjdk-18/openjdk18-openshift victims-java`

Run the image passing MongoDB environment variables:
`docker run -d -p 8080:8080 victims-java`
