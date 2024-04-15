package io.zhongmingmao.zmrpc.core.util;

import io.zhongmingmao.zmrpc.core.api.error.RpcExceptions;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

@Slf4j
public final class HttpUtil {

  public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

  private static final TrustManager[] TRUST_ALL_CERTS =
      new TrustManager[] {
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(
              java.security.cert.X509Certificate[] chain, String authType) {}

          @Override
          public void checkServerTrusted(
              java.security.cert.X509Certificate[] chain, String authType) {}

          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
          }
        }
      };

  public static OkHttpClient buildClient() {
    try {
      // disables the default certificate validations
      SSLContext context = SSLContext.getInstance("SSL");
      context.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
      OkHttpClient.Builder builder =
          new OkHttpClient.Builder()
              .connectTimeout(1, TimeUnit.SECONDS)
              .readTimeout(1, TimeUnit.SECONDS)
              .writeTimeout(1, TimeUnit.SECONDS);
      builder.sslSocketFactory(context.getSocketFactory(), (X509TrustManager) TRUST_ALL_CERTS[0]);
      builder.hostnameVerifier((hostname, session) -> true);
      return builder.build();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      String message = "build client fail";
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
  }

  public static <R> Optional<R> execute(
      final OkHttpClient client, final Request request, final Class<R> res) {
    try (Response response = client.newCall(request).execute()) {
      String body = "";
      if (Objects.nonNull(response.body())) {
        body = response.body().string();
      }

      if (response.isSuccessful()) {
        log.debug("execute success, uri: {}, body: {}", request.url(), body);
        return JsonUtil.fromJson(body, res);
      }
      log.error("execute fail, code: {}, uri: {}, body: {}", response.code(), request.url(), body);
    } catch (IOException e) {
      String message = "execute fail, uri: " + request.url();
      log.error(message, e);
      throw RpcExceptions.newTechErr(message, e);
    }
    return Optional.empty();
  }
}
