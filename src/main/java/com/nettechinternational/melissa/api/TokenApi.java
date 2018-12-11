package com.nettechinternational.melissa.api;

import com.nettechinternational.melissa.store.Application;
import com.nettechinternational.melissa.store.Token;
import com.nettechinternational.melissa.store.TokenService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class TokenApi {

    private final Vertx vertx;
    private final TokenService tokenService;

    public TokenApi(Vertx vertx) {
        this.vertx = vertx;
        tokenService = TokenService.createProxy(vertx);
    }

    public Router gerRouter() {
        Router router = Router.router(vertx);
        router.post("/").handler(this::createToken);
        router.get("/").handler(this::getTokens);
        router.delete("/:token").handler(this::deleteToken);
        return router;
    }

    private void createToken(RoutingContext context) {

        JsonObject body = context.getBodyAsJson();
        Token token = new Token(body);

        Application app = context.get("application");
        token.setApplication(app);

        tokenService.store(token, res -> {
            if (res.failed()) {
                context.response()
                        .setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(res.cause().getMessage());
            } else {

                Token t = res.result();
                JsonObject response = new JsonObject()
                        .put("tokenCode", t.getCode())
                        .put("createdAt", t.getCreatedAt());

                context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(response.toBuffer());
            }
        });

    }

    private void deleteToken(RoutingContext context) {
        String token = context.request().getParam("token");

        tokenService.remove(token, res -> {
            if (res.failed()) {
                context.response()
                        .setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(res.cause().getMessage());
            } else {
                context.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(200)
                        .end();
            }
        });
    }

    private void getTokens(RoutingContext context) {
        tokenService.getAll(res -> {
            if (res.failed()) {
                context.response()
                        .setStatusCode(400)
                        .putHeader("Content-Type", "application/json")
                        .end(res.cause().getMessage());
            } else {
                JsonArray data = new JsonArray();
                res.result().stream().map(Token::toJson).forEach(data::add);
                context.response()
                        .putHeader("Content-Type", "application/json")
                        .end(data.toBuffer());
            }
        });
    }
}
