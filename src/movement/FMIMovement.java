package movement;

import core.Coord;
import core.Settings;

public class FMIMovement extends ExtendedMovementModel {

	private FMIOfficeActivityMovement workerMM;
	private HomeActivityMovement homeMM;
	private CarMovement carMM;

	private static final int TO_WORK_MODE = 0;
	private static final int TO_HOME_MODE = 1;
	private static final int WORK_MODE = 2;
	private static final int HOME_MODE = 3;

	private static final double HOME_PROB = 0.5;

	private int mode;

	public FMIMovement(Settings settings) {
		super(settings);
		workerMM = new FMIOfficeActivityMovement(settings);
		homeMM = new HomeActivityMovement(settings);
		carMM = new CarMovement(settings);

		setCurrentMovementModel(homeMM);
		mode = HOME_MODE;
	}

	public FMIMovement(FMIMovement proto) {
		super(proto);
		workerMM = new FMIOfficeActivityMovement(proto.workerMM);
		homeMM = new HomeActivityMovement(proto.homeMM);
		carMM = new CarMovement(proto.carMM);

		setCurrentMovementModel(homeMM);
		mode = proto.mode;
	}

	@Override
	public boolean newOrders() {
		switch (mode) {
		case WORK_MODE:
			if (workerMM.isReady()) {
				setCurrentMovementModel(carMM);

				if (rng.nextDouble() < HOME_PROB) {
					carMM.setNextRoute(workerMM.getLocation(), homeMM.getHomeLocation());
					mode = TO_HOME_MODE;
				}
				else {
					carMM.setNextRoute(workerMM.getLocation(), workerMM.getRandomOfficeLocation());
					mode = TO_WORK_MODE;
				}
			}
			break;
		case HOME_MODE:
			if (homeMM.isReady()) {
				setCurrentMovementModel(carMM);
				carMM.setNextRoute(homeMM.getHomeLocation(), workerMM.getRandomOfficeLocation());
				mode = TO_WORK_MODE;
			}
			break;
		case TO_WORK_MODE:
			if (carMM.isReady()) {
				setCurrentMovementModel(workerMM);
				mode = WORK_MODE;
			}
			break;
		case TO_HOME_MODE:
			if (carMM.isReady()) {
				setCurrentMovementModel(homeMM);
				mode = HOME_MODE;
			}
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public Coord getInitialLocation() {
		Coord homeLoc = homeMM.getHomeLocation().clone();
		homeMM.setLocation(homeLoc);
		return homeLoc;
	}

	@Override
	public MovementModel replicate() {
		return new FMIMovement(this);
	}

}
