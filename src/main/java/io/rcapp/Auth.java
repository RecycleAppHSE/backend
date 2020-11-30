package io.rcapp;

import com.google.common.primitives.Longs;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auth implements Handler<RoutingContext> {

  private static final Logger log = LoggerFactory.getLogger(Auth.class);

  private BiFunction<RoutingContext, Long, Single<JsonObject>> function;

  public Auth(BiFunction<RoutingContext, Long, Single<JsonObject>> function) {
    this.function = function;
  }

  @Override
  public void handle(RoutingContext event) {
    final String header = event.request().getHeader("USER_ID");
    if (header == null) {
      event.response().setStatusCode(401).end();
      return;
    }
    final Long id = Longs.tryParse(header);
    if (id == null) {
      event.response().setStatusCode(401).end();
      return;
    }
    function
        .apply(event, id)
        .subscribe(
            json -> event.response().end(json.encodePrettily()),
            error -> {
              log.error("Error:", error);
              event.response().setStatusCode(400).end();
            });
  }
}
