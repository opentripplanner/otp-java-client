package org.opentripplanner.otp.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class OffsetDateTimeSerializer extends StdDeserializer<OffsetDateTime> {
  private final ZoneId zoneId;

  public OffsetDateTimeSerializer(ZoneId zone) {
    super((Class<?>) null);
    zoneId = zone;
  }

  @Override
  public OffsetDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    long epochMillis = node.numberValue().longValue();

    return Instant.ofEpochMilli(epochMillis).atZone(zoneId).toOffsetDateTime();
  }
}
