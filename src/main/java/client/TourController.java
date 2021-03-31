package client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import model.Geocache;
import model.GeocacheSize;
import model.GeocacheType;
import model.Tour;
import model.Waypoint;
import model.WaypointInTour;
import model.WaypointType;
import sirius.biz.protocol.TraceData;
import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Register;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Message;
import sirius.web.controller.Page;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.UserContext;
import sirius.web.services.JSONStructuredOutput;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * /* TODO bei uploiad
 * //https://www.geocaching.com/api/proxy/web/search/geocachepreview/GC3M3WH aufrufen & JSON parsen
 * <p>
 * - wenn nicht korrigiert & WP-laden an, GPX aufrufen und WPs als XML parsen
 * - bei korrigierten Koordinaten Original Koords abrufen und setzen
 * - alle Wegpunkte über den Wegpunkt Lookup Code (WP3M3WH) zum Cache (GC3M3WH) zuordnen
 * - alle Wegpuntke mit unsichtbaren Koordinaten nicht speichern??
 * - In Übersicht darstellen wiviele von welchem Typ
 * - Bei Abweichung Final von Listing nur Final als Final-Symbol darstellen auf Karte, in Übersicht aber Cachetyp
 * - Bei Listing == Final trotzdem Final & Listing setzen
 * - Wegpunkte bei korrigierten Koords mit FN Prefix kennzeichnen FN + GC-Code-ohne GC
 * <p>
 * <p>
 * - JSON-Route für Upload & Update Tour
 * - JSON-Route für Tour abrufen um in Browser zu importieren
 * - /tour/:1 --> Tour für User anzeigen
 * - /tours --> Tourliste anzeigen (nur Admin)
 * - /geocaches --> Nach Kartenausschnitt sortierte GC-Liste ausgeben (nur Admin)
 * - /geocache/:1 --> Alle WPs des Caches als Liste & Karte ausgeben (nur Admin)
 */
@Register
public class TourController extends BizController {

    /**
     * Shows a list of all {@link Tour tours}
     *
     * @param ctx the current request
     */
    @LoginRequired
    @Routed("/tours")
    public void showTours(WebContext ctx) {
        showAllTours(ctx);
    }

    /**
     * Deletes a {@link Tour}
     *
     * @param ctx the current request
     */
    @LoginRequired
    @Routed("/tour/:1/delete")
    public void deleteTour(WebContext ctx, String webcode) {
        oma.delete(oma.select(Tour.class).eq(Tour.WEBCODE, webcode).queryFirst());
        showAllTours(ctx);
    }

    /**
     * Shows the detailed view of a {@link Tour}
     *
     * @param ctx the current request
     */
    @DefaultRoute
    @Routed("/tour/:1")
    public void showTour(WebContext ctx, String webcode) {
        if (webcode == null || webcode.isEmpty()) {
            ctx.respondWith().template("/templates/index.html.pasta", new Page<Tour>());
            return;
        }
        Optional<Tour> tour = oma.select(Tour.class).eq(Tour.WEBCODE, webcode.toUpperCase()).first();
        if (!tour.isPresent()) {
            UserContext.message(Message.error("Die Tour mit dem Code '"
                                              + webcode.toUpperCase()
                                              + "' existiert nicht."));
            ctx.respondWith().template(HttpResponseStatus.NOT_FOUND, "/templates/index.html.pasta", new Page<Tour>());
            return;
        }
        Page<WaypointInTour> waypointsInTour = getWaypointsInTour(ctx, tour.get());
        ctx.respondWith()
           .template("/templates/tour.html.pasta",
                     tour.get(),
                     waypointsInTour,
                     getMapBounds(waypointsInTour).getFirst(),
                     getMapBounds(waypointsInTour).getSecond());
    }

    /**
     * Handles the upload of a new tour or updating an already exiting tour
     * with updated {@link model.WaypointInTour waypoints in the tour}.
     *
     * @param ctx the current request
     * @param out the JSON response
     */

