package com.nettechinternational.melissa.store.impl;

import com.nettechinternational.melissa.store.Application;
import com.nettechinternational.melissa.store.ApplicationService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class ApplicationServiceCacheImpl extends ApplicationServiceImpl {

    private final Map<String, Application> APPS = new ConcurrentHashMap();

    public ApplicationServiceCacheImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public ApplicationService store(Application application, Handler<AsyncResult<Application>> handler) {

        super.store(application, res -> {
            if (res.succeeded()) {
                APPS.put(res.result().getId(), res.result());
                handler.handle(Future.succeededFuture(res.result()));
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });

        return this;
    }

    @Override
    public ApplicationService update(Application application, Handler<AsyncResult<Application>> handler) {

        super.update(application, res -> {
            if (res.succeeded()) {
                APPS.put(application.getId(), res.result());
                handler.handle(Future.succeededFuture(res.result()));
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });

        return this;
    }

    @Override
    public ApplicationService get(String id, Handler<AsyncResult<Application>> handler) {

        if (APPS.containsKey(id)) {
            Application app = APPS.get(id);
            handler.handle(Future.succeededFuture(app));
        } else {
            super.get(id, res -> {
                if (res.succeeded()) {
                    APPS.put(res.result().getId(), res.result());
                    handler.handle(Future.succeededFuture(res.result()));
                } else {
                    handler.handle(Future.failedFuture(res.cause()));
                }
            });
        }
        return this;
    }

    @Override
    public ApplicationService getAll(Handler<AsyncResult<List<Application>>> handler) {
        super.getAll(handler);
//        retrieveAll(FETCH_ALL_STATEMENT)
//                .map(rows -> rows.stream().map(Application::new)
//                .collect(Collectors.toList())).setHandler(handler);

        return this;
    }

    @Override
    public ApplicationService getByApiToken(String token, Handler<AsyncResult<Application>> handler) {

        Optional<Application> appOpt = APPS.values()
                .stream()
                .filter(a -> a.getApiToken().equals(token))
                .findFirst();

        if (appOpt.isPresent()) {
            Application app = appOpt.get();
            handler.handle(Future.succeededFuture(app));
        } else {
            super.getByApiToken(token, res -> {
                if (res.succeeded()) {
                    APPS.put(res.result().getId(), res.result());
                    handler.handle(Future.succeededFuture(res.result()));
                } else {
                    handler.handle(Future.failedFuture(res.cause()));
                }
            });
        }

        return this;
    }

}
