package io.rcapp;

import io.rcapp.domain.Corrections;
import io.rcapp.domain.Point;
import io.rcapp.domain.PointIdWIthLocation;
import io.rcapp.domain.Schedule;
import io.rcapp.domain.User;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Transaction;
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

  public Single<Point> point(Long id) {
    return pool.preparedQuery(
        String.format(
            """
                %s
                WHERE id = $1
                """, SELECT_POINT))
        .rxExecute(Tuple.of(id))
        .map(set -> set.iterator().next())
        .map(DB::rowToPoint);
  }

  public Single<Long> newCorrection(Long userId, Long pointId, String field, String changeTo) {
    return pool.preparedQuery(
        """
            insert into correction(rc_user_id, collection_point_id, field, change_to)
            values ($1, $2, $3, $4) returning id
            """)
        .rxExecute(Tuple.of(userId, pointId, field, changeTo))
        .map(rows -> rows.iterator().next().getLong(0));
  }

  public Single<JsonObject> correction(Long correctionId) {
    return pool.preparedQuery(
        """
            select
                id,
                rc_user_id,
                collection_point_id,
                field,
                change_to,
                status,
                submit_time,
                like_count,
                dislike_count
            from correction where id = $1
            """)
        .rxExecute(Tuple.of(correctionId))
        .map(set -> set.iterator().next())
        .map(DB::rowToCorrection);
  }

  public Flowable<Point> search(final String query) {
    return pool.preparedQuery(
        String.format(
            """
                %s
                WHERE to_tsvector('english',address) @@ to_tsquery($1)
                LIMIT 10
                """, SELECT_POINT))
        .rxExecute(Tuple.of(query + ":*"))
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToPoint);
  }

  public Completable likeCorrection(long userId, long correctionId, long like) {
    return pool.rxBegin().flatMapCompletable(tx ->
            tx.preparedQuery("select is_like from correction_like where rc_user_id = $1 and correction_id = $2")
                .rxExecute(Tuple.of(userId, correctionId))
                .flatMapCompletable(rs -> updateLikes(userId, correctionId, like, tx, rs))
                .doAfterTerminate(tx::commit)
    );
  }

  /**
   * Updates likes for correction_like and correction tables.
   */
  private Completable updateLikes(long userId, long correctionId, long like, Transaction tx, RowSet<Row> set) {
    boolean empty = !set.iterator().hasNext();
    boolean isLike = empty ? false : set.iterator().next().getBoolean("is_like");
    if (empty && like == 0) {
      return Completable.complete();
    } else if (!empty && like == 0) {
      return tx.preparedQuery("delete from correction_like where rc_user_id = $1 and correction_id = $2")
          .rxExecute(Tuple.of(userId, correctionId))
          .ignoreElement()
          .andThen(txChangeLikeCounter(tx, correctionId, isLike, -1));
    } else if (empty && like != 0) {
      return tx.preparedQuery("insert into correction_like(rc_user_id, correction_id, is_like) VALUES ($1,$2,$3)")
          .rxExecute(Tuple.of(userId, correctionId, like == 1))
          .ignoreElement()
          .andThen(txChangeLikeCounter(tx, correctionId, like == 1, 1));
    } else if (!empty && like != 0 && isLike == (like == 1)) {
      return Completable.complete();
    } else if (!empty && like != 0 && isLike != (like == 1)) {
      return tx.preparedQuery("update correction_like set is_like = $1 where rc_user_id = $2 and correction_id = $3 ")
          .rxExecute(Tuple.of(like == 1, userId, correctionId))
          .ignoreElement()
          .andThen(txChangeLikeCounter(tx, correctionId, !(like == 1), -1))
          .andThen(txChangeLikeCounter(tx, correctionId, like == 1, 1));
    }
    return Completable.error(new IllegalStateException("Case wasn't found"));
  }

  private static Completable txChangeLikeCounter(Transaction tx, long correctionId, boolean likeOrDislike, int change) {
    if (likeOrDislike) {
      return tx.preparedQuery("update correction set like_count = like_count + $1 where id = $2")
          .rxExecute(Tuple.of(change, correctionId))
          .ignoreElement();
    } else {
      return tx.preparedQuery("update correction set dislike_count = dislike_count + $1 where id = $2")
          .rxExecute(Tuple.of(change, correctionId))
          .ignoreElement();
    }
  }

  private static JsonObject rowToCorrection(Row row) {
    String field = row.getString("field");
    final JsonObject result = new JsonObject();
    result.put("id", row.getLong("id"));
    result.put("point_id", row.getLong("collection_point_id"));
    result.put("field", field);
    if (field.equals("recycle")) {
      result.put("change_to", new JsonArray(row.getString("change_to")));
    } else if (field.equals("works")) {
      result.put("change_to", row.getString("change_to"));
    }
    result.put("status", row.getString("status"));
    result.put("submit_time", row.getLocalDateTime("submit_time").toEpochSecond(ZoneOffset.UTC));
    result.put("like_count", row.getLong("like_count"));
    result.put("dislike_count", row.getLong("dislike_count"));
    return result;
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
