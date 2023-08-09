package org.opentripplanner.otp.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class ObjectMappers {
  public static ObjectMapper withTimezone(ZoneId timezone) {
    var mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    SimpleModule module = new SimpleModule();
    module.addDeserializer(OffsetDateTime.class, new OffsetDateTimeSerializer(timezone));
    module.addDeserializer(Duration.class, new DurationSerializer());
    mapper.registerModule(module);
    return mapper;
  }
}
