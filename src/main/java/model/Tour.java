package model;

import com.rometools.utils.Strings;
import org.apache.commons.lang3.RandomStringUtils;
import sirius.biz.jdbc.BizEntity;
import sirius.biz.protocol.JournalData;
import sirius.biz.protocol.Journaled;
import sirius.biz.protocol.NoJournal;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.BaseEntity;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.nls.NLS;

import java.util.Collections;
import java.util.HashSet;

/**
 *
 */
@Index(name = "tour_index", columns = "webcode", unique = true)
public class Tour extends BizEntity implements Journaled {

    /**
     * Contains the unique code of the tour.
     */
    public static final Mapping WEBCODE = Mapping.named("webcode");
    @Autoloaded
    @Length(8)
    private String webcode;

    /**
     * Contains the name of the tour.
     */
    public static final Mapping NAME = Mapping.named("name");
    @Trim
    @Autoloaded
    @Length(50)
    private String name;

    /**
     * Contains the password to edit the tour details and corresponding {@link WaypointInTour waypoints in the tour}.
     * If no password is provided the {@link Tour#WEBCODE} is set as password.
     */
    public static final Mapping PASSWORD = Mapping.named("password");
    @Trim
    @NoJournal
    @Autoloaded
    @Length(10)
    private String password;

    // TODO Passwort Hash stattdessen speichern & prüfen
    /*
    public static final Mapping PASSWORD_HASH = Mapping.named("passwordHash");
    @Trim
    @Length(20)
    @NoJournal
    private String passwordHash;*/

    /**
     * Used to record changes on fields of the tour.
     */
    public static final Mapping JOURNAL = Mapping.named("journal");
    private final JournalData journal = new JournalData(this);

    @BeforeSave
    protected void beforeSave() {
        if (Strings.isEmpty(webcode)) {
            webcode = generateWebcode();
        }
        if (Strings.isEmpty(password)) {
            password = webcode;
        }
    }

    private String generateWebcode() {
        String generatedWebcode = RandomStringUtils.random(8, "0123456789ABCDEFGHJKMNPQRTVWXYZ");
        if (oma.select(Tour.class).eq(WEBCODE, generatedWebcode).exists()) {
            return generateWebcode();
        }
        return generatedWebcode;
    }

    /**
     * @return hom many waypoints are in the tour.
     */
    public long getWaypointCount() {
        return oma.select(WaypointInTour.class).eq(WaypointInTour.TOUR, this).count();
    }

    /**
     * @return hom many own waypoints are in the tour.
     */
    public long getOwnWaypointCount() {
        return oma.select(WaypointInTour.class)
                  .eq(WaypointInTour.TOUR, this)
                  .eq(WaypointInTour.WAYPOINT.join(Waypoint.WAYPOINT_TYPE), WaypointType.OWN_WAYPOINT)
                  .count();
    }

    /**
     * @return hom many geocaches are referenced in the tour.
     */
    public long getGeocacheCount() {
        return oma.select(WaypointInTour.class)
                  .eq(WaypointInTour.TOUR, this)
                  .ne(WaypointInTour.WAYPOINT.join(Waypoint.GEOCACHE), null)
                  .distinctFields(BaseEntity.ID)
                  .count();
    }

    @Override
    public String toString() {
        if (isNew()) {
            return NLS.get("Tour");
        }
        return name + " (" + webcode + ")";
    }

    public String getWebcode() {
        return webcode;
    }

    public void setWebcode(String webcode) {
        this.webcode = webcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public JournalData getJournal() {
        return journal;
    }
}
