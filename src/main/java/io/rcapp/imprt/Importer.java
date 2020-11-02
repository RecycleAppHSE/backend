package io.rcapp.imprt;

import io.rcapp.Main;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.pgclient.PgPool;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** App which imports data. */
public class Importer {

  private static final Logger log = LoggerFactory.getLogger(Importer.class);

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    Main.useVertxSchedulers(vertx);
    final List<JsonObject> rcmap =
        new RecycleMapRu(WebClient.create(vertx)).download().toList().blockingGet();
    final PgPool pool = PgPool.pool(System.getenv("DB_URI"));
    System.exit(0);
  }
}
