package io.rcapp;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Tuple;

public class DB {

  private PgPool pool;

  public DB(PgPool pool) {
    this.pool = pool;
  }

  public Single<Long> newUser() {
    return pool.preparedQuery(
            "insert into rc_user(name, photo_url) values (null, null) returning id")
        .rxExecute()
        .map(rows -> rows.iterator().next().getLong(0));
  }

  public Completable changeName(String name, Long userId) {
    return pool.preparedQuery("update rc_user set name = $1 where id = $2")
        .rxExecute(Tuple.of(name, userId))
        .ignoreElement();
  }
}
