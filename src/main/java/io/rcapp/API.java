package io.rcapp;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class API {

  public void newUser(RoutingContext routingContext) {}

  public Single<JsonObject> changeName(RoutingContext routingContext, Long usedId) {
    return Single.just(new JsonObject());
  }

  public Single<JsonObject> me(RoutingContext routingContext, Long usedId) {
    return Single.just(new JsonObject());
  }
}
