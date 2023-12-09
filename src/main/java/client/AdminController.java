package client;

import model.Geocache;
import model.GeocacheSize;
import model.GeocacheType;
import model.Tour;
import model.Waypoint;
import model.WaypointInTour;
import model.WaypointType;
import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.jdbc.constraints.SQLConstraint;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Register;
import sirius.web.controller.Page;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Provides admin-routes for testing
 */
@Register
public class AdminController extends BizController {

    @LoginRequired
    @Routed("/insert-empty-tour")
    public void testEmptyTour(WebContext ctx) {
        Tour tour = new Tour();
        tour.setName("Empty Test-Tour");
        oma.update(tour);
        ctx.respondWith().redirectToGet("/tours");
    }

    @LoginRequired
    @Routed("/insert-small-tour")
    public void testSmallTour(WebContext ctx) {
        Random random = new Random();
        Tour tour = new Tour();
        tour.setName("Test-Tour-Klein" + random.nextInt());
        oma.update(tour);

        for (int i = 0; i < 12; i++) {
            Geocache geocache = new Geocache();
            geocache.setGcCode(Geocache.convertIdToGcCode(i + 1 + random.nextInt(999999)));
            geocache.setName("Test-Cache-Klein" + (i + 1 + random.nextInt(999999)));
            geocache.setDifficulty(Amount.of(5));
            geocache.setTerrain(Amount.of(5));
            geocache.setSize(GeocacheSize.MICRO);
            geocache.setType(GeocacheType.TRADITIONAL);
            oma.update(geocache);

            Waypoint original = new Waypoint();
            original.setLatitude(Amount.of(random.nextDouble() + 48.5));
            original.setLongitude(Amount.of(random.nextDouble() + 9.3));
            original.setWaypointType(WaypointType.ORIGINAL);
            original.getGeocache().setValue(geocache);
            oma.update(original);

            WaypointInTour waypointInTour = new WaypointInTour();
            waypointInTour.setPosition(i + 1);
            waypointInTour.getWaypoint().setValue(original);
            waypointInTour.getTour().setValue(tour);
            oma.update(waypointInTour);
        }
        ctx.respondWith().redirectToGet("/tours");
    }

