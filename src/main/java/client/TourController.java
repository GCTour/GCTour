package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.handler.codec.http.HttpResponseStatus;
import model.Geocache;
import model.GeocacheSize;
import model.GeocacheType;
import model.Tour;
import model.Waypoint;
import model.WaypointInTour;
import model.WaypointType;
import sirius.biz.protocol.TraceData;
import sirius.biz.protocol.Traced;
import sirius.biz.web.BizController;
import sirius.biz.web.SQLPageHelper;
import sirius.db.jdbc.OMA;
import sirius.db.jdbc.SmartQuery;
import sirius.db.mixing.query.QueryField;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Json;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Tuple;
import sirius.kernel.di.std.Register;
import sirius.web.controller.DefaultRoute;
import sirius.web.controller.Message;
import sirius.web.controller.Page;
import sirius.web.controller.Routed;
import sirius.web.http.WebContext;
import sirius.web.security.LoginRequired;
import sirius.web.security.UserContext;
import sirius.web.services.InternalService;
import sirius.web.services.JSONStructuredOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * /* TODO bei upload
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
     * @param ctx     the current request
     * @param webcode the {@link Tour} to delete
     */
    @Routed("/tour/:1/delete")
    public void deleteTour(WebContext ctx, String webcode) {
        oma.select(Tour.class).eq(Tour.WEBCODE, webcode).delete();
        UserContext.message(Message.info().withTextMessage("Die Tour wurde erfoglreich gelöscht."));
        if (UserContext.getCurrentUser().isLoggedIn()) {
            ctx.respondWith().redirectToGet("/tours");
        }
        ctx.respondWith().redirectToGet("/");
    }

    /**
     * Deletes a {@link WaypointInTour} from a {@link Tour}
     *
     * @param ctx      the current request
     * @param webcode  the {@link Tour} to edit
     * @param position the position of the {@link WaypointInTour} to remove from the given {@link Tour}
     */
    @Routed("/tour/:1/:2/delete")
    public void deleteWaypointFromTour(WebContext ctx, String webcode, int position) {
        Tour tour = oma.select(Tour.class).eq(Tour.WEBCODE, webcode).queryFirst();
        oma.select(WaypointInTour.class)
                      .eq(WaypointInTour.TOUR, tour)
                      .eq(WaypointInTour.POSITION, position)
                      .delete();
        oma.select(WaypointInTour.class)
           .eq(WaypointInTour.TOUR, tour)
           .where(OMA.FILTERS.gt(WaypointInTour.POSITION, position))
           .orderAsc(WaypointInTour.POSITION)
           .iterateAll(waypointInTour -> {
               waypointInTour.setPosition(waypointInTour.getPosition() - 1);
               oma.update(waypointInTour);
           });
        UserContext.message(Message.info().withTextMessage("Der Wegpunkt wurde aus der Tour gelöscht."));
        ctx.respondWith().redirectToGet("/tour/" + webcode);
    }

    /**
     * Renames a {@link Tour}
     *
     * @param ctx     the current request
     * @param webcode the {@link Tour} to rename
     */
    @Routed("/tour/:1/rename")
    public void renameTour(WebContext ctx, String webcode) {
        Tour tour = oma.select(Tour.class).eq(Tour.WEBCODE, webcode).queryFirst();
        String name = ctx.getParameter("name");
        if (Strings.isEmpty(name)) {
            tour.setName(webcode);
            UserContext.message(Message.warn()
                                       .withTextMessage(
                                               "Die Tour wurde umbenannt, der Name wurde jedoch auf den Webcode gesetzt, da er leer war."));
        } else if (name.length() > 50) {
            tour.setName(ctx.getParameter("name").substring(0, 50));
            UserContext.message(Message.warn()
                                       .withTextMessage(
                                               "Die Tour wurde umbenannt, der Name wurde jedoch auf 50 Zeichen gekürzt."));
        } else {
            tour.setName(ctx.getParameter("name"));
            UserContext.message(Message.info().withTextMessage("Die Tour wurde erfolgreich umbenannt."));
        }
        oma.update(tour);
        ctx.respondWith().redirectToGet("/tour/" + webcode);
    }

    /**
     * Shows the detailed view of a {@link Tour}
     *
     * @param ctx     the current request
     * @param webcode the requested {@link Tour}
     */
    @DefaultRoute
    @Routed("/tour/:1")
    public void showTour(WebContext ctx, String webcode) {
        if (Strings.isEmpty(webcode)) {
            ctx.respondWith().template("/templates/index.html.pasta", new Page<Tour>());
            return;
        }
        Optional<Tour> tour = oma.select(Tour.class).eq(Tour.WEBCODE, webcode.toUpperCase()).first();
        if (tour.isEmpty()) {
            UserContext.message(Message.error()
                                       .withHTMLMessage("Die Tour mit dem Code '"
                                                        + webcode.toUpperCase()
                                                        + "' existiert nicht."));
            ctx.respondWith().template(HttpResponseStatus.NOT_FOUND, "/templates/index.html.pasta", new Page<Tour>());
            return;
        }
        List<WaypointInTour> waypointsInTour = new ArrayList<>(Collections.emptyList());
        oma.select(WaypointInTour.class)
           .eq(WaypointInTour.TOUR, tour.get())
           .orderAsc(WaypointInTour.POSITION)
           .iterateAll(waypointsInTour::add);

        Tuple<Tuple<Double, Double>, Tuple<Double, Double>> mapBounds = getMapBounds(waypointsInTour);
        ctx.respondWith()
           .template("/templates/tour.html.pasta",
                     tour.get(),
                     waypointsInTour,
                     mapBounds.getFirst(),
                     mapBounds.getSecond());
    }

    /**
     * Handles the upload of a new tour or updating an already exiting tour
     * with updated {@link model.WaypointInTour waypoints in the tour}.
     *
     * @param ctx the current request
     * @param out the JSON response
     */

    @InternalService
    @Routed(value = "/tour/upload", priority = 90)
    public void uploadTour(WebContext ctx, JSONStructuredOutput out) {
        ctx.markAsLongCall();
        JsonNode json = ctx.getJSONContent().get("tour");

        // TODO Passwortcheck
        String webcode = json.get("webcode").asText();
        String tourName = json.get("name").asText();
        boolean saveAsNewTour = json.get("saveAsNewTour").asBoolean();

        Tour tour = new Tour();
        Optional<Tour> oldTour = oma.select(Tour.class).eq(Tour.WEBCODE, webcode.toUpperCase()).one();
        List<WaypointInTour> oldWaypointsInTour = new java.util.ArrayList<>(Collections.emptyList());
        List<WaypointInTour> newWaypointsInTour = new java.util.ArrayList<>(Collections.emptyList());

        if (!saveAsNewTour && oldTour.isPresent()) {
            tour = oldTour.get();
            oma.select(WaypointInTour.class)
               .eq(WaypointInTour.TOUR, tour)
               .orderAsc(WaypointInTour.POSITION)
               .iterateAll(oldWaypointsInTour::add);
        }
        tour.setName(tourName);
        oma.update(tour);

        // TODO Geocaches verarbeiten...
        ArrayNode jsonGeocaches = Json.parseArray(json.get("geocaches").toString());
        for (int i = 0; i < jsonGeocaches.size(); i++) {
            String gcCode = jsonGeocaches.get(i).get("code").asText();
            Geocache geocache = oma.select(Geocache.class).eq(Geocache.GC_CODE, gcCode).first().orElse(new Geocache());
            geocache.setGcCode(gcCode);
            geocache.setName(jsonGeocaches.get(i).get("name").asText());
            geocache.setType(GeocacheType.valueOf(jsonGeocaches.get(i).get("geocacheType").asText()));
            geocache.setSize(GeocacheSize.valueOf(jsonGeocaches.get(i).get("containerType").asText()));
            geocache.setDifficulty(Amount.of(jsonGeocaches.get(i).get("difficulty").asDouble()));
            geocache.setTerrain(Amount.of(jsonGeocaches.get(i).get("terrain").asDouble()));
            oma.update(geocache);

            //Listing
            JsonNode postedCoordinates = jsonGeocaches.get(i).get("postedCoordinates");
            Amount originalLatitude = Amount.of(postedCoordinates.get("latitude").decimalValue());
            Amount originalLongitude = Amount.of(postedCoordinates.get("longitude").decimalValue());
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
            JsonNode userCorrectedCoordinates = jsonGeocaches.get(i).get("userCorrectedCoordinates");
            if (userCorrectedCoordinates != null) {
                Amount finalLatitude = Amount.of(userCorrectedCoordinates.get("latitude").asDouble());
                Amount finalLongitude = Amount.of(userCorrectedCoordinates.get("longitude").asDouble());
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
            waypointInTour.setPosition(i + 1);
            newWaypointsInTour.add(waypointInTour);
            //TODO andere Wegpuntke die aber erst aus GPX geholt werden müssen
        }

        //own Waypoints
        ArrayNode jsonWaypoints = Json.parseArray(json.get("ownWaypoints").toString());
        for (int i = 0; i < jsonWaypoints.size(); i++) {
            JsonNode jsonWaypoint = jsonWaypoints.get(i);
            Amount latitude = Amount.of(jsonWaypoint.get("latitude").asDouble());
            Amount longitude = Amount.of(jsonWaypoint.get("longitude").asDouble());
            String note = jsonWaypoint.get("note").asText();
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
            ownWaypointInTour.setPosition(jsonWaypoint.get("position").asInt());
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
        out.property("savedAsNewTour", saveAsNewTour || oldTour.isEmpty());
        out.property("webcode", tour.getWebcode());

        //debug
        //out.property("geocachesInTour", tour.getWebcode());
    }

    private void showAllTours(WebContext ctx) {
        SmartQuery<Tour> smartQuery = oma.select(Tour.class);

        smartQuery.orderDesc(Traced.TRACE.inner(TraceData.CREATED_AT));

        SQLPageHelper<Tour> pageHelper = SQLPageHelper.withQuery(smartQuery)
                                                      .withContext(ctx)
                                                      .withSearchFields(QueryField.contains(Tour.WEBCODE),
                                                                        QueryField.contains(Tour.NAME));
        Page<Tour> page = pageHelper.asPage().withTotalItems((int) smartQuery.count());
        ctx.respondWith().template("/templates/admin/tours.html.pasta", page);
    }

    private Tuple<Tuple<Double, Double>, Tuple<Double, Double>> getMapBounds(List<WaypointInTour> waypointsInTour) {
        double nwLatitude = 0;
        double nwLongitude = 0;
        double seLatitude = 0;
        double seLongitude = 0;
        if (waypointsInTour.isEmpty()) {
            return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
        }
        Waypoint firstWaypoint = waypointsInTour.get(0).getWaypoint().fetchValue();
        nwLatitude = firstWaypoint.getLatitude().getAmount().doubleValue();
        nwLongitude = firstWaypoint.getLongitude().getAmount().doubleValue();
        seLatitude = firstWaypoint.getLatitude().getAmount().doubleValue();
        seLongitude = firstWaypoint.getLongitude().getAmount().doubleValue();

        if (waypointsInTour.size() == 1) {
            return new Tuple<>(new Tuple<>(nwLatitude, nwLongitude), new Tuple<>(seLatitude, seLongitude));
        }

        for (int i = 0; i < waypointsInTour.size(); i++) {
            Waypoint waypoint = waypointsInTour.get(i).getWaypoint().fetchValue();
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
