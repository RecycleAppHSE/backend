package io.rcapp;

import io.rcapp.domain.*;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DB {

  private static final String SELECT_POINT =
          """
          SELECT id,
                 feature_name as name,
                 phone_number,
                 web_site,
                 plastic,
                 glass,
                 paper,
                 metal,
                 tetra_pack,
                 batteries,
                 light_bulbs,
                 clothes,
                 appliances,
                 toxic,
                 other,
                 caps,
                 tires,
                 ST_X(st_transform(geom, 4326)) as longitude,
                 ST_Y(st_transform(geom, 4326)) as latitude,
                 works,
                 last_updated,
                 schedule,
                 corrections_count,
                 address
          from collection_point
          """;

  private PgPool pool;

  public DB(PgPool pool) {
    this.pool = pool;
  }

  /**
   * @param count count of pairs to fetch
   * @return flow of coordinates: pair of Latitude and Longitude
   */
  public Flowable<PointIdWIthLocation> pointsWithoutAddress(long count) {
    return pool.preparedQuery(
        String.format(
              """
              select id,
                     st_x(st_transform(geom, 4326)) as longitude,
                     st_y(st_transform(geom, 4326)) as latitude
              from collection_point
              where address is null
              limit %d
              """,
            count
        )
    )
        .rxExecute()
        .flatMapPublisher(Flowable::fromIterable)
        .map(row -> new PointIdWIthLocation(
            row.getLong("id"),
            row.getDouble("latitude"),
            row.getDouble("longitude"))
        );
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

  public Flowable<Point> allPoints() {
    return pool.preparedQuery(SELECT_POINT)
        .rxExecute()
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToPoint);
  }

  public Flowable<Point> search(final String query) {
    return pool.preparedQuery(
        String.format(
            """
                %s
                WHERE to_tsvector('russian', address) @@ to_tsquery($1)
                LIMIT 10
                """, SELECT_POINT))
        .rxExecute(Tuple.of(query + ":*"))
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToPoint);
  }

  private static Point rowToPoint(Row row) {
    return new Point(
        row.getLong("id"),
        row.getString("name"),
        row.getString("address"),
        row.getString("phone_number"),
        row.getString("web_site"),
        rowToRecycleList(row),
        row.getDouble("latitude"),
        row.getDouble("longitude"),
        row.getString("works"),
        row.getLocalDateTime("last_updated").toEpochSecond(ZoneOffset.UTC),
        rowToSchedule(row),
        row.getLong("corrections_count")
    );
  }

  private static Schedule rowToSchedule(Row row) {
    final String str = row.getString("schedule");
    if(str == null) {
      return new Schedule(null, null);
    } else {
      final String[] split = str.split("-");
      return new Schedule(split[0], split[1]);
    }
  }

  private static Set<String> rowToRecycleList(Row row) {
    return Set.of(
        "plastic",
        "glass",
        "paper",
        "metal",
        "tetra_pack",
        "batteries",
        "light_bulbs",
        "clothes",
        "appliances",
        "toxic",
        "other",
        "caps",
        "tires"
    ).stream().filter(row::getBoolean).collect(Collectors.toSet());
  }
}
