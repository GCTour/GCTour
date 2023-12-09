package model;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.protocol.JournalData;
import sirius.biz.protocol.Journaled;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.MaxValue;
import sirius.db.mixing.annotations.MinValue;
import sirius.db.mixing.annotations.Numeric;
import sirius.db.mixing.annotations.OnValidate;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Amount;
import sirius.kernel.commons.Strings;
import sirius.kernel.nls.NLS;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * // TODO Javadoc
 */
@Index(name = "gcCode_index", columns = "gcCode", unique = true)
@Index(name = "gcId_index", columns = "gcId", unique = true)
public class Geocache extends BizEntity implements Journaled {

    /**
     * Contains the unique code of the geocache known as GC-Code.
     */
    public static final Mapping GC_CODE = Mapping.named("gcCode");
    @Trim
    @Autoloaded
    @Length(16)
    private String gcCode;

    /**
     * Contains the converted GC-Code as GC ID.
     */
    public static final Mapping GC_ID = Mapping.named("gcId");
    @Autoloaded
    private long gcId;

    /**
     * Contains the name of the geocache - the listing title.
     */
    public static final Mapping NAME = Mapping.named("name");
    @Trim
    @Autoloaded
    @Length(50)
    private String name;

    /**
     * Contains the type of the geocache.
     */
    public static final Mapping TYPE = Mapping.named("type");
    @Length(30)
    @Autoloaded
    private GeocacheType type = GeocacheType.UNKNOWN_TYPE;

    /**
     * Contains the size of the geocache-container.
     */
    public static final Mapping SIZE = Mapping.named("size");
    @Length(10)
    @Autoloaded
    private GeocacheSize size = GeocacheSize.UNKNOWN;

    /**
     * Contains the difficulty-rating of the geocache.
     */
    public static final Mapping DIFFICULTY = Mapping.named("difficulty");
    @Autoloaded
    @MinValue(1)
    @MaxValue(5)
    @Numeric(precision = 2, scale = 1)
    private Amount difficulty = Amount.NOTHING;

    /**
     * Contains the terrain-rating of the geocache.
     */
    public static final Mapping TERRAIN = Mapping.named("terrain");
    @Autoloaded
    @MinValue(1)
    @MaxValue(5)
    @Numeric(precision = 2, scale = 1)
    private Amount terrain = Amount.NOTHING;

    // TODO add Status - aktiv, deaktiviert, archiviert, unpublished?

    // TODO add PMO-Status?

    // TODO Listing-WP oder Korr-WP als direkte Referenz reinlegen?^

    /*
    curl 'https://www.geocaching.com/geocache/GC3M3WH -so - | grep -iPo '(?<=<title>)(.*)(?=</title>)'

     */

    /**
     * Used to record changes on fields of the geocache.
     */
    public static final Mapping JOURNAL = Mapping.named("journal");
    private final JournalData journal = new JournalData(this);

    @Override
    public JournalData getJournal() {
        return journal;
    }

    /**
     * @param gcCode the GC-Code to convert
     * @return the ID for the given GC-Code
     * @see <a href="https://api.groundspeak.com/documentation#referencecodes">Groundspeak API Doc</a>
     */
    public static long convertGcCodeToId(@Nonnull String gcCode) {
        String srcAlphabet = "0123456789ABCDEFGHJKMNPQRTVWXYZ";
        String destAlphabet = "0123456789";

        long result = parseLong(convert(gcCode.substring(2), srcAlphabet, destAlphabet));
        if (result < 476656) {
            return parseLong(convert(gcCode.substring(2), "0123456789ABCDEF", destAlphabet));
        }
        return result - 411120;
    }

