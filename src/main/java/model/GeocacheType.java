package model;

import sirius.kernel.nls.NLS;

/**
 * All available types of geocaches for <a href="https://geocaching.com>Geocaching.com</a>
 *
 * @see <a href="https://api.groundspeak.com/documentation#geocache-types">Groundspeak API Documentation</a>
 */

public enum GeocacheType {

    UNKNOWN_TYPE(-1), LAB(0), TRADITIONAL(2), MULTI(3), VIRTUAL(4), LETTERBOX(5), EVENT(6), MYSTERY(8), APE(9),
    WEBCAM(11), LOCATIONLESS(12), CITO_EVENT(13), EARTH(137), MEGA_EVENT(453), GPS_ADVENTURE_EXHIBIT(1304),
    WHERIGO(1858), COMMUNITY_CELEBRATION_EVENT(3653), GEOCACHING_HQ(3773), GEOCACHING_HQ_CELEBRATION(3774),
    GEOCACHING_HQ_BLOCK_PARTY(4738), GIGA_EVENT(7005);

    GeocacheType(int id) {
        this.id = id;
    }

    private final int id;

    @Override
    public String toString() {
        return NLS.get(getClass().getSimpleName() + "." + name());
    }

    public String getIconUrl() {
        if (this == LAB) {
            //TODO check icon
            return "https://www.geocaching.com/images/wpttypes/sm/labs.png";
        }
        if (this == UNKNOWN_TYPE) {
            //TODO find icon
            return "";
        }
        return "https://www.geocaching.com/play/map/public/assets/icons/types/" + name().toLowerCase() + ".svg";
    }

}
