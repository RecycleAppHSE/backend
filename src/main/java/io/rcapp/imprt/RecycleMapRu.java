package io.rcapp.imprt;

import io.reactivex.Flowable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.ext.web.codec.BodyCodec;

/**
 * Imports data from https://recyclemap.ru/api/points_map.
 *
 * <p>Since the whole dataset is ~10 mb, one-shot download is fine.
 */
public class RecycleMapRu {

  private final WebClient client;

  public RecycleMapRu(WebClient client) {
    this.client = client;
  }

  public Flowable<JsonObject> download() {
    return client
        .getAbs("https://recyclemap.ru/api/points_map")
        .as(BodyCodec.jsonArray())
        .rxSend()
        .flatMapPublisher(resp -> Flowable.fromIterable(resp.body()))
        .map(el -> (JsonObject) el);
  }
}
