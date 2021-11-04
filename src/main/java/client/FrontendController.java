package client;

import model.Tour;
import sirius.biz.web.BizController;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Page;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;

/**
 * Controller for the main public accessible routes
 */

@Register
public class FrontendController extends BizController {

    @Routed("/")
    public void index(WebContext ctx) {
        String webcode = ctx.getParameter("query");
        if (webcode != null && !webcode.isEmpty()) {
            ctx.respondWith().redirectToGet("/tour/" + webcode);
            return;
        }
        ctx.respondWith().template("/templates/index.html.pasta", new Page<Tour>());
    }

    @Routed("/install")
    public void install(WebContext ctx) {
        ctx.respondWith().redirectToGet("https://www.gctour.de/files/gctour.user.js");
    }

    @Routed("/imprint")
    public void imprint(WebContext ctx) {
        ctx.respondWith().redirectToGet("https://www.gctour.de/contact");
    }
}
