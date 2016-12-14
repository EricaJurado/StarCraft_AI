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
	public static boolean builtBarracks = false;
	public static Position initEnemyBase = null;
	public static ArrayList<Unit> underConstruction = new ArrayList<Unit>();
	public static String enemyType = "";
	public static boolean determinedEnemy = false;
	public static Position initBasePosition = null;
	public static Position myPos = null;
	public static Position closestCP = null;
	public static Position r1 = null;
	public static Position r2 = null;

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
		if(initBasePosition == null) {
			for(Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Command_Center) {
					myPos = unit.getPosition();
				}
			}
			
			List<Position> choke = artichoke();
			closestCP = choke.get(0);
			r1 = choke.get(1);
			r2 = choke.get(2);
		}
		
		bwapi.drawCircle(closestCP, 5, BWColor.Purple, true, false);
		Position test = Spiral(r1);
		bwapi.drawCircle(test, 5, BWColor.Yellow, true, false);
		
		
		for (Unit unit : bwapi.getMyUnits()) {
			// if unit type is SCV and minerals >= 100, build supply depot in nearby available location
			if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 100) {
				unit.build(r1, UnitTypes.Terran_Supply_Depot);
				break;
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
			double diffX = Math.abs(myPos.getPX() - rp.getPX());
			double diffY = Math.abs(myPos.getPY() - rp.getPY());
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
			double diffX = Math.abs(myPos.getPX() - cp.getPX());
			double diffY = Math.abs(myPos.getPY() - cp.getPY());
			double Z = Math.sqrt((diffX*diffX+diffY*diffY));
			if (Z<= smallestDist || smallestDist == -1){
				smallestDist = Z;
				closestCP = cp;
				smallestIndexCP = index;
			}
			index ++;
		}

		Position r1 = chokepoints.get(smallestIndexCP).getFirstSide();
		Position r2 = chokepoints.get(smallestIndexCP).getSecondSide();
		bwapi.drawCircle(r1,5, BWColor.Red, true, false);
		bwapi.drawCircle(r2, 5, BWColor.Green, true, false);
		bwapi.drawCircle(closestCP, 5, BWColor.White, true, false);
		bwapi.drawCircle(closestR, 5, BWColor.Orange, true, false);
		
		List<Position> cp = new ArrayList<Position>(3);
		cp.add(closestCP);
		cp.add(r1);
		cp.add(r2);
		
		return cp;
	}
	

	public Position Spiral(Position pos){
		int radius = 0;
		boolean canBuild = false;
		Position point = null;
		
		while (!canBuild){
			for(int x = -radius; x <= radius; x++){
				for(int y = -radius; y <= radius; y++){
					if(x == 0 && y == 0){
						continue;
					}
	
					int checkX = pos.getBX() + x;
					int checkY = pos.getBY() + y;
	
					point = new Position (checkX, checkY, PosType.BUILD);
					canBuild = bwapi.getMap().isBuildable(point);
	
				}
			}
			radius ++;
		}
		
		return point;

	}



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
