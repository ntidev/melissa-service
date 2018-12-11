package com.nettechinternational.melissa.store.impl;

import com.nettechinternational.melissa.store.Application;
import com.nettechinternational.melissa.store.Token;
import com.nettechinternational.melissa.store.TokenService;
import com.nettechinternational.melissa.utils.JdbcRepositoryWrapper;
import com.nettechinternational.melissa.utils.TokenGenerator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class TokenServiceImpl extends JdbcRepositoryWrapper implements TokenService {

    private final static Logger LOG = LoggerFactory.getLogger(TokenServiceImpl.class);

    public TokenServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public TokenService store(Token token, Handler<AsyncResult<Token>> handler) {

        LOG.debug("Storing token: {}", token.toJson().encode());

        long createdAt = new Date().getTime();
        token.setEndlife(createdAt + (1000 * 60 * 3));
        token.setCreatedAt(createdAt);

        String id = UUID.randomUUID().toString();
        String code = TokenGenerator.get();

        JsonArray params = new JsonArray()
                .add(id)
                .add(token.getApplication().getId())
                .add(token.getUsername())
                .add(code)
                .add(token.getEndlife())
                .add(token.getCreatedAt());

        executeNoResult(INSERT_TOKEN, params, res1 -> {
            if (res1.succeeded()) {
                token.setCode(code);
                handler.handle(Future.succeededFuture(token));
            } else {
                handler.handle(ServiceException.fail(500, "The message couldn't be created. Error : " + res1.cause().getMessage()));
            }
        });

        return this;
    }

    @Override
    public TokenService get(String code, Handler<AsyncResult<Token>> handler) {

        this.retrieveOne(GET_TOKEN_BY_CODE, code)
                .map(t -> {
                    Token token = new Token(t);
                    Application a = new Application()
                            .setName(t.getString("app_name"))
                            .setId(t.getString("application_id"));
                    token.setApplication(a);
                    return token;
                })
                .setHandler(handler);
        return this;
    }

    @Override
    public TokenService remove(String token, Handler<AsyncResult<Void>> handler) {
        this.execute(DELETE_TOKEN, token).setHandler(res -> {
            if (res.succeeded()) {
                handler.handle(Future.succeededFuture());
            } else {
                handler.handle(Future.failedFuture(res.cause().getMessage()));
            }
        });

        return this;
    }

    @Override
    public TokenService getAll(Handler<AsyncResult<List<Token>>> handler) {
        retrieveAll(FETCH_ALL_STATEMENT)
                .map(rows -> {
                    return rows.stream()
                            .map(t -> {
                                Token token = new Token(t);
                                Application a = new Application()
                                        .setName(t.getString("app_name"))
                                        .setId(t.getString("application_id"));
                                token.setApplication(a);
                                return token;
                            }).collect(Collectors.toList());
                }).setHandler(handler);
        return this;
    }

    @Override
    public TokenService getOldTokens(Handler<AsyncResult<List<Token>>> handler) {
        long time = new Date().getTime();
        retrieveMany(FETCH_OLD_TOKEN, time)
                .map(rows -> {
                    return rows.stream()
                            .map(t -> {
                                Token token = new Token(t);
                                Application a = new Application()
                                        .setName(t.getString("app_name"))
                                        .setId(t.getString("application_id"));
                                token.setApplication(a);
                                return token;
                            }).collect(Collectors.toList());
                }).setHandler(handler);
        return this;
    }

    private static final String INSERT_TOKEN = "INSERT INTO `token` (`id`, `application_id`, `username`, `code`, `lifeend`, `created_at`) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_TOKEN = "SELECT t.id, application_id, t.username, t.code, t.lifeend, t.created_at AS createdAt, a.name AS app_name FROM token t INNER JOIN application a ON t.application_id = a.id";
    private static final String GET_TOKEN_BY_CODE = SELECT_TOKEN + " WHERE code = ?";
    private static final String FETCH_ALL_STATEMENT = SELECT_TOKEN;
    private static final String FETCH_OLD_TOKEN = SELECT_TOKEN + " WHERE lifeend < ?  ";
    private static final String DELETE_TOKEN = "DELETE FROM token WHERE code = ?";

}
