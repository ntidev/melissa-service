package com.nettechinternational.melissa.store.impl;

import com.nettechinternational.melissa.store.Application;
import com.nettechinternational.melissa.store.ApplicationService;
import com.nettechinternational.melissa.utils.JdbcRepositoryWrapper;
import com.nettechinternational.melissa.utils.TokenGenerator;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class ApplicationServiceImpl extends JdbcRepositoryWrapper implements ApplicationService {

    public ApplicationServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public ApplicationService store(Application application, Handler<AsyncResult<Application>> handler) {

        // generate Id and TokenApi
        String token = TokenGenerator.get();
        String id = UUID.randomUUID().toString();

        application.setApiToken(null)
                .setId(null);
        application.setCreatedAt(new Date().getTime());

        JsonArray params = new JsonArray()
                .add(id)
                .add(application.getName())
                .add(emptyIfNull(application.getDescription()))
                .add(application.isEnabled())
                .add(token)
                .add(application.getCreatedAt());

        executeNoResult(INSERT_STATEMENT, params, res -> {
            if (res.succeeded()) {
                application.setId(id).setApiToken(token);
                handler.handle(Future.succeededFuture(application));
            } else {
                if (res.cause() instanceof ReplyException) {
                    handler.handle(Future.failedFuture(res.cause()));
                } else {
                    handler.handle(Future.failedFuture(res.cause().getMessage()));
                }
            }
        });

        return this;
    }

    @Override
    public ApplicationService update(Application application, Handler<AsyncResult<Application>> handler) {

        if (application.getApiToken() == null) {
            String token = TokenGenerator.get();
            application.setApiToken(token);
        }

        JsonArray params = new JsonArray()
                .add(application.getName())
                .add(emptyIfNull(application.getDescription()))
                .add(application.isEnabled())
                .add(application.getApiToken())
                .add(application.getId());

        executeNoResult(UPDATE_STATEMENT, params, res -> {
            if (res.succeeded()) {
                handler.handle(Future.succeededFuture(application));
            } else {
                if (res.cause() instanceof ReplyException) {
                    handler.handle(Future.failedFuture(res.cause()));
                } else {
                    handler.handle(Future.failedFuture("333"));
                }
            }
        });

        return this;
    }

    @Override
    public ApplicationService get(String id, Handler<AsyncResult<Application>> handler) {
        retrieveOne(FETCH_BY_ID_STATEMENT, id, id)
                .map(Application::new)
                .setHandler(handler);
        return this;
    }

    @Override
    public ApplicationService getAll(Handler<AsyncResult<List<Application>>> handler) {
        retrieveAll(FETCH_ALL_STATEMENT)
                .map(rows -> rows.stream().map(Application::new)
                .collect(Collectors.toList())).setHandler(handler);

        return this;
    }

    @Override
    public ApplicationService getByApiToken(String token, Handler<AsyncResult<Application>> handler) {
        retrieveOne(FETCH_BY_TOKEN_STATEMENT, token)
                .map(Application::new)
                .setHandler(handler);
        return this;
    }

    private static final String INSERT_STATEMENT = "INSERT INTO application (id, name, description, enabled, api_token, created_at) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_STATEMENT = "UPDATE application SET name = ?, description = ?, enabled = ?, api_token = ? WHERE id = ?";
    private static final String FETCH_ALL_STATEMENT = "SELECT id, name, description, enabled, api_token AS apiToken, created_at AS createdAt FROM application";
    private static final String FETCH_BY_ID_STATEMENT = FETCH_ALL_STATEMENT + " WHERE id = ? or name = ? ";
    private static final String FETCH_BY_TOKEN_STATEMENT = FETCH_ALL_STATEMENT + " WHERE api_token = ?";

}
