package com.nettechinternational.melissa.security;

import com.nettechinternational.melissa.store.ApplicationService;
import com.nettechinternational.melissa.store.impl.ApplicationTokenAuthImpl;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public interface ApplicationTokenAuth extends Handler<RoutingContext> {

    static ApplicationTokenAuth create(ApplicationService appService, String rootToken) {
        return new ApplicationTokenAuthImpl(appService, rootToken);
    }

    static ApplicationTokenAuth create(Vertx vertx) {
        ApplicationService ap = ApplicationService.createProxy(vertx);
        return new ApplicationTokenAuthImpl(ap, null);
    }
}