    @Routed(value = "/tour/upload", jsonCall = true, priority = 90)
    public void uploadTour(WebContext ctx, JSONStructuredOutput out) {
        ctx.markAsLongCall();
        JSONObject json = ctx.getJSONContent().getJSONObject("tour");

        // TODO Passwortcheck
        String webcode = json.getString("webcode");
        String tourName = json.getString("name");
        boolean saveAsNewTour = json.getBoolean("saveAsNewTour");

        Tour tour = new Tour();
        Optional<Tour> oldTour = oma.select(Tour.class).eq(Tour.WEBCODE, webcode.toUpperCase()).one();
        List<WaypointInTour> oldWaypointsInTour = new java.util.ArrayList<>(Collections.emptyList());
        List<WaypointInTour> newWaypointsInTour = new java.util.ArrayList<>(Collections.emptyList());

        if (!saveAsNewTour && oldTour.isPresent()) {
            tour = oldTour.get();
            oma.select(WaypointInTour.class).eq(WaypointInTour.TOUR, tour).iterateAll(oldWaypointsInTour::add);
        }
        tour.setName(tourName);
        oma.update(tour);

        // TODO Geocaches verarbeiten...
        JSONArray jsonGeocaches = json.getJSONArray("geocaches");
        for (int i = 0; i < jsonGeocaches.size(); i++) {
            String gcCode = jsonGeocaches.getJSONObject(i).getString("code");
            Geocache geocache = oma.select(Geocache.class).eq(Geocache.GC_CODE, gcCode).first().orElse(new Geocache());
            geocache.setGcCode(gcCode);
            geocache.setName(jsonGeocaches.getJSONObject(i).getString("name"));
            geocache.setType(GeocacheType.valueOf(jsonGeocaches.getJSONObject(i).getString("geocacheType")));
            geocache.setSize(GeocacheSize.valueOf(jsonGeocaches.getJSONObject(i).getString("containerType")));
            geocache.setDifficulty(Amount.of(jsonGeocaches.getJSONObject(i).getDoubleValue("difficulty")));
            geocache.setTerrain(Amount.of(jsonGeocaches.getJSONObject(i).getDoubleValue("terrain")));
            oma.update(geocache);

            //Listing
            JSONObject postedCoordinates = jsonGeocaches.getJSONObject(i).getJSONObject("postedCoordinates");
            Amount originalLatitude = Amount.of(postedCoordinates.getDoubleValue("latitude"));
            Amount originalLongitude = Amount.of(postedCoordinates.getDoubleValue("longitude"));
            Waypoint listingWaypoint = oma.select(Waypoint.class)
                                          .eq(Waypoint.GEOCACHE, geocache)
                                          .eq(Waypoint.WAYPOINT_TYPE, WaypointType.ORIGINAL)
                                          .first()
                                          .orElse(new Waypoint());
            listingWaypoint.setWaypointType(WaypointType.ORIGINAL);
            listingWaypoint.setLatitude(originalLatitude);
            listingWaypoint.setLongitude(originalLongitude);
            listingWaypoint.getGeocache().setValue(geocache);
            oma.update(listingWaypoint);
            Waypoint waypointForTour = listingWaypoint;

            //Final
            JSONObject userCorrectedCoordinates =
                    jsonGeocaches.getJSONObject(i).getJSONObject("userCorrectedCoordinates");
            if (userCorrectedCoordinates != null) {
                Amount finalLatitude = Amount.of(userCorrectedCoordinates.getDoubleValue("latitude"));
                Amount finalLongitude = Amount.of(userCorrectedCoordinates.getDoubleValue("longitude"));
                Waypoint finalWaypoint = oma.select(Waypoint.class)
                                            .eq(Waypoint.GEOCACHE, geocache)
                                            .eq(Waypoint.WAYPOINT_TYPE, WaypointType.FINAL_LOCATION)
                                            .eq(Waypoint.LATITUDE, finalLatitude)
                                            .eq(Waypoint.LONGITUDE, finalLongitude)
                                            .first()
                                            .orElse(new Waypoint());
                finalWaypoint.setWaypointType(WaypointType.FINAL_LOCATION);
                finalWaypoint.setLatitude(finalLatitude);
                finalWaypoint.setLongitude(finalLongitude);
                finalWaypoint.getGeocache().setValue(geocache);
                oma.update(finalWaypoint);
                waypointForTour = finalWaypoint;
            }
            //TODO Positionen anpassen beim Tour überschreiben/hochladen

            WaypointInTour waypointInTour = oma.select(WaypointInTour.class)
                                               .eq(WaypointInTour.TOUR, tour)
                                               .eq(WaypointInTour.WAYPOINT, waypointForTour)
                                               .first()
                                               .orElse(new WaypointInTour());
            waypointInTour.getTour().setValue(tour);
            waypointInTour.getWaypoint().setValue(waypointForTour);
            waypointInTour.setPosition(i+1);
            newWaypointsInTour.add(waypointInTour);
            //TODO andere Wegpuntke die aber erst aus GPX geholt werden müssen
        }

        //own Waypoints
        JSONArray jsonWaypoints = json.getJSONArray("ownWaypoints");
        for (int i = 0; i < jsonWaypoints.size(); i++) {
            JSONObject jsonWaypoint = jsonWaypoints.getJSONObject(i);
            Amount latitude = Amount.of(jsonWaypoint.getDoubleValue("latitude"));
            Amount longitude = Amount.of(jsonWaypoint.getDoubleValue("longitude"));
            String note = jsonWaypoint.getString("note");
            Waypoint ownWaypoint = new Waypoint();
            ownWaypoint.setLatitude(latitude);
            ownWaypoint.setLongitude(longitude);
            ownWaypoint.setNote(note);
            oma.update(ownWaypoint);
            WaypointInTour ownWaypointInTour = oma.select(WaypointInTour.class)
                                                  .eq(WaypointInTour.TOUR, tour)
                                                  .eq(WaypointInTour.WAYPOINT, ownWaypoint)
                                                  .first()
                                                  .orElse(new WaypointInTour());
            ownWaypointInTour.getTour().setValue(tour);
            ownWaypointInTour.getWaypoint().setValue(ownWaypoint);
            ownWaypointInTour.setPosition(jsonWaypoint.getInteger("position"));
            newWaypointsInTour.add(ownWaypointInTour);
        }

        AtomicInteger removedWaypoints = new AtomicInteger();
        oldWaypointsInTour.forEach(oldWaypointInTour -> {
            if (!newWaypointsInTour.contains(oldWaypointInTour)) {
                removedWaypoints.getAndIncrement();
                oma.delete(oldWaypointInTour);
            }
        });

        newWaypointsInTour.forEach(newWaypointInTour -> oma.update(newWaypointInTour));

        out.property("removedWaypoints", removedWaypoints);
        out.property("geocaches", jsonGeocaches.size());
        out.property("ownWaypoints", jsonWaypoints.size());
        out.property("savedAsNewTour", saveAsNewTour || !oldTour.isPresent());
        out.property("webcode", tour.getWebcode());

        //debug
        //out.property("geocachesInTour", tour.getWebcode());
    }