    /**
     * @param id the ID to convert
     * @return the GC-Code for the given ID
     * @see <a href="https://api.groundspeak.com/documentation#referencecodes">Groundspeak API Doc</a>
     */
    public static String convertIdToGcCode(long id) {
        String srcAlphabet = "0123456789";
        String destAlphabet = "0123456789ABCDEFGHJKMNPQRTVWXYZ";

        if (id + 411120 < 476656) {
            return "GC" + convert(String.valueOf(id), srcAlphabet, "0123456789ABCDEF");
        }
        return "GC" + convert(String.valueOf(id + 411120), srcAlphabet, destAlphabet);
    }

    private static String convert(@Nonnull String src, @Nonnull String srcAlphabet, @Nonnull String destAlphabet) {
        int srcBase = srcAlphabet.length();
        int dstBase = destAlphabet.length();

        int val = 0;
        int mlt = 1;

        while (src.length() > 0) {
            char digit = src.charAt(src.length() - 1);
            int digVal = srcAlphabet.indexOf(digit);
            if (digVal > -1) {
                val += mlt * digVal;
                mlt *= srcBase;
            }
            src = src.substring(0, src.length() - 1);
        }

        String ret = "";

        while (val >= dstBase) {
            int digitVal = val % dstBase;
            char digit = destAlphabet.charAt(digitVal);
            ret = Strings.apply("%s%s", digit, ret);
            val = val / dstBase;
        }

        char digit = destAlphabet.charAt(val);
        ret = digit + ret;

        return ret;
    }

    //TODO check S ->5 und O ->0 - wird Validate oder Safe zuerst ausgeführt?
    @OnValidate
    protected void validate(Consumer<String> warnings) {
        gcCode = gcCode.toUpperCase();
        if (gcCode.contains("S") || gcCode.contains("O")) {
            gcCode = gcCode.replace('S', '5').replace("O", "0");
            warnings.accept(NLS.fmtr("Geocache.gcCodeFixed").set("gcCode", gcCode).format());
        }
        if (!gcCode.matches("GC[0123456789ABCDEFGHJKMNPQRTVWXYZ]*")) {
            warnings.accept(NLS.fmtr("Geocache.invalidGcCode").set("gcCode", gcCode).format());
        }
    }

    @BeforeSave
    protected void beforeSave() {
        //TODO check GC-Code, Size, Type, Coordinates
        gcCode = gcCode.toUpperCase().replace('S', '5').replace("O", "0");
        if (gcId == 0) {
            gcId = convertGcCodeToId(gcCode);
        }
    }

    @Override
    public String toString() {
        if (isNew()) {
            return NLS.get("Geocache");
        }
        return name + " (" + gcCode + ")";
    }

    /**
     * @return all corresponding {@link Waypoint waypoints} of the geocache.
     */
    public List<Waypoint> getWaypoints() {
        List<Waypoint> waypoints = new ArrayList<>();
        oma.select(Waypoint.class)
           .eq(Waypoint.GEOCACHE, this)
           .orderAsc(Waypoint.WAYPOINT_TYPE)
           .iterateAll(waypoints::add);
        return waypoints;
    }

    /**
     * @return the {@link Waypoint Listing waypoint} of the geocache.
     */
    public Waypoint getListingWaypoint() {
        return oma.select(Waypoint.class)
                  .eq(Waypoint.GEOCACHE, this)
                  .eq(Waypoint.WAYPOINT_TYPE, WaypointType.ORIGINAL)
                  .queryOne();
    }

    public String getGcCode() {
        return gcCode;
    }

    public void setGcCode(String gcCode) {
        this.gcCode = gcCode;
    }

    public long getGcId() {
        return gcId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeocacheType getType() {
        return type;
    }

    public void setType(GeocacheType type) {
        this.type = type;
    }

    public GeocacheSize getSize() {
        return size;
    }

    public void setSize(GeocacheSize size) {
        this.size = size;
    }

    public Amount getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Amount difficulty) {
        this.difficulty = difficulty;
    }

    public Amount getTerrain() {
        return terrain;
    }

    public void setTerrain(Amount terrain) {
        this.terrain = terrain;
    }
}
