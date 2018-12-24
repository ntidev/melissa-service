package com.nettechinternational.melissa;

import com.nettechinternational.melissa.api.ApplicationApi;
import com.nettechinternational.melissa.api.NotifyApi;
import com.nettechinternational.melissa.api.TokenApi;
import com.nettechinternational.melissa.security.ApplicationTokenAuth;
import com.nettechinternational.melissa.store.ApplicationService;
import com.nettechinternational.melissa.store.Token;
import com.nettechinternational.melissa.store.TokenService;
import com.nettechinternational.melissa.utils.ConfigUtils;
import com.nettechinternational.melissa.utils.HttpUtils;
import com.nettechinternational.melissa.utils.TokenGenerator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServerVerticle.class);

    private static final int KB = 1024;
    private static final int MB = 1024 * KB;
    private boolean secureMode = false;
    private TokenService tokenService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        tokenService = TokenService.createProxy(vertx);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setBodyLimit(2 * MB));

        // Health Check
        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
        healthCheckHandler.register("melissa.service", future -> future.complete(Status.OK()));
        router.get("/health").handler(healthCheckHandler);

        NotifyApi notifyRouter = new NotifyApi(vertx);
        TokenApi tokenRouter = new TokenApi(vertx);
        ApplicationApi applicationRouter = new ApplicationApi(vertx);

        String rootToken = System.getenv(Config.ENV_ROOT_TOKEN);
        LOG.debug("Env ROOT_TOKEN={}", rootToken);

        if (rootToken == null || rootToken.isEmpty()) {
            rootToken = config().getString(Config.CONFIG_API_ROOT_TOKEN);
            LOG.debug("Config ROOT_TOKEN={}", rootToken);

            if (rootToken == null) {
                rootToken = TokenGenerator.get();
            }
        }

        ApplicationService appService = ApplicationService.createProxy(vertx);
        ApplicationTokenAuth auth = ApplicationTokenAuth.create(appService, rootToken);

        router.route("/api/*")
                .consumes("application/json")
                .produces("application/json")
                .handler(auth);
        
        notifyRouter.addRoutes(router);
        tokenRouter.addRoutes(router);
        applicationRouter.addRoutes(router);