    private void showAllTours(WebContext ctx) {
        SmartQuery<Tour> smartQuery = oma.select(Tour.class);

        smartQuery.orderDesc(Tour.TRACE.inner(TraceData.CREATED_AT));

        SQLPageHelper<Tour> pageHelper = SQLPageHelper.withQuery(smartQuery)
                                                      .withContext(ctx)
                                                      .withSearchFields(QueryField.contains(Tour.WEBCODE),
                                                                        QueryField.contains(Tour.NAME));
        Page<Tour> page = pageHelper.asPage().withTotalItems((int) smartQuery.count());
        ctx.respondWith().template("/templates/admin/tours.html.pasta", page);
    }

    private Page<WaypointInTour> getWaypointsInTour(WebContext ctx, Tour tour) {
        SmartQuery<WaypointInTour> smartQuery = oma.select(WaypointInTour.class).eq(WaypointInTour.TOUR, tour);

        smartQuery.orderAsc(WaypointInTour.POSITION);

        SQLPageHelper<WaypointInTour> pageHelper = SQLPageHelper.withQuery(smartQuery).withContext(ctx);
        return pageHelper.withPageSize(500).asPage().withTotalItems((int) smartQuery.count());
    }

    private Tuple<Tuple<Double, Double>, Tuple<Double, Double>> getMapBounds(Page<WaypointInTour> waypointsInTour) {
        double nwLatitude = 0;
        double nwLongitude = 0;
        double seLatitude = 0;
        double seLongitude = 0;
        if (waypointsInTour.getItems().isEmpty()) {
            return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
        }
        Waypoint firstWaypoint = waypointsInTour.getItems().get(0).getWaypoint().fetchValue();
        nwLatitude = firstWaypoint.getLatitude().getAmount().doubleValue();
        nwLongitude = firstWaypoint.getLongitude().getAmount().doubleValue();
        seLatitude = firstWaypoint.getLatitude().getAmount().doubleValue();
        seLongitude = firstWaypoint.getLongitude().getAmount().doubleValue();

        if (waypointsInTour.getItems().size() == 1) {
            return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
        }

        for (int i = 0; i < waypointsInTour.getItems().size(); i++) {
            Waypoint waypoint = waypointsInTour.getItems().get(i).getWaypoint().fetchValue();
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
