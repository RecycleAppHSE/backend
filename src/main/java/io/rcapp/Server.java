package io.rcapp;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.pgclient.PgPool;

/** The http server of this app. */
public class Server {

  private final HttpServer server;
  private final Vertx vertx;
  private final int port;

  public Server(final Vertx vertx, final int port) {
    this.vertx = vertx;
    this.port = port;
    final String uri = System.getenv("DB_URI");
    final PgPool pool = PgPool.pool(uri);
    final DB db = new DB(pool);
    final API api = new API(db);
    this.server =
        vertx
            .createHttpServer(
                new HttpServerOptions().setCompressionSupported(true).setCompressionLevel(9))
            .requestHandler(this.router(api));
  }

  public void start() {
    this.server.listen(port);
  }

  public void stop() {
    this.server.rxClose().blockingGet();
  }

  private Router router(final API api) {
    final Router router = Router.router(this.vertx);
    router.route().handler(BodyHandler.create());
    router
        .get("/")
        .handler(
            req -> {
              req.response().putHeader("content-type", "text/plain").end("Hello world!");
            });
    // USER PROFILE
    router.get("/new_user").handler(api::newUser);
    router.post("/change_name").handler(new Auth(api::changeName));
    router.get("/me").handler(new Auth(api::me));
    // ADVICES
    // MAP
    router.get("/point").handler(new Auth(api::allPoints));
    router.get("/point/:pointId").handler(new Auth(api::point));
    router.get("/point/:pointId/corrections").handler(new Auth(api::correctionsForPoint));
    router.get("/search").handler(new Auth(api::search));
    router.post("/correction/suggest").handler(new Auth(api::suggest));
    router.get("/correction/:correctionId").handler(new Auth(api::correction));
    router.post("/correction/:correctionId/like").handler(new Auth(api::like));
    // tips
    router.get("/tip/collections").handler(new Auth(api::getAllTipsCollections));
    router.get("/tip/:collectionId").handler(new Auth(api::getAllTipsByCollection));
    return router;
  }
}
