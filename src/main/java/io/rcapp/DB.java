package io.rcapp;

import io.rcapp.domain.Corrections;
import io.rcapp.domain.Point;
import io.rcapp.domain.PointIdWIthLocation;
import io.rcapp.domain.Schedule;
import io.rcapp.domain.Tip;
import io.rcapp.domain.TipCollection;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB {

  private static final Logger log = LoggerFactory.getLogger(DB.class);

  public static final Set<String> RECYCLE_TYPES =
      Set.of(
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
          "tires");

  private static final String SELECT_POINT =
      """
                    SELECT id,
                           feature_name AS name,
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
                           ST_X(st_transform(geom, 4326)) AS longitude,
                           ST_Y(st_transform(geom, 4326)) AS latitude,
                           works,
                           last_updated,
                           schedule,
                           corrections_count,
                           address
                    FROM collection_point
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
                count))
        .rxExecute()
        .flatMapPublisher(Flowable::fromIterable)
        .map(
            row ->
                new PointIdWIthLocation(
                    row.getLong("id"), row.getDouble("latitude"), row.getDouble("longitude")));
  }

  public Single<Long> newUser() {
    return pool.preparedQuery(
            "INSERT INTO rc_user(name, photo_url) VALUES (NULL, NULL) RETURNING id")
        .rxExecute()
        .map(rows -> rows.iterator().next().getLong(0));
  }

  public Completable changeName(String name, Long userId) {
    return pool.preparedQuery("UPDATE rc_user SET name = $1 WHERE id = $2")
        .rxExecute(Tuple.of(name, userId))
        .ignoreElement();
  }

  public Single<User> me(Long userId) {
    return pool.preparedQuery(
            """
                        SELECT id, name, photo_url,
                               (SELECT string_agg(id || '', ' ' ORDER BY id) FROM correction WHERE rc_user_id = $1 AND status = 'applied'::CORRECTION_STATUS) AS applied,
                               (SELECT string_agg(id || '', ' ' ORDER BY id) FROM correction WHERE rc_user_id = $1 AND status = 'rejected'::CORRECTION_STATUS) AS rejected,
                               (SELECT string_agg(id || '', ' ' ORDER BY id) FROM correction WHERE rc_user_id = $1 AND status = 'in-progress'::CORRECTION_STATUS) AS in_progress
                        FROM rc_user WHERE id = $1;
                        """)
        .rxExecute(Tuple.of(userId))
        .map(
            set -> {
              final Row next = set.iterator().next();
              final String applied = next.getString("applied");
              final String rejected = next.getString("rejected");
              final String inProgress = next.getString("in_progress");
              return new User(
                  next.getLong("id"),
                  next.getString("name"),
                  next.getString("photo_url"),
                  new Corrections(
                      applied == null
                          ? List.of()
                          : List.of(applied.split(" ")).stream()
                              .map(Long::parseLong)
                              .collect(Collectors.toList()),
                      inProgress == null
                          ? List.of()
                          : List.of(inProgress.split(" ")).stream()
                              .map(Long::parseLong)
                              .collect(Collectors.toList()),
                      rejected == null
                          ? List.of()
                          : List.of(rejected.split(" ")).stream()
                              .map(Long::parseLong)
                              .collect(Collectors.toList())));
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
                                """,
                SELECT_POINT))
        .rxExecute(Tuple.of(id))
        .map(set -> set.iterator().next())
        .map(DB::rowToPoint);
  }

  public Single<Long> newCorrection(Long userId, Long pointId, String field, String changeTo) {
    return pool.rxBegin()
        .flatMap(
            tx ->
                tx.preparedQuery(
                        "UPDATE collection_point SET corrections_count = corrections_count + 1"
                            + " WHERE id = $1")
                    .rxExecute(Tuple.of(pointId))
                    .ignoreElement()
                    .andThen(
                        tx.preparedQuery(
                                """
                                                INSERT INTO correction(rc_user_id, collection_point_id, field, change_to)
                                                VALUES ($1, $2, $3, $4) RETURNING id
                                                """)
                            .rxExecute(Tuple.of(userId, pointId, field, changeTo))
                            .map(rows -> rows.iterator().next().getLong(0)))
                    .doAfterSuccess(l -> tx.commit())
                    .doOnError(
                        err -> {
                          tx.rollback();
                          log.error("Rollback tx cause:", err);
                        }));
  }

  public Single<JsonObject> correction(Long correctionId) {
    return pool.preparedQuery(
            """
                        SELECT
                            id,
                            rc_user_id,
                            collection_point_id,
                            field,
                            change_to,
                            status,
                            submit_time,
                            like_count,
                            dislike_count
                        FROM correction WHERE id = $1
                        """)
        .rxExecute(Tuple.of(correctionId))
        .map(set -> set.iterator().next())
        .map(DB::rowToCorrection);
  }

  public Flowable<JsonObject> correctionsByPoint(Long pointId) {
    return pool.preparedQuery(
            """
                        SELECT
                            id,
                            rc_user_id,
                            collection_point_id,
                            field,
                            change_to,
                            status,
                            submit_time,
                            like_count,
                            dislike_count
                        FROM correction WHERE collection_point_id = $1
                        """)
        .rxExecute(Tuple.of(pointId))
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToCorrection);
  }

  public Flowable<Point> search(final String query) {
    return pool.preparedQuery(
            String.format(
                """
                                %s
                                WHERE to_tsvector('english',address) @@ to_tsquery($1)
                                LIMIT 10
                                """,
                SELECT_POINT))
        .rxExecute(Tuple.of(query + ":*"))
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToPoint);
  }

  public Completable likeCorrection(long userId, long correctionId, long like) {
    return pool.rxBegin()
        .flatMapCompletable(
            tx ->
                tx.preparedQuery(
                        "SELECT is_like FROM correction_like WHERE rc_user_id = $1 AND"
                            + " correction_id = $2")
                    .rxExecute(Tuple.of(userId, correctionId))
                    .flatMapCompletable(rs -> updateLikes(tx, userId, correctionId, like, rs))
                    .andThen(applyChangesIfNeeded(tx, correctionId))
                    .doOnComplete(tx::commit)
                    .doOnError(
                        err -> {
                          tx.rollback();
                          log.error("Rollback tx cause:", err);
                        }));
  }

  public Flowable<TipCollection> selectAllTipsCollections() {
    return pool.preparedQuery("SELECT id, title, tips_number FROM tip_collection")
        .rxExecute()
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToTipCollection);
  }

  public Flowable<Tip> selectAllTipsByCollection(long collectionId) {
    return pool.preparedQuery(
            "SELECT id, collection_id, title, content FROM tip WHERE collection_id = $1")
        .rxExecute(Tuple.of(collectionId))
        .flatMapPublisher(Flowable::fromIterable)
        .map(DB::rowToTip);
  }

  private Completable applyChangesIfNeeded(Transaction tx, long correctionId) {
    return tx.preparedQuery(
            """
                        UPDATE correction
                        SET status = CASE id
                                        WHEN $1 THEN 'applied'::CORRECTION_STATUS
                                        ELSE 'rejected'::CORRECTION_STATUS
                                     END
                        WHERE field = (SELECT field FROM correction WHERE id = $1)
                          AND collection_point_id = (SELECT collection_point_id FROM correction WHERE id = $1)
                          AND (SELECT like_count - dislike_count FROM correction WHERE id = $1) >= 3
                        RETURNING correction.id AS id,
                                  correction.field AS field,
                                  correction.collection_point_id AS collection_point_id,
                                  correction.status AS status,
                                  correction.change_to AS change_to
                        """)
        .rxExecute(Tuple.of(correctionId))
        .flatMapPublisher(Flowable::fromIterable)
        .filter(row -> row.getString("status").equals("applied"))
        .flatMapCompletable(
            row -> {
              final String field = row.getString("field");
              final String changeTo = row.getString("change_to");
              final Long pointId = row.getLong("collection_point_id");
              if (field.equals("works")) {
                return tx.preparedQuery("UPDATE collection_point SET works = $1 WHERE id = $2")
                    .rxExecute(Tuple.of(changeTo, pointId))
                    .ignoreElement();
              } else if (field.equals("recycle")) {
                Set<String> trues =
                    new JsonArray(changeTo)
                        .stream().map(el -> (String) el).collect(Collectors.toSet());
                Set<String> falses = new HashSet<>(RECYCLE_TYPES);
                falses.removeAll(trues);
                List<String> truesCond =
                    trues.stream()
                        .map(str -> String.format(str + " = true"))
                        .collect(Collectors.toList());
                List<String> falsesCond =
                    falses.stream()
                        .map(str -> String.format(str + " = false"))
                        .collect(Collectors.toList());
                truesCond.addAll(falsesCond);
                final String sets = truesCond.stream().collect(Collectors.joining(",\n"));
                String sql = "update collection_point set " + sets + " where id = $1";
                return tx.preparedQuery(sql).rxExecute(Tuple.of(pointId)).ignoreElement();
              }
              return Completable.error(new IllegalStateException("Unknown field to apply update"));
            });
  }

  /** Updates likes for correction_like and correction tables. */
  private Completable updateLikes(
      Transaction tx, long userId, long correctionId, long like, RowSet<Row> set) {
    boolean empty = !set.iterator().hasNext();
    boolean isLike = empty ? false : set.iterator().next().getBoolean("is_like");
    if (empty && like == 0) {
      return Completable.complete();
    } else if (!empty && like == 0) {
      return tx.preparedQuery(
              "DELETE FROM correction_like WHERE rc_user_id = $1 AND correction_id = $2")
          .rxExecute(Tuple.of(userId, correctionId))
          .ignoreElement()
          .andThen(txChangeLikeCounter(tx, correctionId, isLike, -1));
    } else if (empty && like != 0) {
      return tx.preparedQuery(
              "INSERT INTO correction_like(rc_user_id, correction_id, is_like) VALUES ($1,$2,$3)")
          .rxExecute(Tuple.of(userId, correctionId, like == 1))
          .ignoreElement()
          .andThen(txChangeLikeCounter(tx, correctionId, like == 1, 1));
    } else if (!empty && like != 0 && isLike == (like == 1)) {
      return Completable.complete();
    } else if (!empty && like != 0 && isLike != (like == 1)) {
      return tx.preparedQuery(
              "UPDATE correction_like SET is_like = $1 WHERE rc_user_id = $2 AND correction_id ="
                  + " $3 ")
          .rxExecute(Tuple.of(like == 1, userId, correctionId))
          .ignoreElement()
          .andThen(txChangeLikeCounter(tx, correctionId, !(like == 1), -1))
          .andThen(txChangeLikeCounter(tx, correctionId, like == 1, 1));
    }
    return Completable.error(new IllegalStateException("Case wasn't found"));
  }

  private static Completable txChangeLikeCounter(
      Transaction tx, long correctionId, boolean likeOrDislike, int change) {
    if (likeOrDislike) {
      return tx.preparedQuery("UPDATE correction SET like_count = like_count + $1 WHERE id = $2")
          .rxExecute(Tuple.of(change, correctionId))
          .ignoreElement();
    } else {
      return tx.preparedQuery(
              "UPDATE correction SET dislike_count = dislike_count + $1 WHERE id = $2")
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

  private static TipCollection rowToTipCollection(Row row) {
    return new TipCollection(
        row.getLong("id"), row.getString("title"), row.getInteger("tips_number"));
  }

  private static Tip rowToTip(Row row) {
    return new Tip(
        row.getLong("id"),
        row.getLong("collection_id"),
        row.getString("title"),
        row.getString("content"));
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
        row.getLong("corrections_count"));
  }

  private static Schedule rowToSchedule(Row row) {
    final String str = row.getString("schedule");
    if (str == null) {
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
            "tires")
        .stream()
        .filter(row::getBoolean)
        .collect(Collectors.toSet());
  }
}
