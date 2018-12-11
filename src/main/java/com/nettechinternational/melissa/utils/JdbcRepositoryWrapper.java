package com.nettechinternational.melissa.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.serviceproxy.ServiceException;
import java.util.List;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class JdbcRepositoryWrapper {

    protected final JDBCClient client;

    public JdbcRepositoryWrapper(Vertx vertx, JsonObject config) {
        this.client = JDBCClient.createShared(vertx, config);
    }

    private JsonArray getParams(Object... objects) {
        JsonArray jsonArray = new JsonArray();
        for (Object k : objects) {
            jsonArray.add(k);
        }

        return jsonArray;
    }

    protected int calcPage(int page, int limit) {
        if (page <= 0) {
            return 0;
        }
        return limit * (page - 1);
    }

    /**
     * Suitable for `add`, `exists` operation.
     *
     * @param params query params
     * @param sql SQL Query to execute
     * @param resultHandler async result handler
     */
    protected void executeNoResult(String sql, JsonArray params, Handler<AsyncResult<Void>> resultHandler) {
        client.getConnection(connHandler(resultHandler, connection -> {
            connection.updateWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    resultHandler.handle(Future.succeededFuture());
                } else {
                    resultHandler.handle(Future.failedFuture(r.cause()));
                }
                connection.close();
            });
        }));
    }

    /**
     *
     * @param sql
     * @param params
     * @return
     */
    protected Future<JsonObject> retrieveOne(String sql, Object... params) {
        return getConnection()
                .compose(connection -> {
                    Future<JsonObject> future = Future.future();
                    JsonArray jsonArray = getParams(params);
                    connection.queryWithParams(sql, jsonArray, r -> {
                        if (r.succeeded()) {
                            List<JsonObject> resList = r.result().getRows();
                            if (resList == null || resList.isEmpty()) {
                                future.fail(new ServiceException(404, "Not found!"));
                            } else {
                                future.complete(resList.get(0));
                            }
                        } else {
                            future.fail(new ServiceException(500, r.cause().getMessage()));
                        }
                        connection.close();
                    });
                    return future;
                });
    }

    protected Future<List<JsonObject>> retrieveByPage(String sql, int page, int limit) {
        JsonArray params = new JsonArray().add(calcPage(page, limit)).add(limit);
        return getConnection().compose(connection -> {
            Future<List<JsonObject>> future = Future.future();
            connection.queryWithParams(sql, params, r -> {
                if (r.succeeded()) {
                    future.complete(r.result().getRows());
                } else {
                    future.fail(new ServiceException(500, r.cause().getMessage()));
                }
                connection.close();
            });
            return future;
        });
    }

    protected Future<List<JsonObject>> retrieveMany(String sql, Object... params) {
        return getConnection().compose(connection -> {
            Future<List<JsonObject>> future = Future.future();
            JsonArray jsonArray = getParams(params);
            connection.queryWithParams(sql, jsonArray, r -> {
                if (r.succeeded()) {
                    future.complete(r.result().getRows());
                } else {
                    future.fail(new ServiceException(500, r.cause().getMessage()));
                }
                connection.close();
            });
            return future;
        });
    }

    protected Future<List<JsonObject>> retrieveAll(String sql) {
        return getConnection().compose(connection -> {
            Future<List<JsonObject>> future = Future.future();
            connection.query(sql, r -> {
                if (r.succeeded()) {
                    future.complete(r.result().getRows());
                } else {
                    future.fail(new ServiceException(500, r.cause().getMessage()));
                }
                connection.close();
            });
            return future;
        });
    }

    protected Future<Integer> execute(String sql, Object... params) {
        return getConnection()
                .compose(connection -> {
                    Future<Integer> future = Future.future();
                    JsonArray prm = getParams(params);
                    connection.updateWithParams(sql, prm, r -> {
                        if (r.succeeded()) {
                            future.complete(r.result().getUpdated());
                        } else {
                            future.fail(new ServiceException(500, r.cause().getMessage()));
                        }
                        connection.close();
                    });
                    return future;
                });
    }

    /**
     * A helper methods that generates async handler for SQLConnection
     *
     * @param <R>
     * @param h1
     * @param h2
     * @return generated handler
     */
    protected <R> Handler<AsyncResult<SQLConnection>> connHandler(Handler<AsyncResult<R>> h1, Handler<SQLConnection> h2) {
        return conn -> {
            if (conn.succeeded()) {
                final SQLConnection connection = conn.result();
                h2.handle(connection);
            } else {
                h1.handle(Future.failedFuture(conn.cause().getMessage()));
            }
        };
    }

    protected Future<SQLConnection> getConnection() {
        Future<SQLConnection> future = Future.future();
        client.getConnection(future.completer());
        return future;
    }

    protected String emptyIfNull(String object) {
        if (object == null) {
            return "";
        }

        return object;
    }

}
