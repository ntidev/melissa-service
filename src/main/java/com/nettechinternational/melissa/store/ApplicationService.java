package com.nettechinternational.melissa.store;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
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
@VertxGen
@ProxyGen
public interface ApplicationService {

    public static final String SERVICE_ADDRESS = "notification.application.eb-service";

    @GenIgnore
    public static ApplicationService createProxy(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(SERVICE_ADDRESS);
        return builder.build(ApplicationService.class);
    }

    @GenIgnore
    public static void register(Vertx vertx, ApplicationService service) {
        ServiceBinder binder = new ServiceBinder(vertx).setAddress(SERVICE_ADDRESS);
        binder.register(ApplicationService.class, service);
    }

    @Fluent
    ApplicationService store(Application application, Handler<AsyncResult<Application>> handler);

    @Fluent
    ApplicationService update(Application application, Handler<AsyncResult<Application>> handler);

    @Fluent
    ApplicationService get(String id, Handler<AsyncResult<Application>> handler);

    @Fluent
    ApplicationService getAll(Handler<AsyncResult<List<Application>>> handler);

    @Fluent
    ApplicationService getByApiToken(String token, Handler<AsyncResult<Application>> handler);

}
