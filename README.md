# Cloud Foundry Java Client

[![Maven Central](https://img.shields.io/maven-central/v/io.pivotal/pivotal-cloudfoundry-client/3.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aio.pivotal%20AND%20a%3Apivotal-cloudfoundry-*)

| Artifact | Javadocs
| -------- | --------
| `pivotal-cloudfoundry-client` | [![Javadocs](https://javadoc.io/badge/io.pivotal/pivotal-cloudfoundry-client.svg)](https://javadoc.io/doc/io.pivotal/pivotal-cloudfoundry-client)
| `pivotal-cloudfoundry-client-reactor` | [![Javadocs](https://javadoc.io/badge/io.pivotal/pivotal-cloudfoundry-client-reactor.svg)](https://javadoc.io/doc/io.pivotal/pivotal-cloudfoundry-client-reactor)

| Job | Status
| --- | ------
| `unit-test`        | [![unit-test](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/pivotal-java-client/jobs/unit-test/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/pivotal-java-client/jobs/unit-test)
| `integration-test` | [![integration-test](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/pivotal-java-client/jobs/integration-test/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/pivotal-java-client/jobs/integration-test)
| `deploy`           | [![deploy](https://java-experience.ci.springapps.io/api/v1/teams/java-experience/pipelines/pivotal-java-client/jobs/deploy/badge)](https://java-experience.ci.springapps.io/teams/java-experience/pipelines/pivotal-java-client/jobs/deploy)


The `pcf-java-client` project is a Java language binding for interacting with a Pivotal Cloud Foundry instance.  Most of the Cloud Foundry API can be accessed with the [`cf-java-client`][d] project, and this is an extension of that project for Pivotal Cloud Foundry-specific APIs.  The project is broken up into a number of components which expose different levels of abstraction depending on need.

* `pivotal-cloudfoundry-client` – Interfaces, request, and response objects mapping to the Pivotal Cloud Foundry REST APIs.  This project has no implementation and therefore cannot connect a Pivotal Cloud Foundry instance on its own.
* `pivotal-cloudfoundry-client-reactor` – The default implementation of the `pivotal-cloudfoundry-client` project.  This implementation is based on the Reactor Netty [`HttpClient`][h].

## Dependencies
Most projects will need one dependencies; the implementation of the Client API.  For Maven, the dependencies would be defined like this:

```xml
<dependencies>
    <dependency>
        <groupId>io.pivotal</groupId>
        <artifactId>pivotal-cloudfoundry-client-reactor</artifactId>
        <version>1.0.0.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-core</artifactId>
        <version>3.1.5.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>io.projectreactor.ipc</groupId>
        <artifactId>reactor-netty</artifactId>
        <version>0.7.5.RELEASE</version>
    </dependency>
    ...
</dependencies>
```

Snapshot artifacts can be found in the Spring snapshot repository:

```xml
<repositories>
    <repository>
        <id>spring-snapshots</id>
        <name>Spring Snapshots</name>
        <url>http://repo.spring.io/snapshot</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
    ...
</repositories>
```

For Gradle, the dependencies would be defined like this:

```groovy
dependencies {
    compile 'io.pivotal:pivotal-cloudfoundry-client-reactor:1.0.0.RELEASE'
    compile 'io.projectreactor:reactor-core:3.1.5.RELEASE'
    compile 'io.projectreactor.ipc:reactor-netty:0.7.5.RELEASE'
    ...
}
```

Snapshot artifacts can be found in the Spring snapshot repository:

```groovy
repositories {
    maven { url 'http://repo.spring.io/snapshot' }
    ...
}
```

## Usage
The `pivotal-cloudfoundry-client` projects follows a ["Reactive"][r] design pattern and expose its responses with [Project Reactor][p] `Monos`s and `Flux`s.

### `SchedulerClient` Builder

The lowest-level building blocks of the API are `ConnectionContext` and `TokenProvider`.  These types are intended to be shared between instances of the clients, and come with out of the box implementations.  To instantiate them, you configure them with builders:

```java
DefaultConnectionContext.builder()
    .apiHost(apiHost)
    .build();

PasswordGrantTokenProvider.builder()
    .password(password)
    .username(username)
    .build();
```

In Spring-based applications, you'll want to encapsulate them in bean definitions:

```java
@Bean
DefaultConnectionContext connectionContext(@Value("${cf.apiHost}") String apiHost) {
    return DefaultConnectionContext.builder()
        .apiHost(apiHost)
        .build();
}

@Bean
PasswordGrantTokenProvider tokenProvider(@Value("${cf.username}") String username,
                                         @Value("${cf.password}") String password) {
    return PasswordGrantTokenProvider.builder()
        .password(password)
        .username(username)
        .build();
}
```

`SchedulerClient` is only an interface.  It has a [Reactor][p]-based implementation.  To instantiate it, you configure it with a builder:

```java
ReactorSchedulerClient.builder()
    .connectionContext(connectionContext)
    .tokenProvider(tokenProvider)
    .build();
```

In Spring-based applications, you'll want to encapsulate it in bean a definition:

```java
@Bean
ReactorSchedulerClient schedulerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
    return ReactorSchedulerClient.builder()
        .connectionContext(connectionContext)
        .tokenProvider(tokenProvider)
        .build();
}
```

## Development
The project depends on Java 8.  To build from source and install to your local Maven cache, run the following:

```shell
$ ./mvnw clean install
```

To run the integration tests, run the following:

```shell
$ ./mvnw -Pintegration-test clean test
```

**IMPORTANT**
Integration tests should be run against an empty Pivotal Cloud Foundry instance. The integration tests are destructive, affecting nearly everything on an instance given the chance.

The integration tests require a running instance of Pivotal Cloud Foundry to test against.  We recommend using [PCF Dev][i] to start a local instance to test with.  To configure the integration tests with the appropriate connection information use the following environment variables:

Name | Description
---- | -----------
`TEST_ADMIN_CLIENTID` | Client ID for a client with permissions for a Client Credentials grant
`TEST_ADMIN_CLIENTSECRET` | Client secret for a client with permissions for a Client Credentials grant
`TEST_ADMIN_PASSWORD` | Password for a user with admin permissions
`TEST_ADMIN_USERNAME` | Username for a user with admin permissions
`TEST_APIHOST` | The host of a Cloud Foundry instance.  Typically something like `api.local.pcfdev.io`.
`TEST_PROXY_HOST` | _(Optional)_ The host of a proxy to route all requests through
`TEST_PROXY_PASSWORD` | _(Optional)_ The password for a proxy to route all requests through
`TEST_PROXY_PORT` | _(Optional)_ The port of a proxy to route all requests through. Defaults to `8080`.
`TEST_PROXY_USERNAME` | _(Optional)_ The username for a proxy to route all requests through
`TEST_SKIPSSLVALIDATION` | _(Optional)_ Whether to skip SSL validation when connecting to the Cloud Foundry instance.  Defaults to `false`.

## Contributing
[Pull requests][u] and [Issues][e] are welcome.

## License
This project is released under version 2.0 of the [Apache License][l].

[c]: https://github.com/cloudfoundry/cli
[d]: https://github.com/cloudfoundry/java-client
[e]: https://github.com/cloudfoundry/java-client/issues
[g]: https://gradle.org
[h]: http://projectreactor.io/io/docs/api/reactor/io/netty/http/HttpClient.html
[i]: https://github.com/pivotal-cf/pcfdev
[l]: https://www.apache.org/licenses/LICENSE-2.0
[m]: https://maven.apache.org
[p]: https://projectreactor.io
[r]: http://reactivex.io
[u]: https://help.github.com/articles/using-pull-requests
