package io.rcapp;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.Set;
import java.util.stream.Collectors;

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

  public Single<JsonObject> suggest(RoutingContext routingContext, Long userId) {
    final JsonObject request = routingContext.getBodyAsJson();
    final String changeTo;
    final String field = request.getString("field");
    if (field.equals("recycle")
        && request.getValue("change_to") instanceof JsonArray
        && DB.RECYCLE_TYPES.containsAll(
            request.getJsonArray("change_to").stream()
                .map(el -> (String) el)
                .collect(Collectors.toSet()))) {
      changeTo = request.getJsonArray("change_to").encode();
    } else if (field.equals("works")
        && request.getValue("change_to") instanceof String
        && Set.of("broken", "would_not_work", "works_fine")
            .contains(request.getString("change_to"))) {
      changeTo = request.getString("change_to");
    } else {
      return Single.error(new Exception("Invalid request"));
    }
    return db.newCorrection(userId, request.getLong("point_id"), field, changeTo)
        .map(id -> new JsonObject().put("correction_id", id));
  }

  public Single<JsonObject> correction(RoutingContext routingContext, Long userId) {
    return db.correction(Long.parseLong(routingContext.pathParam("correctionId")));
  }

  public Single<JsonObject> like(RoutingContext routingContext, Long userId) {
    return db.likeCorrection(
            userId,
            Long.parseLong(routingContext.pathParam("correctionId")),
            routingContext.getBodyAsJson().getLong("like"))
        .andThen(Single.just(new JsonObject()));
  }

  public Single<JsonObject> correctionsForPoint(RoutingContext routingContext, Long userId) {
    final Long pointId = Long.parseLong(routingContext.pathParam("pointId"));
    return db.correctionsByPoint(pointId)
        .toList()
        .map(
            corrections -> {
              final JsonArray arr = new JsonArray();
              corrections.forEach(arr::add);
              return new JsonObject().put("corrections", arr);
            });
  }

  public Single<JsonObject> getAllTipsCollections(RoutingContext routingContext, Long userId) {
    return db.selectAllTipsCollections()
        .map(JsonObject::mapFrom)
        .toList()
        .map(
            collections -> {
              final JsonArray arr = new JsonArray();
              collections.forEach(arr::add);
              return new JsonObject().put("collections", arr);
            });
  }

  public Single<JsonObject> getAllTipsByCollection(RoutingContext routingContext, Long userId) {
    final Long collectionId = Long.parseLong(routingContext.pathParam("collectionId"));
    return db.selectAllTipsByCollection(collectionId)
        .map(JsonObject::mapFrom)
        .toList()
        .map(
            tips -> {
              final JsonArray arr = new JsonArray();
              tips.forEach(arr::add);
              return new JsonObject().put("tips", arr);
            });
  }

  public Single<JsonObject> deleteCorrection(RoutingContext routingContext, Long userId) {
    long correctionId = Long.parseLong(routingContext.pathParam("correctionId"));
    return db.deleteCorrectionBy(correctionId, userId)
        .map(
            res -> {
              if (res) {
                return new JsonObject().put("message", "Successfully delete");
              } else {
                return new JsonObject()
                    .put("message", "Correction was not deleted due to unmet conditions");
              }
            });
  }
}
