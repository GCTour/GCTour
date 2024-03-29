package client;

import model.Geocache;
import model.Waypoint;
import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.jdbc.constraints.SQLConstraint;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Facet;
import sirius.web.controller.Message;
import sirius.web.controller.Page;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.UserContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Register
public class GeocacheController extends BizController {

    /**
     * Shows a detailed view of one {@link Geocache}
     *
     * @param ctx the current request
     */
    @LoginRequired
    @Routed("/geocache/:1")
    public void showGeocache(WebContext ctx, String gcCode) {
        Geocache geocache = oma.select(Geocache.class).eq(Geocache.GC_CODE, gcCode.toUpperCase()).queryFirst();
        if (geocache == null) {
            UserContext.message(Message.error()
                                       .withHTMLMessage("Der Geocache mit dem Code '" + gcCode + "' existiert nicht."));
            ctx.respondWith().redirectPermanently("/geocaches");
            return;
        }
        List<Waypoint> waypoints = geocache.getWaypoints();
        Tuple<Tuple<Double, Double>, Tuple<Double, Double>> mapBounds = getMapBounds(waypoints);

        ctx.respondWith()
           .template("/templates/admin/geocache.html.pasta", geocache, mapBounds.getFirst(), mapBounds.getSecond());
    }

    /**
     * Shows a list of all {@link Geocache geocaches}
     *
     * @param ctx the current request
     */
    @LoginRequired
    @Routed("/geocaches")
    public void showGeocaches(WebContext ctx) {
        Amount minLat = Amount.ofMachineString(ctx.getParameter("minLat"));
        Amount maxLat = Amount.ofMachineString(ctx.getParameter("maxLat"));
        Amount minLon = Amount.ofMachineString(ctx.getParameter("minLon"));
        Amount maxLon = Amount.ofMachineString(ctx.getParameter("maxLon"));
        SmartQuery<Geocache> smartQuery = oma.select(Geocache.class);

        smartQuery.orderAsc(Geocache.GC_ID);

        SQLPageHelper<Geocache> pageHelper = SQLPageHelper.withQuery(smartQuery)
                                                          .withContext(ctx)
                                                          .withSearchFields(QueryField.contains(Geocache.GC_CODE),
                                                                            QueryField.contains(Geocache.NAME));
        Facet dFacet = new Facet("D-Wertung", Geocache.DIFFICULTY.getName(), ctx.get("difficulty").asString(), null);
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

        Facet tFacet = new Facet("T-Wertung", Geocache.TERRAIN.getName(), ctx.get("terrain").asString(), null);
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

        Page<Geocache> page = pageHelper.asPage().withPageSize(999).withTotalItems((int) smartQuery.count());

        List<Waypoint> waypoints = new ArrayList<>(Collections.emptyList());
        page.getItems().forEach(geocache -> waypoints.add(geocache.getListingWaypoint()));
        Tuple<Tuple<Double, Double>, Tuple<Double, Double>> mapBounds = getMapBounds(waypoints);

        ctx.respondWith()
           .template("/templates/admin/geocaches.html.pasta", page, mapBounds.getFirst(), mapBounds.getSecond());
    }

    private Tuple<Tuple<Double, Double>, Tuple<Double, Double>> getMapBounds(List<Waypoint> waypoints) {
        double nwLatitude = 0;
        double nwLongitude = 0;
        double seLatitude = 0;
        double seLongitude = 0;
        if (waypoints.isEmpty()) {
            return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
        }
        Waypoint firstWaypoint = waypoints.get(0);
        nwLatitude = firstWaypoint.getLatitude().getAmount().doubleValue();
        nwLongitude = firstWaypoint.getLongitude().getAmount().doubleValue();
        seLatitude = firstWaypoint.getLatitude().getAmount().doubleValue();
        seLongitude = firstWaypoint.getLongitude().getAmount().doubleValue();

        if (waypoints.size() == 1) {
            return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
        }

        for (Waypoint waypoint : waypoints) {
            double latitude = waypoint.getLatitude().getAmount().doubleValue();
            double longitude = waypoint.getLongitude().getAmount().doubleValue();
            if (latitude > nwLatitude) {
                nwLatitude = latitude;
            }
            if (longitude < nwLongitude) {
                nwLongitude = longitude;
            }
            if (latitude < seLatitude) {
                seLatitude = latitude;
            }
            if (longitude > seLongitude) {
                seLongitude = longitude;
            }
        }
        return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
    }
}
