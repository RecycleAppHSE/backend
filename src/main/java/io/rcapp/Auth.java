package io.rcapp;

import com.google.common.primitives.Longs;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.function.BiFunction;

public class Auth implements Handler<RoutingContext> {

  private BiFunction<RoutingContext, Long, JsonObject> function;

  public Auth(BiFunction<RoutingContext, Long, JsonObject> function) {
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
    function.apply(event, id);
  }
}
