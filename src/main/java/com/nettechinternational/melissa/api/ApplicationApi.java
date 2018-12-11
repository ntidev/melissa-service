package com.nettechinternational.melissa.api;

import com.nettechinternational.melissa.store.Application;
import com.nettechinternational.melissa.store.ApplicationService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class ApplicationApi {

    private final Vertx vertx;
    private final ApplicationService applicationService;

    public ApplicationApi(Vertx vertx) {
        this.vertx = vertx;
        applicationService = ApplicationService.createProxy(vertx);
    }

    public Router gerRouter() {
        Router router = Router.router(vertx);
        router.post("/").handler(this::createApp);
        router.get("/").handler(this::getApps);
        router.get("/:id").handler(this::getApp);
        return router;
    }

    private void getApp(RoutingContext context) {

        String idOrName = context.request().getParam("id");

        if ("me".equals(idOrName)) {
            Application app = context.get("application");
            context.response()
                    .putHeader("Content-Type", "application/json")
                    .end(app.toJson().toBuffer());
        } else {
            applicationService.get(idOrName, res -> {
                if (res.failed()) {
                    context.response()
                            .setStatusCode(404)
                            .putHeader("Content-Type", "application/json")
                            .end(res.cause().getMessage());
                } else {
                    context.response()
                            .putHeader("Content-Type", "application/json")
                            .end(res.result().toJson().toBuffer());
                }
            });
        }
    }

    private void getApps(RoutingContext context) {

        applicationService.getAll(res -> {
            if (res.failed()) {
                context.response()
                        .setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(res.cause().getMessage());
            } else {
                JsonArray data = new JsonArray();
                res.result().stream().map(Application::toJson).forEach(data::add);
                context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(data.toBuffer());
            }
        });
    }

    private void createApp(RoutingContext context) {

        JsonObject body = context.getBodyAsJson();
        Application token = new Application(body);

        if ("me".equals(token.getName())) {
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(new JsonObject().put("error", "The name 'me' can't be used.").toBuffer());
            return;
        }

        applicationService.store(token, res -> {
            if (res.failed()) {
                context.response()
                        .setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(res.cause().getMessage());
            } else {
                Application t = res.result();
                JsonObject response = new JsonObject()
                        .put("apiToken", t.getApiToken());

                context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(response.toBuffer());
            }
        });

    }
}
