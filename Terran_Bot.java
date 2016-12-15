package bot;

		import java.util.ArrayList;
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
import jnibwapi.Region;
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

	public static int TerranSCV_Count = 0;
	public static int TerranMarine_Count = 0;
	public static int Tank_Count = 0;
	public static int Vulture_Count = 0;
	public static int Factory_Count = 0;
	public static int SupplyDepot_Count = 0;
	public static int Bunker_Count = 0;
	
	public static boolean builtBarracks = false;
	public static boolean builtDepot = false;
	public static boolean builtMaxBunkers = false;

	public static BaseLocation baseLocation;

	public static Position myBase;
	public static Position initEnemybasePosition = null;
	public static Position initBasePosition = null;
	public static Position closestCP = null;
	public static Position r1 = null;
	public static Position r2 = null;
	
	public static Unit scv = null;
	public static boolean issuedMove = false;
	
	public static Position barracksPos = null;
	

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
		//TODO: supply depot loop (total supplies - supplies used)=1 then we've used up all supplies and need to build new
		//TODO: build academy so we can build medic
		//TODO: build bunker at enemy chokepoint
		
		TerranSCV_Count = 0;
		TerranMarine_Count = 0;
		Vulture_Count = 0;
		Factory_Count = 0;
		SupplyDepot_Count = 0;
		Bunker_Count = 0;
		
		for (Unit unit : bwapi.getMyUnits()){
			if (unit.getType() == UnitTypes.Terran_SCV){
				TerranSCV_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Marine){
				TerranMarine_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Vulture){
				Vulture_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Factory){
				Factory_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Supply_Depot){
				SupplyDepot_Count++;
			} else if (unit.getType() == UnitTypes.Terran_Bunker){
				Bunker_Count ++;
			}
			
		}
		
		if (Bunker_Count >=3){
			builtMaxBunkers = true;
		}
		
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

		if(initBasePosition == null) {
			for(Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Command_Center) {
					initBasePosition = unit.getPosition();
				}
			}
			
			List<Position> choke = artichoke();
			closestCP = choke.get(0);
			r1 = choke.get(1);
			r2 = choke.get(2);
		}
	
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
					if (null != buildHere) {
						Position here = Spiral(unit, buildHere, UnitTypes.Terran_Barracks);
						barracksPos = here;
						bwapi.drawCircle(here, 5, BWColor.Grey, true, false);
					}
				}
			}
		}
		
		
		for (Unit unit : bwapi.getMyUnits()){
			if (unit.getType() == UnitTypes.Terran_Barracks){
				builtBarracks = true;
				break;
			}
		}
		
		for (Unit unit : bwapi.getMyUnits()){
			if (unit.getType() == UnitTypes.Terran_Supply_Depot){
				builtDepot = true;
			}
		}
		
		if (scv==null){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					scv = unit;
				}
			}
		}

		bwapi.drawCircle(closestCP, 5, BWColor.White, true, false);
		if(builtDepot){
			scv.move(closestCP, false);
		}
		
		if (builtDepot && !builtMaxBunkers){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					Position here = Spiral(unit, closestCP, UnitTypes.Terran_Bunker);
					bwapi.drawCircle(here, 5, BWColor.Green, true, false);
					break;
				}
			}
		}
		
		if(bwapi.getSelf().getSupplyUsed()>=9 && builtBarracks && !builtDepot){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					Position here = Spiral(unit, barracksPos, UnitTypes.Terran_Supply_Depot);
					bwapi.drawCircle(here, 5, BWColor.Green, true, false);
					break;
				}
			}
		}
		
		if (builtMaxBunkers && bwapi.getSelf().getMinerals()>=50){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_Barracks){
					unit.train(UnitTypes.Terran_Marine);
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
					break;
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

	public List<Position> artichoke(){
		List<Region> regions = bwapi.getMap().getRegions();
		List<ChokePoint> chokepoints = bwapi.getMap().getChokePoints();
		List<Position> regionPos = new ArrayList<Position>(regions.size());
		List<Position> chokePos = new ArrayList<Position>(chokepoints.size());
		for (Region r : regions){
			Position p = r.getCenter();
			regionPos.add(p);
		}
		
		Position closestR = null; //closest region pos to my start pos
		double smallestDist = -1;
		int smallestIndexR = 0;
		int	index = 0;
		for (Position rp : regionPos){
			double diffX = Math.abs(initBasePosition.getPX() - rp.getPX());
			double diffY = Math.abs(initBasePosition.getPY() - rp.getPY());
			double Z = Math.sqrt((diffX*diffX+diffY*diffY));
			if (Z<= smallestDist || smallestDist == -1){
				smallestDist = Z;
				closestR = rp;
				smallestIndexR = index;
			}
			index ++;
		}
		
		for (ChokePoint cp : chokepoints){
			Position p = cp.getCenter();
			chokePos.add(p);
		}

		Position closestCP = null;
		smallestDist = -1;
		int smallestIndexCP = 0;
		index = 0;
		for (Position cp : chokePos){
			double diffX = Math.abs(initBasePosition.getWX() - cp.getWX());
			double diffY = Math.abs(initBasePosition.getWY() - cp.getWY());
			double Z = Math.sqrt((diffX*diffX+diffY*diffY));
			if (Z<= smallestDist || smallestDist == -1){
				smallestDist = Z;
				closestCP = new Position(cp.getWX(), cp.getWY(), PosType.WALK);
				smallestIndexCP = index;
			}
			index ++;
		}

		Position r1 = chokepoints.get(smallestIndexCP).getFirstSide();
		Position r2 = chokepoints.get(smallestIndexCP).getSecondSide();
		
		List<Position> cp = new ArrayList<Position>(3);
		cp.add(closestCP);
		cp.add(r1);
		cp.add(r2);
		
		return cp;
	}

	public Position Spiral(Unit unit, Position pos, UnitType building){
		int radius = 2;
		boolean canBuild = false;
		Position point = null;
		
		while (!canBuild){
			for(int x = -radius; x <= radius; x++){
				for(int y = -radius; y <= radius; y++){
					if((x == 0 && y == 0) || Math.abs(x) == 1 || Math.abs(y) == 1){
						continue;
					}
	
					int checkX = pos.getBX() + x;
					int checkY = pos.getBY() + y;
	
					point = new Position (checkX, checkY, PosType.BUILD);
					bwapi.drawCircle(point, 5, BWColor.Teal, true, false);
					
					if (bwapi.canBuildHere(point, building, true)){
						unit.build(point, building);
						canBuild = true;
					}
				}
			}
			radius = radius + 2;
		}
		
		return point;
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
