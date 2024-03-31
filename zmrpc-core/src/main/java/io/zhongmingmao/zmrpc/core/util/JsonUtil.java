package io.zhongmingmao.zmrpc.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
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

  public static <T> Optional<T> fromJson(final String json, final Class<T> klass) {
    try {
      return Optional.ofNullable(MAPPER.readValue(decorate(json), klass));
    } catch (IOException e) {
      log.error("fromJson fail, json: " + json, e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> fromJsonList(final String json, final Class<T> elementClass) {
    return fromJsonCollection(json, List.class, elementClass);
  }

  public static <T> Optional<T> fromJsonSet(final String json, final Class<T> elementClass) {
    return fromJsonCollection(json, Set.class, elementClass);
  }

  public static <T> Optional<T> fromJsonCollection(
      final String json,
      final Class<? extends Collection> collectionClass,
      final Class<T> elementClass) {
    try {
      return Optional.ofNullable(
          MAPPER.readValue(
              decorate(json),
              MAPPER.getTypeFactory().constructCollectionType(collectionClass, elementClass)));
    } catch (IOException e) {
      log.error("fromJsonCollection fail, json: " + json, e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> fromJsonMap(
      final String json, final Class<T> keyClass, final Class<T> valueClass) {
    try {
      return Optional.ofNullable(
          MAPPER.readValue(
              decorate(json),
              MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass)));
    } catch (IOException e) {
      log.error("fromJsonMap fail, json: " + json, e);
      return Optional.empty();
    }
  }

  public static <T> T fromJsonOrNull(final String json, final Class<T> klass) {
    return fromJson(json, klass).orElse(null);
  }

  public static <T> T fromJsonListOrNull(final String json, final Class<T> elementClass) {
    return fromJsonList(json, elementClass).orElse(null);
  }

  public static <T> T fromJsonSetOrNull(final String json, final Class<T> elementClass) {
    return fromJsonSet(json, elementClass).orElse(null);
  }

  public static <T> T fromJsonMapOrNull(
      final String json, final Class<T> keyClass, final Class<T> valueClass) {
    return fromJsonMap(json, keyClass, valueClass).orElse(null);
  }

  private static String decorate(final String json) {
    return Optional.ofNullable(json).map(String::trim).orElse(emptyJson());
  }

  public static String emptyJson() {
    return "{}";
  }
}
