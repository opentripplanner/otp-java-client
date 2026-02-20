## OTP GraphQL API Java Client

This library provides an easy way to access OTP's GTFS GraphQL API with Java.

### OTP version compatibility

Not every version of this client works with every version of OTP. Check the
following table to select the correct artifact:

| OTP version | Client version |
|-------------|----------------|
| 2.4.0       | =< 0.0.31      |
| 2.5.0       | 0.1.0 - 0.1.13 |
| 2.7.0       | => 1.0.0       |

### Backwards compatibility

Until version 2.0.0 this library will not be strictly following semantic versioning as a lot 
of changes are planned.

### Installation

[![Maven Central](https://img.shields.io/maven-central/v/org.opentripplanner/otp-client.svg)](https://mvnrepository.com/artifact/org.opentripplanner/otp-client)

It's deployed to Maven Central so add it to your project like this:

```xml
<dependency>
   <groupId>org.opentripplanner</groupId>
   <artifactId>otp-client</artifactId>
   <version>${LATEST_VERSION}</version>
</dependency>
```

### Usage

```java
Coordinate ORIGIN = new Coordinate(52.4885, 13.3398);
Coordinate DEST = new Coordinate(52.5211, 13.4106);
OtpApiClient client = new OtpApiClient(ZoneId.of("Europe/Berlin"), "https://example.com");

var result = client.plan(
      TripPlanParameters.builder()
        .withFrom(ORIGIN)
        .withTo(DEST)
        .withTime(LocalDateTime.now())
        .withModes(Set.of(RequestMode.TRANSIT))
        .build()
    );
```

To create a more customized client, for example to specify the API path or some default Headers in the HTTP client, you can also use the builder API of the client:
```java
OtpApiClient client = OtpApiClient.builder()
        .graphQLUri("https://example.com/custom/path")
        .timeZone(ZoneId.of("Europe/Berlin"))
        .httpClient(customHttpClient)
        .build();
```

For more examples take a look at [`IntegrationTest.java`](https://github.com/opentripplanner/otp-java-client/blob/main/client/src/test/java/org/opentripplanner/IntegrationTest.java).

### Releasing

```sh
git tag 0.0.4 -m "Release 0.0.4"
mvn clean deploy
git push --tags
```
