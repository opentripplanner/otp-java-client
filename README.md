## OTP GraphQL API Java Client

This library provides an easy way to access OTP's GTFS GraphQL API with Java.

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

var result =client.plan(
      TripPlanParameters.builder()
        .withFrom(ORIGIN)
        .withTo(DEST)
        .withTime(LocalDateTime.now())
        .withModes(Set.of(RequestMode.TRANSIT))
        .build()
    );
```

For more examples take a look at [`IntegrationTest.java`](https://github.com/opentripplanner/otp-java-client/blob/main/src/test/java/org/opentripplanner/IntegrationTest.java).

### Releasing

```sh
git tag 0.0.4 -m "Release 0.0.4"
mvn clean deploy
git push --tags
```