//        router.mountSubRouter("/api/notify", notifyRouter.getRouter());
//        router.mountSubRouter("/api/token", tokenRouter.gerRouter());
//        router.mountSubRouter("/api/application", applicationRouter.gerRouter());

        vertx.createHttpServer(createServerOptions())
                .requestHandler(router::accept)
                .websocketHandler(createWebSocketHandler())
                .listen(Config.DEFAULT_HTTP_PORT, res -> {
                    if (res.succeeded()) {

                        // register consumer
                        registerConsumer();
                        LOG.info("Server is running on port http{}://0.0.0.0:{}", (secureMode ? "s" : ""), Config.DEFAULT_HTTP_PORT);
                        LOG.info("Deployment successfully.");
                        startFuture.complete();
                    } else {
                        LOG.error(res.cause());
                        startFuture.fail(res.cause());
                    }
                });

    }

    private Handler<ServerWebSocket> createWebSocketHandler() {
        return serverWebSocket -> {

//            if (!serverWebSocket.path().equals("/")) {
//                serverWebSocket.reject();
//            }
            String tokenCode = HttpUtils.getToken(serverWebSocket.query());
            Client client = new Client(serverWebSocket.textHandlerID(), tokenCode);
            LOG.debug("Socket Client connecting with tokenCode = {}", tokenCode);

            authenticateClient(client, res -> {

                if (res.succeeded()) {
                    LOG.debug("Socket Client connected with tokenCode = {}", tokenCode);
                    serverWebSocket.closeHandler(v -> {
                        LOG.info("The user has gone off");
                        ConnectedClientStore.get().remove(client);

                        // publish connected users.
                        vertx.eventBus().publish("melissa.events", new JsonObject()
                                .put("type", "disconnected-user")
                                .put("user", client.getToken().getUsername())
                                .put("application", client.getToken().getApplication().toJson())
                        );
                    });
                } else {
                    LOG.debug("Error login - {}", res.cause().getMessage());
                    JsonObject message = new JsonObject()
                            .put("status", "ko")
                            .put("error", "BadToken")
                            .put("message", "Bad auth token");
                    serverWebSocket.writeTextMessage(message.encode());
                    serverWebSocket.close();
                }
            });
        };
    }

    private void authenticateClient(Client client, Handler<AsyncResult<Void>> handler) {

        if (client.getTokenCode().isPresent()) {
            tokenService.get(client.getTokenCode().get(), res -> {

                if (res.failed()) {
                    // publish connected users.
                    vertx.eventBus().publish("melissa.events", new JsonObject()
                            .put("type", "bad-login")
                            .put("error", res.cause().getMessage()));

                    handler.handle(Future.failedFuture(res.cause().getMessage()));
                } else {
                    Token token = res.result();
                    client.setToken(token);

                    JsonObject message = new JsonObject()
                            .put("status", "ok")
                            .put("username", token.getUsername())
                            .put("app", token.getApplication().getName());

                    vertx.eventBus().send(client.getSocketId(), message.encode());
                    message.put("", secureMode);

                    // publish connected users.
                    vertx.eventBus().publish("melissa.events", new JsonObject()
                            .put("type", "connected-user")
                            .put("user", token.getUsername())
                            .put("application", token.getApplication().toJson())
                    );

                    ConnectedClientStore.get().put(client);
                    handler.handle(Future.succeededFuture());
                }
            });
        } else {
            handler.handle(Future.failedFuture("Token is missing"));
        }
    }

    private HttpServerOptions createServerOptions() {

        String configDir = ConfigUtils.getEnv(Config.ENV_CONFIG_DIR, Config.DEFAULT_CONFIG_DIR);

        String certPath = String.format("%s/%s", configDir, Config.SSL_CERT_FILE_NAME);
        String keyPath = String.format("%s/%s", configDir, Config.SSL_KEY_FILE_NAME);

        HttpServerOptions httpServerOptions = new HttpServerOptions();

        if (vertx.fileSystem().existsBlocking(certPath)
                && vertx.fileSystem().existsBlocking(keyPath)) {

            LOG.info("Enabled HTTPS");
            LOG.info("Using SSL cert file {}", certPath);
            LOG.info("Using SSL key file {}", keyPath);

            httpServerOptions.setSsl(true)
                    .setKeyCertOptions(new PemKeyCertOptions()
                            .setCertPath(certPath)
                            .setKeyPath(keyPath));

            secureMode = true;
        } else {
            LOG.warn("The server is not running on secure mode!");
        }

        return httpServerOptions;
    }

    private void registerConsumer() {

        vertx.eventBus().<JsonObject>consumer("notification.notify", res -> {

            JsonObject message = res.body();
            LOG.info("Message: {}", message.encodePrettily());

            // search user.
            boolean hasUsers = message.containsKey("users");
            boolean hasApps = message.containsKey("apps");
            boolean hasGroups = message.containsKey("groups");

            Predicate<Client> predicateClient = client -> {

                boolean userExist = true;
                JsonArray users = message.getJsonArray("users");
                if (hasUsers && !users.isEmpty()) {
                    userExist = users.stream()
                            .map(u -> (String) u)
                            .anyMatch(u -> client.getToken().getUsername().equals(u));
                }

                boolean appExist = true;
                JsonArray apps = message.getJsonArray("apps");
                if (hasApps && !apps.isEmpty()) {
                    appExist = apps.stream()
                            .map(app -> (String) app)
                            .anyMatch(app -> app.equals(client.getToken().getApplication().getName()));
                }

                //  if (hasGroups)
                return userExist && appExist;
            };

            List<Client> clients = ConnectedClientStore.get().filterClients(predicateClient);

            JsonObject resume = new JsonObject()
                    .put("totalUsers", clients.size());

            res.reply(resume);

            if (!clients.isEmpty()) {
                JsonObject mes = new JsonObject()
                        .put("sendFrom", message.getJsonObject("fromApplication").getString("name"))
                        .put("data", message.getValue("data"))
                        .put("timestamp", new Date().getTime());

                // Notify all clients;
                clients.parallelStream()
                        .forEach(c -> {
                            LOG.info("Sending message to {}", c.getToken().getUsername());
                            vertx.eventBus().send(c.getSocketId(), mes.encode());
                        });
            }
        });
    }
}
