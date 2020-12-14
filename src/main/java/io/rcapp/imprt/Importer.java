package io.rcapp.imprt;

import io.rcapp.Main;
import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** App which imports data. */
public class Importer {

  private static final Logger log = LoggerFactory.getLogger(Importer.class);

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    Main.useVertxSchedulers(vertx);
    final PgPool pool = PgPool.pool(System.getenv("DB_URI"));
    new RecycleMapRu(WebClient.create(vertx))
        .download()
        .map(
            entries ->
                Tuple.wrap(
                    List.of(
                        entries.getLong("gid"),
                        entries.getString("feature_name"),
                        entries.getBoolean("plastic"),
                        entries.getBoolean("glass"),
                        entries.getBoolean("paper"),
                        entries.getBoolean("metal"),
                        entries.getBoolean("tetra_pack"),
                        entries.getBoolean("batteries"),
                        entries.getBoolean("light_bulbs"),
                        entries.getBoolean("clothes"),
                        entries.getBoolean("appliances"),
                        entries.getBoolean("toxic"),
                        entries.getBoolean("other"),
                        entries.getBoolean("caps"),
                        entries.getBoolean("tires"),
                        entries.getString("geom"))))
        .buffer(1000)
        .flatMapCompletable(
            tuples ->
                pool.preparedQuery(
                        """
                  insert into collection_point(gid, feature_name, plastic, glass, paper, metal, tetra_pack, batteries,
                                               light_bulbs, clothes, appliances, toxic, other, caps, tires, geom)
                  values ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
                  ON CONFLICT (gid) DO NOTHING
                  """)
                    .rxExecuteBatch(tuples)
                    .ignoreElement()
                    .andThen(
                        Completable.fromRunnable(
                            () -> log.info("Imported {} records", tuples.size()))))
        .blockingAwait();
    vertx.close();
    System.exit(0);
  }
}
