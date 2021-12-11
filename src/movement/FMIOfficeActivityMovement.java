package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import input.WKTReader;
import movement.map.SimMap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FMIOfficeActivityMovement extends MapBasedMovement implements SwitchableMovement {

	public static final String WORK_DAY_LENGTH_SETTING = "workDayLength";
	public static final String OFFICE_LOCATIONS_FILE_SETTING = "officeLocationsFile";

	private int workDayLength;
	private int startedWorkingTime;
	private boolean ready;

	private List<Coord> allOffices;

	private Coord lastWaypoint;
	private Coord officeLocation;

	public FMIOfficeActivityMovement(Settings settings) {
		super(settings);

		workDayLength = settings.getInt(WORK_DAY_LENGTH_SETTING);
		startedWorkingTime = -1;

		String officeLocationsFile = null;
		try {
			officeLocationsFile = settings.getSetting(OFFICE_LOCATIONS_FILE_SETTING);
		} catch (Throwable ignored) {
		}

		try {
			allOffices = new LinkedList<>();
			List<Coord> locationsRead = (new WKTReader()).readPoints(new File(officeLocationsFile));
			for (Coord coord : locationsRead) {
				SimMap map = getMap();
				Coord offset = map.getOffset();

				if (map.isMirrored()) {
					coord.setLocation(coord.getX(), -coord.getY());
				}
				coord.translate(offset.getX(), offset.getY());
				allOffices.add(coord);
			}
			officeLocation = allOffices.get(rng.nextInt(allOffices.size())).clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FMIOfficeActivityMovement(FMIOfficeActivityMovement proto) {
		super(proto);
		this.workDayLength = proto.workDayLength;
		startedWorkingTime = -1;

		this.allOffices = proto.allOffices;
		officeLocation = allOffices.get(rng.nextInt(allOffices.size())).clone();
	}

	@Override
	public Coord getInitialLocation() {
		double x = rng.nextDouble() * getMaxX();
		double y = rng.nextDouble() * getMaxY();
		Coord c = new Coord(x,y);

		this.lastWaypoint = c;
		return c.clone();
	}

	@Override
	public Path getPath() {
		if (startedWorkingTime == -1) {
			startedWorkingTime = SimClock.getIntTime();
		}

		if (SimClock.getIntTime() - startedWorkingTime >= workDayLength) {
			Path path =  new Path(1);
			path.addWaypoint(lastWaypoint.clone());
			ready = true;
			return path;
		}

		Path path =  new Path(1);
		path.addWaypoint(officeLocation.clone());
		return path;
	}

	@Override
	protected double generateWaitTime() {
		return workDayLength - (SimClock.getIntTime() - startedWorkingTime);
	}

	@Override
	public MapBasedMovement replicate() {
		return new FMIOfficeActivityMovement(this);
	}

	public Coord getLastLocation() {
		return lastWaypoint.clone();
	}

	public boolean isReady() {
		return ready;
	}

	public void setLocation(Coord lastWaypoint) {
		this.lastWaypoint = lastWaypoint.clone();
		startedWorkingTime = -1;
		ready = false;
	}

	public Coord getOfficeLocation() {
		return officeLocation.clone();
	}

	public Coord getRandomOfficeLocation() {
		officeLocation = allOffices.get(rng.nextInt(allOffices.size())).clone();
		return officeLocation.clone();
	}

}
