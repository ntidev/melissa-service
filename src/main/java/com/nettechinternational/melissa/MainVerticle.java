package com.nettechinternational.melissa;

import com.nettechinternational.melissa.store.ApplicationService;
import com.nettechinternational.melissa.store.TokenService;
import com.nettechinternational.melissa.store.impl.ApplicationServiceCacheImpl;
import com.nettechinternational.melissa.store.impl.TokenServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        LOG.debug(config());
        validateConfig();

        TokenService tokenService = new TokenServiceImpl(vertx, config());
        TokenService.register(vertx, tokenService);

        ApplicationService applicationService = new ApplicationServiceCacheImpl(vertx, config());
        ApplicationService.register(vertx, applicationService);

        DeploymentOptions doptions = new DeploymentOptions().setConfig(config());

        vertx.deployVerticle(HttpServerVerticle.class.getName(), doptions, res -> {
            if (res.failed()) {
                LOG.error(res.cause());
                startFuture.fail(res.cause());
            } else {
                //  vertx.deployVerticle(TokenWatcherVerticle.class.getName());
                LOG.info("Deployed");
                startFuture.complete();
            }
        });
    }

    public void validateConfig() {
        config().put("url", System.getenv().getOrDefault(Config.ENV_DATABASE_URL, config().getString(Config.CONFIG_DATABASE_URL, Config.DEFAULT_DATABASE_URL)))
                .put("user", System.getenv().getOrDefault(Config.ENV_DATABASE_USER, config().getString(Config.CONFIG_DATABASE_USER, Config.DEFAULT_DATABASE_USER)))
                .put("password", System.getenv().getOrDefault(Config.ENV_DATABASE_PASSWORD, config().getString(Config.CONFIG_DATABASE_PASSWORD, Config.DEFAULT_DATABASE_PASSWORD)))
                .put("max_pool_size", 10)
                .put("max_idle_time", 10)
                .put("min_pool_size", 3)
                .put("driver_class", "com.mysql.jdbc.Driver");
    }

}
