package io.rcapp;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class API {


  public JsonObject newUser(RoutingContext routingContext, Long usedId) {
    return new JsonObject();
  }

  public JsonObject changeName(RoutingContext routingContext, Long usedId) {
    return new JsonObject();
  }

  public JsonObject me(RoutingContext routingContext, Long usedId) {
    return new JsonObject();
  }
}
