package io.rcapp;

import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    vertx
        .createHttpServer()
        .requestHandler(
            req -> {
              req.response().putHeader("content-type", "text/plain").end("Hello world!");
            })
        .listen(8080);
    System.out.println("Started at port 8080");
  }
}
