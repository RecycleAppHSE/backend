package io.rcapp;

import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;

public class Main {

  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    final var port = 8080;
    useVertxSchedulers(vertx);
    startServer(vertx, port);
    System.out.println("Started at port 8080");
  }

  private static void startServer(Vertx vertx, int port) {
    final Router router = Router.router(vertx);
    final API api = new API();
    final DB db = new DB();
    router
        .get("/")
        .handler(
            req -> {
              req.response().putHeader("content-type", "text/plain").end("Hello world!");
            });
    // USER PROFILE
    router.get("/new_user").handler(new Auth(api::newUser));
    router.post("/change_name").handler(new Auth(api::changeName));
    router.post("/me").handler(new Auth(api::me));
    // ADVICES
    // NEWS
    // MAP
    vertx.createHttpServer().requestHandler(router).listen(port);
  }

  public static void useVertxSchedulers(Vertx vertx) {
    RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
    RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
    RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));
  }
}
