package model;

import sirius.kernel.nls.NLS;

/**
 * All available types of waypoints in a {@link Tour}
 *
 * @see <a href="https://api.groundspeak.com/documentation#additional-waypoint-types">Groundspeak API Documentation</a>
 * //TODO id stattdessen dazunehmen mit /images/wpttypes/pins/<ID>.png
 */
public enum WaypointType {

    /**
     * The original coordinates (in case of some cache types also the {@link WaypointType#FINAL_LOCATION})
     */
    ORIGINAL(null),

    /**
     * The final waypoint of the cache used as main symbol on the map
     * //TODO Icon anpassen auf puzzle-icon
     */
    FINAL_LOCATION("https://www.geocaching.com/images/wpttypes/flag.jpg"),

    /**
     * An available location for parking
     */
    PARKING_AREA("https://www.geocaching.com/images/wpttypes/pkg.jpg"),

    /**
     * A virtual stage where you e.g. have to answer a question
     */
    VIRTUAL_STAGE("https://www.geocaching.com/images/wpttypes/puzzle.jpg"),

    /**
     * A Physical Stage where you e.g. have to find the next coordinates
     */
    PHYSICAL_STAGE("https://www.geocaching.com/images/wpttypes/stage.jpg"),

    /**
     * An informational waypoint e.g. an available access point to the final location
     */
    TRAILHEAD("https://www.geocaching.com/images/wpttypes/trailhead.jpg"),

    /**
     * A reference point e.g. a nearby station or bus-stop
     */
    REFERENCE_POINT("https://www.geocaching.com/images/wpttypes/waypoint.jpg"),

    /**
     * Custom added waypoint e.g. a restaurant or a nice view
     * //TODO Icon anpassen
     */
    OWN_WAYPOINT("https://www.gctour.de/i/RedFlag.png");

    WaypointType(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    private final String iconUrl;

    @Override
    public String toString() {
        return NLS.get(getClass().getSimpleName() + "." + name());
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