    @LoginRequired
    @Routed("/insert-small-wp-tour")
    public void testSmallWpTour(WebContext ctx) {
        Random random = new Random();
        Tour tour = new Tour();
        tour.setName("Test-Tour-Klein-Mit-WP" + random.nextInt());
        oma.update(tour);

        for (int i = 0; i < 12; i++) {
            Geocache geocache = new Geocache();
            geocache.setGcCode(Geocache.convertIdToGcCode(i + 1 + random.nextInt(999999)));
            geocache.setName("Test-Cache-Klein" + (i + 1 + random.nextInt(999999)));
            geocache.setDifficulty(Amount.of(5));
            geocache.setTerrain(Amount.of(5));
            geocache.setSize(GeocacheSize.MICRO);
            geocache.setType(GeocacheType.TRADITIONAL);
            oma.update(geocache);

            Waypoint original = new Waypoint();
            original.setLatitude(Amount.of(random.nextDouble() + 48.5));
            original.setLongitude(Amount.of(random.nextDouble() + 9.3));
            original.setWaypointType(WaypointType.ORIGINAL);
            original.getGeocache().setValue(geocache);
            oma.update(original);

            Waypoint s1 = new Waypoint();
            s1.setLatitude(Amount.of(random.nextDouble() + 48.5));
            s1.setLongitude(Amount.of(random.nextDouble() + 9.3));
            s1.setWaypointType(WaypointType.VIRTUAL_STAGE);
            s1.getGeocache().setValue(geocache);
            oma.update(s1);

            Waypoint p = new Waypoint();
            p.setLatitude(Amount.of(random.nextDouble() + 48.5));
            p.setLongitude(Amount.of(random.nextDouble() + 9.3));
            p.setWaypointType(WaypointType.PARKING_AREA);
            p.getGeocache().setValue(geocache);
            oma.update(p);

            Waypoint s2 = new Waypoint();
            s2.setLatitude(Amount.of(random.nextDouble() + 48.5));
            s2.setLongitude(Amount.of(random.nextDouble() + 9.3));
            s2.setWaypointType(WaypointType.PHYSICAL_STAGE);
            s2.getGeocache().setValue(geocache);
            oma.update(s2);

            Waypoint s3 = new Waypoint();
            s3.setLatitude(Amount.of(random.nextDouble() + 48.5));
            s3.setLongitude(Amount.of(random.nextDouble() + 9.3));
            s3.setWaypointType(WaypointType.TRAILHEAD);
            s3.getGeocache().setValue(geocache);
            oma.update(s3);

            Waypoint own = new Waypoint();
            own.setLatitude(Amount.of(random.nextDouble() + 48.5));
            own.setLongitude(Amount.of(random.nextDouble() + 9.3));
            own.setWaypointType(WaypointType.ORIGINAL);
            oma.update(own);

            WaypointInTour waypointInTour = new WaypointInTour();
            waypointInTour.setPosition(i + 1);
            waypointInTour.getWaypoint().setValue(original);
            waypointInTour.getTour().setValue(tour);
            oma.update(waypointInTour);

            i++;

            WaypointInTour pinTour = new WaypointInTour();
            pinTour.setPosition(i + 1);
            pinTour.getWaypoint().setValue(p);
            pinTour.getTour().setValue(tour);
            oma.update(pinTour);

            i++;

            WaypointInTour s1inTour = new WaypointInTour();
            s1inTour.setPosition(i + 1);
            s1inTour.getWaypoint().setValue(s1);
            s1inTour.getTour().setValue(tour);
            oma.update(s1inTour);

            i++;

            WaypointInTour s2inTour = new WaypointInTour();
            s2inTour.setPosition(i + 1);
            s2inTour.getWaypoint().setValue(s2);
            s2inTour.getTour().setValue(tour);
            oma.update(s2inTour);

            i++;

            WaypointInTour s3InTour = new WaypointInTour();
            s3InTour.setPosition(i + 1);
            s3InTour.getWaypoint().setValue(s3);
            s3InTour.getTour().setValue(tour);
            oma.update(s3InTour);

            i++;

            WaypointInTour ownInTour = new WaypointInTour();
            ownInTour.setPosition(i + 1);
            ownInTour.getWaypoint().setValue(own);
            ownInTour.getTour().setValue(tour);
            oma.update(ownInTour);

            i++;
        }
        ctx.respondWith().redirectToGet("/tours");
    }

    @LoginRequired
    @Routed("/insert-big-tour")
    public void testBigTour(WebContext ctx) {
        Random random = new Random();
        Tour tour = new Tour();
        tour.setName("Test-Tour");
        oma.update(tour);

        for (int i = 0; i < 1234; i++) {
            Geocache geocache = new Geocache();
            geocache.setGcCode(Geocache.convertIdToGcCode(i + 1 + random.nextInt(999999)));
            geocache.setName("Test-Cache" + (i + 1 + random.nextInt(999999)));
            geocache.setDifficulty(Amount.of(5));
            geocache.setTerrain(Amount.of(5));
            geocache.setSize(GeocacheSize.MICRO);
            geocache.setType(GeocacheType.TRADITIONAL);
            oma.update(geocache);

            Waypoint original = new Waypoint();
            original.setLatitude(Amount.of(random.nextDouble() + 48.5));
            original.setLongitude(Amount.of(random.nextDouble() + 9.3));
            original.setWaypointType(WaypointType.ORIGINAL);
            original.getGeocache().setValue(geocache);
            oma.update(original);

            WaypointInTour waypointInTour = new WaypointInTour();
            waypointInTour.setPosition(i + 1);
            waypointInTour.getWaypoint().setValue(original);
            waypointInTour.getTour().setValue(tour);
            oma.update(waypointInTour);
        }
        ctx.respondWith().redirectToGet("/tours");
    }

    /**
     * Simple route that calls the pasta template containing the name chooser.
     *
     * @param ctx the context of the web request
     */
    @LoginRequired
    @Routed("/admin")
    public void admin(WebContext ctx) {
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

        ctx.respondWith()
           .template("/templates/admin/geocaches.html.pasta",
                     page,
                     new Tuple<>(55.00, 8.00),
                     new Tuple<>(46.00, 12.00));
    }
}
