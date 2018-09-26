# flux-java

> This library is under development and no stable version has been released yet.  
> The API can change at any moment.

[![Build Status](https://travis-ci.org/bonitoo-io/flux-java.svg?branch=master)](https://travis-ci.org/bonitoo-io/flux-java)
[![codecov](https://codecov.io/gh/bonitoo-io/flux-java/branch/master/graph/badge.svg)](https://codecov.io/gh/bonitoo-io/flux-java)
[![License](https://img.shields.io/github/license/bonitoo-io/flux-java.svg)](https://github.com/bonitoo-io/flux-java/blob/master/LICENSE)
[![Snapshot Version](https://img.shields.io/nexus/s/https/apitea.com/nexus/io.bonitoo.flux/flux-java.svg)](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/)
[![GitHub issues](https://img.shields.io/github/issues-raw/bonitoo-io/flux-java.svg)](https://github.com/bonitoo-io/flux-java/issues)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/bonitoo-io/flux-java.svg)](https://github.com/bonitoo-io/flux-java/pulls)

The Java client for:

* [InfluxData Platform](#influxdata-platform-client)
* [Flux](#flux---data-scripting-language)

## InfluxData Platform
The Java client for the **InfluxData Platform for Time Series** that implement HTTP API defined by [Influx API Service](https://github.com/influxdata/platform/blob/master/http/swagger.yml).

### Factory

The `PlatformClientFactory` creates the instance of a Platform client and can be configured by `PlatformOptions`.

#### Platform configuration
- `url` -  the url to connect to Platform
- `okHttpClient` - the HTTP client to use for communication with Platform (optional)

```java
// Connection configuration
PlatformOptions options = PlatformOptions.builder()
    .url("http://localhost:9999")
    .build();

// Platform client
PlatformClient platformClient = PlatformOptions.connect(options);

...

platformClient.close();
```

### Health

Get the health of an Platform instance.
```java
Health health = platformClient.health();

boolean healthy = health.isHealthy();
```

### Users

The `UserClient` supports:
1. creating users
2. find users
3. update user
4. delete user

```java
UserClient userClient = platformClient.createUserClient();

// Creates a new user with name 'John Ryzen'
User user = userClient.createUser("John Ryzen");

// Update a user
user.setName("Tom Push");
userClient.updateUser(user);

// Delete a user
User createdUser = ...;
userClient.deleteUser(createdUser);

// Retrieve a user by ID
User user = userClient.findUserByID("00");

// List all users
List<User> users = userClient.findUsers();
```

### Organizations

The `OrganizationClient` supports:
1. creating organizations
2. find organizations
3. update organization
4. delete organization

```java
OrganizationClient organizationClient = platformClient.createOrganizationClient();

// Creates a new organization with name 'TechnologiesRT'
Organization organization = organizationClient.createOrganization("TechnologiesRT");

// Update a organization
organization.setName("THT Dia");
organizationClient.updateOrganization(organization);

// Delete a organization
Organization createdOrganization = ...;
organizationClient.deleteOrganization(createdOrganization);

// Retrieve a organization by ID
Organization organization = organizationClient.findOrganizationByID("00");

// List all organizations
List<Organization> organizations = organizationClient.findOrganizations();
```

### Buckets

The `BucketClient` supports:
1. creating buckets
2. find buckets
3. update bucket
4. delete bucket

```java
BucketClient bucketClient = platformClient.createBucketClient();

// Creates a new bucket with name 'robot-sensors' and retention one hour
Bucket bucket = bucketClient.createBucket("robot-sensors", "1h", organization);

// Update a bucket
bucket.setName("robots-sensors-speed");
bucket.setRetentionPeriod("2h30m");
bucketClient.updateBucket(bucket);

// Delete a bucket
Bucket createdBucket = ...;
bucketClient.deleteBucket(createdBucket);

// Retrieve a bucket by ID
Bucket bucket = bucketClient.findBucketByID("00");

// Retrieve a buckets by Organization
List<Bucket> buckets = bucketClient.findBucketsByOrganization(organization);

// List all buckets
List<Bucket> buckets = bucketClient.findBuckets();
```

### Authorization

The `AuthorizationClient` supports:
1. creating authorization
2. find authorizations
3. update authorization
4. delete authorization

```java
AuthorizationClient authorizationClient = platformClient.createAuthorizationClient();

// Create a new authorization to create and update organizations, users
User user = ...;

Permission readUsers = new Permission();
readUsers.setAction(Permission.READ_ACTION);
readUsers.setResource(Permission.USER_RESOURCE);

Permission writeOrganizations = new Permission();
writeOrganizations.setAction(Permission.WRITE_ACTION);
writeOrganizations.setResource(Permission.ORGANIZATION_RESOURCE);

List<Permission> permissions = new ArrayList<>();
permissions.add(readUsers);
permissions.add(writeOrganizations);

Authorization authorization = authorizationClient.createAuthorization(user, permissions);

// Update Authorization status
Authorization authorization = ...;
authorization.setStatus(Status.INACTIVE);
authorization = authorizationClient.updateAuthorizationStatus(authorization);

// Delete a Authorization
Authorization createdAuthorization = ...;
authorizationClient.deleteAuthorization(createdAuthorization);

// Find Authorization by User
List<Authorization> authorizations = authorizationClient.findAuthorizationsByUser(user);
```

### Source
The `SourceClient` supports:
1. creating source
2. find sources
3. update source
4. delete source
5. find sources buckets
6. check sources health

```java
SourceClient sourceClient = platformClient.createSourceClient();

// Create a new source for local InfluxDB
Source source = new Source();

source.setOrganizationID("00");
source.setDefaultSource(false);
source.setName(generateName("Source"));
source.setType(Source.SourceType.V1SourceType);
source.setUrl("http://localhost:8086");
source.setInsecureSkipVerify(true);
source.setTelegraf("telegraf");
source.setToken(UUID.randomUUID().toString());
source.setUsername("admin");
source.setPassword("password");
source.setSharedSecret(UUID.randomUUID().toString());
source.setMetaUrl("/usr/local/var/influxdb/meta");
source.setDefaultRP("autogen");

source = sourceClient.createSource(source);

// Update Source
source.setInsecureSkipVerify(false);
source.setFluxURL("http://localhost:8082");
sourceClient.updateSource(bucket);

// Delete Source
Source createdSource = ...;
sourceClient.deleteSource(createdSource);

// Find Sources
List<Source> sources = sourceClient.findSources();

// Find Sources Buckets
List<Bucket> buckets = sourceClient.findBucketsBySource(source);

// Check Sources Health
Health health = sourceClient.health(source);
boolean healthy = health.isHealthy();
```

### Tasks

The `TaskClient` supports:
1. [creating tasks](#create)
2. find tasks
3. update task
4. delete task

```java
TaskClient taskClient = platformClient.createTaskClient();
```

#### Create
The task can be created with `cron` or `every` expression that specify task repetition. 

The required `Task` attributes are:
- `name` - the description of the task
- `flux` - the Flux script to run
- `userID` - the ID of the user that owns this Task
- `organizationID` - the ID of the organization that owns this Task

##### Cron
```java
String flux = "from(bucket: \"telegraf\") |> last()";

Task task = taskClient.createTaskCron("task name", flux, "0 2 * * *", "01", "01");
...
```

##### Every
```java
String flux = "from(bucket: \"telegraf\") |> last()";

Task task = taskClient.createTaskEvery("task name", flux, "0 2 * * *", "10m", "01");
...
```

### Write

The `WriteClient` supports:
1. writing data points in [InfluxDB Line Protocol](https://bit.ly/2QL99fu)
2. use batching for writes
3. use client backpressure strategy
4. produces events that allow user to be notified and react to this events
    - `WriteSuccessEvent` - published when arrived the success response from Platform server
    - `BackpressureEvent` - published when is **client** backpressure applied
    - `UnhandledErrorEvent` - published when occurs a unhandled exception
5. use GZIP compression for data

#### Writing data

```java
WriteClient writeClient = platformClient.createWriteClient();

String record = "h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1";
writeClient.write("b1", "org1", "token1", record);

writeClient.close();
```

#### Batching
The writes are processed in batches which are configurable by `WriteOptions`.

- `batchSize` - the number of data point to collect in batch
- `flushInterval` - the number of milliseconds before the batch is written 
- `jitterInterval` - the number of milliseconds to increase the batch flush interval by a random amount (see documentation above)
- `retryInterval` - the number of milliseconds to retry unsuccessful write
- `bufferLimit` - the maximum number of unwritten stored points
- `writeScheduler` - the scheduler which is used for write data points (by overriding default settings can be disabled batching)
- `backpressureStrategy` - the strategy to deal with buffer overflow

```java
WriteOptions writeOptions = WriteOptions.builder()
    .batchSize(5_000)
    .flushInterval(10_000)
    .jitterInterval(5_000)
    .retryInterval(5_000)
    .bufferLimit(100_000)
    .backpressureStrategy(BackpressureOverflowStrategy.ERROR)
    .build();

// Write client
WriteClient writeClient = platformClient.createWriteClient(writeOptions);

...

writeClient.close();
```

#### Backpressure
The backpressure presents the problem of what to do with a growing backlog of unconsumed data points. 
The key feature of backpressure is to provide the capability to avoid consuming the unexpected amount of system resources.  
This situation is not common and can be caused by several problems: generating too much measurements in short interval,
long term unavailability of the InfluxDB server, network issues. 

The size of backlog is configured by 
`WriteOptions.bufferLimit` and backpressure strategy by `WriteOptions.backpressureStrategy`.

##### Strategy how react to backlog overflows
- `DROP_OLDEST` - Drop the oldest data points from the backlog 
- `DROP_LATEST` - Drop the latest data points from the backlog  
- `ERROR` - Signal a exception
- `BLOCK` - (not implemented yet) Wait specified time for space in buffer to become available
  - `timeout` - how long to wait before giving up
  - `unit` - TimeUnit of the timeout

If is used the strategy `DROP_OLDEST` or `DROP_LATEST` there is a possibility to react on backpressure event and slowdown the producing new measurements:

```java
WriteClient writeClient = platformClient.createWriteClient(writeOptions);
writeClient.listenEvents(BackpressureEvent.class).subscribe(event -> {
    
    // slowdown producers
    ...
});
```

#### Handle the Events

##### Handle the Success write
```java
WriteClient writeClient = platformClient.createWriteClient();
writeClient.listenEvents(WriteSuccessEvent.class).subscribe(event -> {

    String data = event.getLineProtocol();

    // handle success
    ...
});
```
##### Handle the Error Write
```java
WriteClient writeClient = platformClient.createWriteClient();
writeClient.listenEvents(UnhandledErrorEvent.class).subscribe(event -> {
            
    Throwable exception = event.getThrowable();

    // handle error
    ...
});
```

#### Gzip's support
`WriteClient` doesn't enable gzip compress for http request body by default. If you want to enable gzip to reduce transfer data's size, you can call:
```java
WriteClient writeClient = platformClient.createWriteClient();
writeClient.enableGzip();
```

## Flux - Data Scripting Language
Java client for the Flux. The [Flux](https://github.com/influxdata/platform/tree/master/query#flux---influx-data-language) is centered on querying and manipulating time series data.

### Factory

The `FluxClientFactory` creates the instance of a Flux client and can be configured by `FluxConnectionOptions`.

#### Flux configuration
- `url` -  the url to connect to Flux
- `orgID` - the organization id required by Flux 
- `okHttpClient` - the HTTP client to use for communication with Flux (optional)

```java
// Connection configuration
FluxConnectionOptions options = FluxConnectionOptions.builder()
    .url("http://localhost:8093")
    .orgID("0")
    .build();

// Flux client
FluxClient fluxClient = FluxClientFactory.connect(options);

...

fluxClient.close();
```

### Queries
There are two possibilities how to create Flux query:

1. Directly write Flux query
2. Use build-in operators

#### Flux query

```java
String query = "from(bucket:\"telegraf\") |> filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

List<FluxTable> tables = fluxClient.flux(query);
```

#### Build-in operators

For all supported operators have a look at [Operators](OPERATORS.md) and for instructions how to write own operator have a look at [Custom operator](OPERATORS.md#custom-operator).

```java
Flux query = Flux
    .from("telegraf")
    .groupBy("_measurement")
    .difference();

List<FluxTable> tables = fluxClient.flux(query);
```

#### Asynchronous query

Execute a Flux query against the Flux service and asynchronous stream `FluxRecord`s to `callback`.

```java

String query = "from(bucket:\"telegraf\") |> range(start: -30m) |> group(by: [\"tag_a\", \"tag_b\"])";

fluxClient.flux(query, (cancellable, fluxRecord) -> {

    // FluxRecord
    logFluxRecord(fluxRecord);
});
```

##### OnComplete notification
The callback to consume a notification about successfully end of stream.

```java
fluxClient.flux(query, 
    (cancellable, fluxRecord) -> logFluxRecord(fluxRecord), 
    () -> {
        // callback to consume a completion notification 
        System.out.println("End of response");   
    });
```

##### OnError notification  
The callback to consume any error notification.                   
                   
```java
fluxClient.flux(query, 
    fluxRecord -> logFluxRecord(fluxRecord), 
    canceled -> {},
    throwable -> {
        logError(throwable);
    });
```

##### Cancel the query
The `Cancellable` object has the cancel method to stop asynchronous query.

```java
fluxClient.flux(query, (cancellable, fluxRecord) -> {
                  
    // found what I'm looking for ?
    if (foundRequest(fluxRecord)) {
      // yes => cancel query
      cancellable.cancel();
    }
    
    // no => process next result
    processResult(fluxRecord);
});
```

#### Handling server response

There are two possibilities how to handle server response:
1. Mapping to the `FluxTable` POJO ([mentioned above](#flux-query))
2. Use directly server response to the custom handling

##### Custom Handling  
```java
Response<ResponseBody> result = fluxClient.fluxRaw(query);
```


#### Query configuration

The Flux query can be configured by `FluxOptions`:

- `dialect` - the dialect is an object defining the options to use by Flux server when encoding the response.
    - `header` - Header is a boolean value, if true the header row is included, otherwise its is omitted. Defaults to `true`.
    - `delimiter` - Delimiter is a character to use as the delimiting value between columns. Defaults to `,`.
    - `quoteChar` - QuoteChar is a character to use to quote values containing the delimiter. Defaults to `"`.
    - `annotations` - Annotations is a list of annotations that should be encoded. If the list is empty the annotation column is omitted entirely.
    - `commentPrefix` -  CommentPrefix is a string prefix to add to comment rows. Defaults to `#`. Annotations are always comment rows.
- `queryOptions` - the options specify a context in which a Flux query is to be run. Currently supported options are `NowOption`, `TaskOption`, `LocationOption` and `CustomOption`.
    
```java
TaskOption task = TaskOption.builder("foo")
    .every(1L, ChronoUnit.HOURS)
    .delay(10L, ChronoUnit.MINUTES)
    .cron("0 2 * * *")
    .retry(5)
    .build();

FluxOptions options = FluxOptions.builder()
    .addOption(task)
    .build();

FluxResult results = fluxClient.flux(Flux.from("telegraf"), options);
```

### Events
The Flux client produces events that allow user to be notified and react to this events:

- `FluxSuccessEvent` - published when arrived the success response from Flux server
- `FluxErrorEvent` - published when arrived the error response from Flux server
- `UnhandledErrorEvent` -  published when occurs a unhandled exception

#### Handling success response by events

```java
FluxClient fluxClient = FluxClientFactory.connect(options);
fluxClient.subscribeEvents(FluxSuccessEvent.class, event -> {

    // handle success
    
    String query = event.getFluxQuery();
    ...
});
```

#### Handling error response by events

```java
FluxClient fluxClient = FluxClientFactory.connect(options);
fluxClient.subscribeEvents(FluxErrorEvent.class, event -> {
    
    // handle error
    
    InfluxDBException influxDBException = event.getException();
    ...
});
```
### Advanced Usage

#### Gzip's support

> Currently unsupported by `flux` server.

 
flux-java client doesn't enable gzip compress for http request body by default. If you want to enable gzip to reduce transfer data's size , you can call:

```java
fluxClient.enableGzip();
```

#### Log HTTP Request and Response
The Requests and Responses can be logged by changing OkHttp LogLevel.
```java
fluxClient.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);
```

#### Check the status of Flux instance
The Flux HTTP API [/ping](https://github.com/influxdata/platform/blob/master/http/swagger.yml) endpoint provides ability 
to check the status of your Flux instance:

```java
boolean running = fluxClient.ping()
System.out.println("Flux service running: " + runnning);
```



## Version

The latest version for Maven dependency:
```xml
<dependency>
  <groupId>io.bonitoo.flux</groupId>
  <artifactId>flux-java</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
  
Or when using with Gradle:
```groovy
dependencies {
    compile "io.bonitoo.flux:flux-java:1.0.0-SNAPSHOT"
}
```

### Snapshot repository
The snapshot repository is temporally located [here](https://apitea.com/nexus/content/repositories/bonitoo-snapshot/).

#### Maven
```xml
<repository>
    <id>bonitoo-snapshot</id>
    <name>Bonitoo.io snapshot repository</name>
    <url>https://apitea.com/nexus/content/repositories/bonitoo-snapshot/</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
#### Gradle
```
repositories {

    maven { url "https://apitea.com/nexus/content/repositories/bonitoo-snapshot" }
}
```

### Build Requirements

* Java 1.8+ (tested with jdk8)
* Maven 3.0+ (tested with maven 3.5.0)
* Docker daemon running

Then you can build flux-java with all tests with:

```bash
$ mvn clean install
```

If you don't have Docker running locally, you can skip tests with -DskipTests flag set to true:

```bash
$ mvn clean install -DskipTests=true
```

If you have Docker running, but it is not at localhost (e.g. you are on a Mac and using `docker-machine`) you can set an optional environments to point to the correct IP addresses and ports:

- `INFLUXDB_IP`
- `INFLUXDB_PORT_API`
- `FLUX_IP`
- `FLUX_PORT_API`

```bash
$ export INFLUXDB_IP=192.168.99.100
$ mvn test
```

For convenience we provide a small shell script which starts a InfluxDB and Flux server inside Docker containers and executes `mvn clean install` with all tests locally.

```bash
$ ./compile-and-test.sh
```

## Developer

Add licence to files: `mvn license:format`.