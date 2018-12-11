package com.nettechinternational.melissa.api;

import com.nettechinternational.melissa.store.Application;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class NotifyApi {

    private final Vertx vertx;

    public NotifyApi(Vertx vertx) {
        this.vertx = vertx;
    }

    public Router getRouter() {
        Router router = Router.router(vertx);
        router.post("/").handler(this::notfy);
        return router;
    }

    private void notfy(RoutingContext context) {

        JsonObject body = context.getBodyAsJson();
        Application app = context.get("application");

        JsonObject messageNotify = new JsonObject()
                .put("fromApplication", app.toJson())
                .put("data", body.getValue("data"));

        if (body.containsKey("users")) {
            messageNotify.put("users", body.getJsonArray("users"));
        }

        if (body.containsKey("apps")) {
            messageNotify.put("apps", body.getJsonArray("apps"));
        }

        if (body.containsKey("groups")) {
            messageNotify.put("groups", body.getJsonArray("groups"));
        }

        // publish connected users.
        vertx.eventBus().publish("melissa.events", new JsonObject()
                .put("type", "new-notification")
                .put("fromApp", app.toJson())
                .put("message", messageNotify)
        );

        vertx.eventBus().<JsonObject>send("notification.notify", messageNotify, rp -> {
            if (rp.succeeded()) {
                context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(rp.result().body().toBuffer());
            } else {
                context.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(400).end();
            }
        });
    }
}
