package org.opentripplanner.client.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class ObjectMappers {
  public static ObjectMapper withTimezone(ZoneId timezone) {
    SimpleModule module = new SimpleModule();
    module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeSerializer(timezone));
    module.addDeserializer(Duration.class, new DurationSerializer());
    return new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(module)
        .registerModule(new Jdk8Module());
  }
}
