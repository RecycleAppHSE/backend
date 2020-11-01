package io.rcapp;

import io.rcapp.domain.Corrections;
import io.rcapp.domain.User;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;
import java.util.List;

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

  public Single<User> me(Long userId) {
    return pool.preparedQuery("select id, name, photo_url from rc_user where id = $1")
        .rxExecute(Tuple.of(userId))
        .map(
            set -> {
              final Row next = set.iterator().next();
              return new User(
                  next.getLong("id"),
                  next.getString("name"),
                  next.getString("photo_url"),
                  List.of(),
                  new Corrections(List.of(), List.of()));
            });
  }
}
