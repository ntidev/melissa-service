package com.nettechinternational.melissa.security;

import com.nettechinternational.melissa.store.Token;
import com.nettechinternational.melissa.store.TokenService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.List;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class TokenWatcherVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(TokenWatcherVerticle.class);

    // When token gets 2 minutes old and hasn't been used, remove it.
    private TokenService tokenService;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        tokenService = TokenService.createProxy(vertx);
        launchWatcher();

        startFuture.complete();
    }

    private void reschechule() {
        vertx.setTimer(1000 * 30, p -> {
            launchWatcher();
        });
    }

    private void launchWatcher() {
        LOG.info("Launching getOldTokens.....");

        tokenService.getOldTokens(res -> {
            if (res.failed()) {
                LOG.error(res.cause());
                reschechule();
            } else {
                List<Token> tokens = res.result();
                LOG.info("{} old tokens were found.", tokens.size());
                tokens.stream().forEach(t -> {
                    LOG.debug("Old token {}", t.toJson().encode());
                    tokenService.remove(t.getCode(), tr -> {
                        LOG.debug("removed {}", tr.succeeded());
                    });
                });

                reschechule();
            }
        });

    }
}
