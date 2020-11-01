package io.rcapp;

import io.vertx.reactivex.pgclient.PgPool;

public class DB {

  private PgPool pool;

  public DB(PgPool pool) {
    this.pool = pool;
  }
}
