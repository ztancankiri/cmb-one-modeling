package movement;

import core.Coord;
import core.Settings;
import input.WKTReader;
import movement.map.SimMap;

import java.io.File;
import java.util.List;

public class StationaryMovement extends MapBasedMovement {
	/** Per node group setting for setting the location ({@value}) */
	public static final String LOCATION_S = "nodeLocationFile";

	private Coord loc;

	public StationaryMovement(Settings s) {
		super(s);

		String locationFile =  null;
		try {
			locationFile = s.getSetting(LOCATION_S);
			List<Coord> locationRead = (new WKTReader()).readPoints(new File(locationFile));

			SimMap map = getMap();
			Coord offset = map.getOffset();

			Coord coord = locationRead.get(0);
			if (map.isMirrored()) {
				coord.setLocation(coord.getX(), -coord.getY());
			}
			coord.translate(offset.getX(), offset.getY());

			this.loc = coord;
		} catch (Throwable ignored) {
		}
	}

	/**
	 * Copy constructor.
	 * @param sm The StationaryMovement prototype
	 */
	public StationaryMovement(StationaryMovement sm) {
		super(sm);
		this.loc = sm.loc;
	}

	/**
	 * Returns the only location of this movement model
	 * @return the only location of this movement model
	 */
	@Override
	public Coord getInitialLocation() {
		return loc;
	}

	/**
	 * Returns a single coordinate path (using the only possible coordinate)
	 * @return a single coordinate path
	 */
	@Override
	public Path getPath() {
		Path p = new Path(0);
		p.addWaypoint(loc);
		return p;
	}

	@Override
	public double nextPathAvailable() {
		return Double.MAX_VALUE;	// no new paths available
	}

	@Override
	public StationaryMovement replicate() {
		return new StationaryMovement(this);
	}

}
