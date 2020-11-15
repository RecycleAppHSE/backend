package io.rcapp;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class API {

  private final DB db;

  public API(DB db) {
    this.db = db;
  }

  public void newUser(RoutingContext rc) {
    db.newUser()
        .subscribe(id -> rc.response().end(new JsonObject().put("user_id", id).encodePrettily()));
  }

  public Single<JsonObject> changeName(RoutingContext routingContext, Long usedId) {
    final JsonObject body = routingContext.getBodyAsJson();
    final String change = body.getString("change_to");
    return db.changeName(change, usedId).andThen(Single.just(new JsonObject()));
  }

  public Single<JsonObject> me(RoutingContext routingContext, Long usedId) {
    return db.me(usedId).map(JsonObject::mapFrom);
  }

  public Single<JsonObject> allPoints(RoutingContext routingContext, Long userId) {
    return db.allPoints()
        .map(JsonObject::mapFrom)
        .toList()
        .map(
            points -> {
              final JsonArray arr = new JsonArray();
              points.forEach(arr::add);
              return new JsonObject().put("points", arr);
            });
  }

  public Single<JsonObject> point(RoutingContext routingContext, Long userId) {
    return db.point(Long.parseLong(routingContext.pathParam("pointId"))).map(JsonObject::mapFrom);
  }

  public Single<JsonObject> search(RoutingContext routingContext, Long userId) {
    return db.search(routingContext.request().getParam("q"))
        .map(JsonObject::mapFrom)
        .toList()
        .map(
            points -> {
              final JsonArray arr = new JsonArray();
              points.forEach(arr::add);
              return new JsonObject().put("points", arr);
            });
  }
}
