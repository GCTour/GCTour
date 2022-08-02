package client;

import model.Geocache;
import sirius.biz.codelists.jdbc.SQLCodeLists;
import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.jdbc.constraints.SQLConstraint;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Facet;
import sirius.web.controller.Page;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides a route to login to the backend page
 */
@Deprecated
@Register
public class AdminController extends BizController {

    @Part
    private static SQLCodeLists codeLists;

    /**
     * Simple route that calls the pasta template containing the name chooser.
     *
     * @param webContext the context of the web request
     */
    @LoginRequired
    @Routed("/admin")
    public void admin(@Nonnull WebContext webContext) {
        Amount minLat = Amount.ofMachineString(webContext.getParameter("minLat"));
        Amount maxLat = Amount.ofMachineString(webContext.getParameter("maxLat"));
        Amount minLon = Amount.ofMachineString(webContext.getParameter("minLon"));
        Amount maxLon = Amount.ofMachineString(webContext.getParameter("maxLon"));
        SmartQuery<Geocache> smartQuery = oma.select(Geocache.class);

        smartQuery.orderAsc(Geocache.GC_ID);

        SQLPageHelper<Geocache> pageHelper = SQLPageHelper.withQuery(smartQuery)
                                                               .withContext(webContext)
                                                               .withSearchFields(QueryField.contains(Geocache.GC_CODE),
                                                                                 QueryField.contains(Geocache.NAME));
        Facet dFacet = new Facet("D-Wertung",
                                 Geocache.DIFFICULTY.getName(),
                                 webContext.get("difficulty").asString(),
                                 null);
        dFacet.addItem("1.0", "1.0", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.ONE).count());
        dFacet.addItem("1.5", "1.5", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(1.5)).count());
        dFacet.addItem("2.0", "2.0", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(2)).count());
        dFacet.addItem("2.5", "2.5", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(2.5)).count());
        dFacet.addItem("3.0", "3.0", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(3)).count());
        dFacet.addItem("3.5", "3.5", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(3.5)).count());
        dFacet.addItem("4.0", "4.0", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(4)).count());
        dFacet.addItem("4.5", "4.5", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(4.5)).count());
        dFacet.addItem("5.0", "5.0", (int) smartQuery.copy().eq(Geocache.DIFFICULTY, Amount.of(5)).count());
        pageHelper.addFilterFacet(dFacet);

        Facet tFacet =
                new Facet("T-Wertung", Geocache.TERRAIN.getName(), webContext.get("terrain").asString(), null);
        tFacet.addItem("1.0", "1.0", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.ONE).count());
        tFacet.addItem("1.5", "1.5", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(1.5)).count());
        tFacet.addItem("2.0", "2.0", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(2)).count());
        tFacet.addItem("2.5", "2.5", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(2.5)).count());
        tFacet.addItem("3.0", "3.0", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(3)).count());
        tFacet.addItem("3.5", "3.5", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(3.5)).count());
        tFacet.addItem("4.0", "4.0", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(4)).count());
        tFacet.addItem("4.5", "4.5", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(4.5)).count());
        tFacet.addItem("5.0", "5.0", (int) smartQuery.copy().eq(Geocache.TERRAIN, Amount.of(5)).count());
        pageHelper.addFilterFacet(tFacet);

        pageHelper.addQueryFacet(Geocache.TYPE.getName(),
                                 "Typ",
                                 q -> q.copy().distinctFields(Geocache.TYPE, Geocache.TYPE).asSQLQuery());

        List<SQLConstraint> constraints = new ArrayList<>();
//        constraints.add(smartQuery.filters().gte(Geocache.LATITUDE, minLat));
//        constraints.add(smartQuery.filters().lte(Geocache.LATITUDE, maxLat));
//        constraints.add(smartQuery.filters().gte(Geocache.LONGITUDE, minLon));
//        constraints.add(smartQuery.filters().lte(Geocache.LONGITUDE, maxLon));

        smartQuery.where(smartQuery.filters().and(constraints));

        Page<Geocache> page =
                pageHelper.withPageSize(999).asPage().withPageSize(999).withTotalItems((int) smartQuery.count());

        webContext.respondWith().template("/templates/admin/geocaches.html.pasta", page, new Tuple<>(54.00, 8.00), new Tuple<>(45.00, 12.00));
    }
}
