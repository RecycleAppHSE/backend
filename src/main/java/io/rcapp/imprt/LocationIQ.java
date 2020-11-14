package io.rcapp.imprt;

import io.reactivex.Single;
import io.vertx.reactivex.ext.web.client.WebClient;

/**
 * A reverse geocoder based on https://locationiq.com/ which performs location(lat, lon) to symbolic
 * name resolution .
 */
public class LocationIQ {

  /** locationiq.com access token. */
  private final String token;

  /** Http client. */
  private final WebClient http;

  public LocationIQ(final String token, final WebClient http) {
    this.token = token;
    this.http = http;
  }

  public Single<String> resolve(final double latitude, final double longitude) {
    return http.getAbs("https://us1.locationiq.com/v1/reverse.php")
        .addQueryParam("key", token)
        .addQueryParam("lat", String.valueOf(latitude))
        .addQueryParam("lon", String.valueOf(longitude))
        .addQueryParam("format", "json")
        .rxSend()
        .flatMap(
            resp -> {
              if (resp.statusCode() == 200) {
                return Single.just(resp.bodyAsJsonObject().getString("display_name"));
              } else {
                return Single.error(
                    new Exception(
                        String.format(
                            "Non 200 status code: %d\nbody:\n%s",
                            resp.statusCode(), resp.bodyAsJsonObject().encodePrettily())));
              }
            });
  }
}
