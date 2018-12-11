package com.nettechinternational.melissa.store;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import java.util.List;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
@ProxyGen
public interface TokenService {

    public static final String SERVICE_ADDRESS = "notification.token.eb-service";

    public static TokenService createProxy(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(SERVICE_ADDRESS);
        return builder.build(TokenService.class);
    }

    public static void register(Vertx vertx, TokenService emailStoreService) {
        ServiceBinder binder = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS);
        binder.register(TokenService.class, emailStoreService);
    }

    @Fluent
    TokenService store(Token token, Handler<AsyncResult<Token>> handler);

    @Fluent
    TokenService get(String id, Handler<AsyncResult<Token>> handler);

    @Fluent
    TokenService getAll(Handler<AsyncResult<List<Token>>> handler);

    @Fluent
    TokenService remove(String token, Handler<AsyncResult<Void>> handler);

    @Fluent
    TokenService getOldTokens(Handler<AsyncResult<List<Token>>> handler);

}
