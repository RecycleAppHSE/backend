package io.rcapp.imprt;

import io.rcapp.DB;
import io.rcapp.Main;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Performs dataset geocoding. */
public class Geocoder {

  private static final Logger log = LoggerFactory.getLogger(Geocoder.class);

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    Main.useVertxSchedulers(vertx);
    PgPool pool = PgPool.pool(System.getenv("DB_URI"));
    final DB db = new DB(pool);
    final LocationIQ liq = new LocationIQ(System.getenv("LIQ_TOKEN"), WebClient.create(vertx));
    try {
      db.pointsWithoutAddress(1000)
          .buffer(2)
          .flatMap(elem -> Flowable.just(elem).delaySubscription(2, TimeUnit.SECONDS), 1)
          .flatMap(
              points ->
                  Single.merge(
                      points.stream()
                          .map(
                              point ->
                                  liq.resolve(point.latitude(), point.longitude())
                                      .onErrorReturnItem("NOT_FOUND")
                                      .map(name -> Tuple.of(name, point.id()))
                          )
                          .collect(Collectors.toList())))
          .buffer(2)
          .flatMapCompletable(
              tuples ->
                  pool.preparedQuery("update collection_point set address = $1 where id = $2")
                      .rxExecuteBatch(tuples)
                      .ignoreElement()
                      .andThen(
                          Completable.fromRunnable(
                              () ->
                                  log.info(
                                      "updated address for {} collection points", tuples.size()))))
          .blockingAwait();
    } catch (Exception e) {
      System.exit(-1);
    } finally {
      vertx.close();
    }
    System.exit(0);
  }
}
