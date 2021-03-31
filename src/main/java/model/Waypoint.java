package model;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.protocol.JournalData;
import sirius.biz.protocol.Journaled;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.MaxValue;
import sirius.db.mixing.annotations.MinValue;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.Trim;
import sirius.db.mixing.types.BaseEntityRef;
import sirius.kernel.commons.Amount;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * //TODO Javadoc
 */
@Index(name = "geocache_waypoint_index", columns = {"latitude", "longitude", "waypointType", "geocache"}, unique = true)
public class Waypoint extends BizEntity implements Journaled {

    /**
     * Contains the latitude.
     */
    public static final Mapping LATITUDE = Mapping.named("latitude");
    @Autoloaded
    @MinValue(-90)
    @MaxValue(90)
    @Numeric(precision = 10, scale = 8)
    private Amount latitude = Amount.NOTHING;

    /**
     * Contains the longitude.
     */
    public static final Mapping LONGITUDE = Mapping.named("longitude");
    @Autoloaded
    @MinValue(-180)
    @MaxValue(180)
    @Numeric(precision = 11, scale = 8)
    private Amount longitude = Amount.NOTHING;

    /**
     * Contains the elevation fetched via
     * <a href="https://api.open-elevation.com/api/v1/lookup?locations=lat,lon">Open Elevation</a>
     * TODO andere Höhenbestimmung (siehe gclh)
     */
    public static final Mapping ELEVATION = Mapping.named("elevation");
    @Autoloaded
    @NullAllowed
    @Numeric(precision = 5, scale = 0)
    private Amount elevation = Amount.NOTHING;

    /**
     * Contains a note about the waypoint
     */
    public static final Mapping NOTE = Mapping.named("note");
    @Trim
    @Length(1024)
    @Autoloaded
    @NullAllowed
    private String note;

    /**
     * Contains the {@link WaypointType} of the waypoint
     */
    public static final Mapping WAYPOINT_TYPE = Mapping.named("waypointType");
    @Autoloaded
    private WaypointType waypointType;

    //TODO WP-Code + in Index aufnehmen um Dopplung zu verhindern, aber Update zu erlauben anstatt GC zu speichern?

    /**
     * Only for waypoints of type {@link WaypointType#OWN_WAYPOINT} the {@link Geocache} is not set.
     */
    public static final Mapping GEOCACHE = Mapping.named("geocache");
    @Autoloaded
    @NullAllowed
    private final SQLEntityRef<Geocache> geocache =
            SQLEntityRef.writeOnceOn(Geocache.class, BaseEntityRef.OnDelete.CASCADE);

    /**
     * Used to record changes on fields of the waypoint.
     */
    public static final Mapping JOURNAL = Mapping.named("journal");
    private final JournalData journal = new JournalData(this);

    @BeforeSave
    protected void beforeSave() {
        if (geocache.isEmpty()) {
            waypointType = WaypointType.OWN_WAYPOINT;
        }
        //TODO check elevation
    }

    /**
     * @return Coordinates of the waypoint in the format N/S DD° MM.MMM E/W DDD° MM.MMM
     */
    public String getCoordinates() {
        String coords = "";

        coords += getLatitude().isNegative() ? "S " : "N ";
        if (getLatitude().getAmount().abs().intValue() < 10) {
            coords += "0";
        }
        coords += getLatitude().getAmount().abs().toBigInteger();
        coords += "° ";
        BigDecimal latMinutes = getLatitude().getAmount()
                                             .subtract(new BigDecimal(getLatitude().getAmount().intValue()))
                                             .multiply(new BigDecimal(60))
                                             .abs()
                                             .setScale(3, RoundingMode.HALF_UP);
        if (latMinutes.compareTo(BigDecimal.TEN) < 0) {
            coords += "0";
        }
        coords += latMinutes;

        coords += getLongitude().isNegative() ? " W " : " E ";
        if (getLongitude().getAmount().abs().intValue() < 100) {
            coords += "0";
        }
        if (getLongitude().getAmount().abs().intValue() < 10) {
            coords += "0";
        }
        coords += getLongitude().getAmount().abs().toBigInteger();
        coords += "° ";
        BigDecimal lonMinutes = getLongitude().getAmount()
                                              .subtract(new BigDecimal(getLongitude().getAmount().intValue()))
                                              .multiply(new BigDecimal(60))
                                              .abs()
                                              .setScale(3, RoundingMode.HALF_UP);
        if (lonMinutes.compareTo(BigDecimal.TEN) < 0) {
            coords += "0";
        }
        coords += lonMinutes;

        return coords;
    }

    public Amount getLatitude() {
        return latitude;
    }

    public void setLatitude(Amount latitude) {
        this.latitude = latitude;
    }

    public Amount getLongitude() {
        return longitude;
    }

    public void setLongitude(Amount longitude) {
        this.longitude = longitude;
    }

    public Amount getElevation() {
        return elevation;
    }

    public void setElevation(Amount elevation) {
        this.elevation = elevation;
    }

    public WaypointType getWaypointType() {
        return waypointType;
    }

    public void setWaypointType(WaypointType waypointType) {
        this.waypointType = waypointType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public SQLEntityRef<Geocache> getGeocache() {
        return geocache;
    }

    @Override
    public JournalData getJournal() {
        return journal;
    }
}
