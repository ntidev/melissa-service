package com.nettechinternational.melissa.store.impl;

import com.nettechinternational.melissa.security.ApplicationTokenAuth;
import com.nettechinternational.melissa.store.Application;
import com.nettechinternational.melissa.store.ApplicationService;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Hector Ventura <hventura@syneteksolutions.com>
 */
public class ApplicationTokenAuthImpl implements ApplicationTokenAuth {

    private final ApplicationService appService;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private String rootToken = null;

    public ApplicationTokenAuthImpl(ApplicationService appService, String rootToken) {
        this.appService = appService;
        this.rootToken = rootToken;
    }

    @Override
    public void handle(RoutingContext context) {

        MultiMap headers = context.request().headers();

        if (!headers.contains(HEADER_AUTHORIZATION)) {
            notAuthorized(context);
            return;
        }

        String autherization = headers.get(HEADER_AUTHORIZATION);
        String parts[] = autherization.split(" ");

        if (parts.length != 2) {
            notAuthorized(context);
            return;
        }

        if (rootToken != null && rootToken.equals(parts[1])) {
            Application app = new Application()
                    .setId(rootToken)
                    .setApiToken(rootToken)
                    .setDescription("ROOT USER")
                    .setName("ROOT USER");

            context.put("application", app);
            context.next();
        } else {
            appService.getByApiToken(parts[1], res -> {

                if (res.succeeded()) {
                    Application app = res.result();
                    if (app.isEnabled()) {
                        context.put("application", app);
                        context.next();
                    } else {
                        notAuthorized(context);
                    }
                } else {
                    notAuthorized(context);
                }

            });
        }

    }

    private void notAuthorized(RoutingContext context) {
        context.response().setStatusCode(401)
                .putHeader("Content-Type", "application/json")
                .end();
    }

}
