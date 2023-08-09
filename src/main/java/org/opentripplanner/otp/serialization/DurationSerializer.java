package org.opentripplanner.otp.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Duration;

public class DurationSerializer extends StdDeserializer<Duration> {

  public DurationSerializer() {
    super((Class<?>) null);
  }

  @Override
  public Duration deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    long seconds = node.numberValue().longValue();
    return Duration.ofSeconds(seconds);
  }
}
