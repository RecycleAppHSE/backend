package io.rcapp;

import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;

public class DB {

  private PgPool pool;

  public DB(PgPool pool) {
    this.pool = pool;
  }

  public Single<Long> newUser(){
    return pool.query("insert into rc_user(name, photo_url) values (null, null) returning id")
        .rxExecute()
        .map(rows -> rows.iterator().next().getLong(0));
  }
}
