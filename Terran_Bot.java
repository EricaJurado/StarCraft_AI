package bot;

		import java.util.HashSet;
		import java.util.List;

/**
 * Example of a Java AI Client that does nothing.
 */
		import jnibwapi.BWAPIEventListener;
		import jnibwapi.BaseLocation;
		import jnibwapi.ChokePoint;
		import jnibwapi.JNIBWAPI;
		import jnibwapi.Position;
		import jnibwapi.Position.PosType;
		import jnibwapi.Unit;
		import jnibwapi.types.UnitType;
		import jnibwapi.types.UnitType.UnitTypes;
		import jnibwapi.Map;
		import jnibwapi.util.BWColor;
		import jnibwapi.Player;


public class Terran_Bot implements BWAPIEventListener {
	private final JNIBWAPI bwapi;

	/** used for mineral splits */
	private final HashSet<Unit> claimedMinerals = new HashSet<>();

	/** when should the next overlord be spawned? */
	private int supplyCap;

	public static int TerranSCV_Count = 4;
	public static int TerranMarine_Count = 0;
	public static boolean builtBarracks = false;
	public static Position myBase;
	public static BaseLocation baseLocation;
	public static Position initEnemybasePosition = null;
	public static Position initBasePosition = null;

	public static void main(String[] args) {
		new Terran_Bot();
	}

	public Terran_Bot() {
		bwapi = new JNIBWAPI(this, true);
		bwapi.start();
	}

	@Override
	public void connected() {}

	@Override
	public void matchStart() {
		bwapi.enableUserInput();
		bwapi.enablePerfectInformation();
		bwapi.setGameSpeed(0);

		// reset agent state
		claimedMinerals.clear();
		supplyCap = 0;

	}

	@Override
	public void matchFrame() {
		// build supply depot
		// TODO: Determine strategy/timing of building depot
		if(initEnemybasePosition == null){
			for(Unit unit : bwapi.getEnemyUnits()){
				if(unit.getType() == UnitTypes.Zerg_Hatchery){
					bwapi.drawCircle(unit.getPosition(), 5, BWColor.Blue, true, false);
					initEnemybasePosition = unit.getPosition();
				}
				else if (unit.getType() == UnitTypes.Protoss_Nexus){
					bwapi.drawCircle(unit.getPosition(), 5, BWColor.Blue, true, false);
					initEnemybasePosition = unit.getPosition();
				}
				else if (unit.getType() == UnitTypes.Terran_Command_Center){
					bwapi.drawCircle(unit.getPosition(), 5, BWColor.Blue, true, false);
					initEnemybasePosition = unit.getPosition();
				}
			}
		}

		if(initBasePosition == null)
			for(Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Command_Center) {
					initBasePosition = new Position(unit.getPosition().getBX(), unit.getPosition().getBY(), PosType.BUILD);
				}
			}

		System.out.println(initBasePosition.getBX());
		System.out.println(initBasePosition.getBY());

		bwapi.drawCircle(initBasePosition, 5, BWColor.Cyan, true, false);

		// if we haven't already built barracks and we have a scv unit available, spend 250 minerals to build barracks
		if (!builtBarracks){
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 150) {
					int differenceX = (initBasePosition.getBX() - initEnemybasePosition.getBX());
					int differenceY = (initBasePosition.getBY() - initEnemybasePosition.getBY());
					float magnitude = (float)Math.sqrt(Math.pow(differenceX, 2) + Math.pow(differenceY, 2));
					float normalizedDifferenceX = differenceX/magnitude;
					float normalizedDifferenceY = differenceY/magnitude;
					int distance = 5;
					int buildHereX = initBasePosition.getBX() - (int)(normalizedDifferenceX * distance);
					int buildHereY = initBasePosition.getBY() - (int)(normalizedDifferenceY * distance) ;
					Position buildHere = new Position(buildHereX, buildHereY, PosType.BUILD);
					bwapi.drawCircle(buildHere, 10, BWColor.Red, true, false);
					if (null != buildHere && false != bwapi.canBuildHere(buildHere,UnitTypes.Terran_Barracks, true)){
						unit.build(buildHere, UnitTypes.Terran_Barracks);
						break;
					}
				}
			}
		}



		// if marine count is less than 4 and we have barracks, build another marine
		if (TerranMarine_Count < 4){
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Barracks && bwapi.getSelf().getMinerals() >= 50 ) {
					unit.train(UnitTypes.Terran_Marine);
					TerranMarine_Count ++;
				}
			}
		}

		// spawn a unit?
		// if scv counter is less than 6, build another scv
		if (TerranSCV_Count < 6){
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Command_Center && bwapi.getSelf().getMinerals() >= 50 ) {
					unit.train(UnitTypes.Terran_SCV);
					TerranSCV_Count ++;
				}
			}
		}


		// collect minerals
		// currently all (idle) scv units near minerals will collect minerals
		for (Unit unit : bwapi.getMyUnits()) {

			if (unit.getType() == UnitTypes.Terran_SCV) {

				if (unit.isIdle()) {
					for (Unit minerals : bwapi.getNeutralUnits()){
						if (minerals.getType().isMineralField()){
							double dist = unit.getDistance(minerals);

							if (dist < 300) {
								unit.rightClick(minerals, false);
								claimedMinerals.add(minerals);
								break;
							}
						}

					}

				}

			}
		}
	}

	public Position Spiral(Position pos){
		for(int x = -2 x <= 2; x++){
			for(int y = -2; y <= 2; y++){
				if((x == 0 && y == 0) || Math.abs(x) == 1 || Math.abs(y) == 1){
					continue;
				}

				int checkX = pos.getBX() + x;
				int checkY = pos.getBY() + y;

				new Position point = (checkX, checkY, PosType.BUILD);
				boolean canBuild = point.canBuild();

				if (canBuild){
					return point;
				}

			}
		}

		return null;

	}


	//TODO: build choke points


	@Override
	public void keyPressed(int keyCode) {}
	@Override
	public void matchEnd(boolean winner) {}
	@Override
	public void sendText(String text) {}
	@Override
	public void receiveText(String text) {}
	@Override
	public void nukeDetect(Position p) {}
	@Override
	public void nukeDetect() {}
	@Override
	public void playerLeft(int playerID) {}
	@Override
	public void unitCreate(int unitID) {}
	@Override
	public void unitDestroy(int unitID) {}
	@Override
	public void unitDiscover(int unitID) {}
	@Override
	public void unitEvade(int unitID) {}
	@Override
	public void unitHide(int unitID) {}
	@Override
	public void unitMorph(int unitID) {}
	@Override
	public void unitShow(int unitID) {}
	@Override
	public void unitRenegade(int unitID) {}
	@Override
	public void saveGame(String gameName) {}
	@Override
	public void unitComplete(int unitID) {}
	@Override
	public void playerDropped(int playerID) {}
}
