package model;

import sirius.kernel.nls.NLS;

/**
 * All available sizes of geocaches for <a href="https://geocaching.com>Geocaching.com</a>
 *
 * @see <a href="https://api.groundspeak.com/documentation#geocache-sizes">Groundspeak API Documentation</a>
 */

public enum GeocacheSize {

    UNKNOWN(1), MICRO(2), SMALL(8), REGULAR(3), LARGE(4), VIRTUAL(5), OTHER(6);

    GeocacheSize(int id) {
        this.id = id;
    }

    private final int id;

    @Override
    public String toString() {
        return NLS.get(getClass().getSimpleName() + "." + name());
    }

    public String getIconUrl() {
        return "https://geocaching.com/images/icons/container/" + name().toLowerCase() + ".gif";
    }
}
