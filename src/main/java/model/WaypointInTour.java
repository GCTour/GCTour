package model;

import sirius.biz.jdbc.BizEntity;
import sirius.biz.web.Autoloaded;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Index;
import sirius.db.mixing.annotations.Unique;
import sirius.db.mixing.types.BaseEntityRef;

/**
 * The mapping for a {@link Waypoint} in a {@link Tour}
 */
@Index(name = "tour_waypoint_index", columns = {"tour", "waypoint", "position"}, unique = true)
public class WaypointInTour extends BizEntity {

    /**
     * The referenced {@link Tour}
     */
    public static final Mapping TOUR = Mapping.named("tour");
    @Autoloaded
    private final SQLEntityRef<Tour> tour = SQLEntityRef.writeOnceOn(Tour.class, BaseEntityRef.OnDelete.CASCADE);

    /**
     * The referenced {@link Waypoint}
     */
    public static final Mapping WAYPOINT = Mapping.named("waypoint");
    @Autoloaded
    @Unique(within = "tour")
    private final SQLEntityRef<Waypoint> waypoint =
            SQLEntityRef.writeOnceOn(Waypoint.class, BaseEntityRef.OnDelete.CASCADE);

    /**
     * The position of the {@link Waypoint} in the {@link Tour} starting with #1
     */
    public static final Mapping POSITION = Mapping.named("position");
    @Autoloaded
    @Unique(within = "tour")
    private int position;

    public SQLEntityRef<Tour> getTour() {
        return tour;
    }

    public SQLEntityRef<Waypoint> getWaypoint() {
        return waypoint;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
