package io.rcapp;

import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.pgclient.PgPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    final var port = 8080;
    useVertxSchedulers(vertx);
    startServer(vertx, port);
    log.info("Started at port {}", port);
  }

  private static void startServer(Vertx vertx, int port) {
    final String uri = System.getenv("DB_URI");
    final PgPool pool = PgPool.pool(uri);
    final DB db = new DB(pool);
    final API api = new API(db);
    final Router router = Router.router(vertx);
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
    // NEWS
    // MAP
    router.get("/point").handler(new Auth(api::allPoints));
    router.get("/point/:pointId").handler(new Auth(api::point));
    router.get("/search").handler(new Auth(api::search));
    vertx
        .createHttpServer(
            new HttpServerOptions().setCompressionSupported(true).setCompressionLevel(9))
        .requestHandler(router)
        .listen(port);
  }

  public static void useVertxSchedulers(Vertx vertx) {
    RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
    RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
    RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));
  }
}
