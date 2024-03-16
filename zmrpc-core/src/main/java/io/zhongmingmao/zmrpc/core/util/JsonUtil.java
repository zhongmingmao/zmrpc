package io.zhongmingmao.zmrpc.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtil {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  public static Optional<String> toJson(final Object object) {
    return Optional.ofNullable(object)
        .map(
            o -> {
              try {
                return MAPPER.writeValueAsString(o);
              } catch (JsonProcessingException e) {
                log.error("toJson fail, object: " + o, e);
                return null;
              }
            });
  }

  public static String toJsonOrEmpty(final Object object) {
    return toJson(object).orElse(emptyJson());
  }

  public static String toJsonOrNull(final Object object) {
    return toJson(object).orElse(null);
  }

  public static <T> Optional<T> fromJson(final String json, final Class<T> klass) {
    return fromJson(json, klass, false);
  }

  public static <T> Optional<T> fromJson(
      final String json, final Class<T> klass, final boolean silent) {
    try {
      return Optional.ofNullable(MAPPER.readValue(decorate(json), klass));
    } catch (IOException e) {
      if (silent) {
        log.debug("fromJson fail, json: " + json);
      } else {
        log.error("fromJson fail, json: " + json, e);
      }
      return Optional.empty();
    }
  }

  public static <T> T fromJsonOrNull(final String json, final Class<T> klass) {
    return fromJsonOrNull(json, klass, false);
  }

  public static <T> T fromJsonOrNull(
      final String json, final Class<T> klass, final boolean silent) {
    return fromJson(json, klass, silent).orElse(null);
  }

  public static <T> Optional<T> fromJson(final String json, final TypeReference<T> type) {
    try {
      return Optional.ofNullable(MAPPER.readValue(decorate(json), type));
    } catch (IOException e) {
      log.error("fromJson fail, json: " + json, e);
      return Optional.empty();
    }
  }

  public static <T> T fromJsonOrNull(final String json, final TypeReference<T> type) {
    return fromJson(json, type).orElse(null);
  }

  private static String decorate(final String json) {
    return Optional.ofNullable(json).map(String::trim).orElse(emptyJson());
  }

  public static String emptyJson() {
    return "{}";
  }
}
