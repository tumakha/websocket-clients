package websocket.server.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

/**
 * @author Yuriy Tumakha
 */
public interface JsonSupport {

  ObjectMapper objectMapper = new ObjectMapper()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
      .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
      .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
      .setSerializationInclusion(Include.NON_NULL)
      .registerModule(new Jdk8Module())
      .registerModule(new JavaTimeModule());

  @SneakyThrows
  default <T> T fromJson(String json, Class<T> valueClass) {
    return objectMapper.readValue(json, valueClass);
  }

  @SneakyThrows
  default <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
    return objectMapper.readValue(json, valueTypeRef);
  }

  @SneakyThrows
  default String toJson(Object value) {
    return objectMapper.writeValueAsString(value);
  }

}
