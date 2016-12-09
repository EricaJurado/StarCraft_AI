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
import jnibwapi.BWAPIEventListener;

public class Testing implements BWAPIEventListener {
	private final JNIBWAPI bwapi;
	
	/** used for mineral splits */
	private final HashSet<Unit> claimedMinerals = new HashSet<>();
	
	/** when should the next overlord be spawned? */
	private int supplyCap;
	
	public static int TerranSCV_Count = 4;
	public static int TerranMarine_Count = 0;
	public static int TerranMedic_Count = 0;
	public static boolean builtBarracks = false;
	public static boolean builtAcademy = false;
	public static Position myBase;
	
	public static void main(String[] args) {
		new Testing();
	}
	
	public Testing() {
		bwapi = new JNIBWAPI(this, false);
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
		for (Unit unit : bwapi.getMyUnits()) {
			// if unit type is SCV and minerals >= 100, build supply depot in nearby available location
			if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 100) {
				Position buildHere = getSuitablePos(unit, UnitTypes.Terran_Supply_Depot, bwapi.getSelf().getStartLocation());
				if (null != buildHere) {
					unit.build(buildHere, UnitTypes.Terran_Supply_Depot);
				}
				break;
			}
		}
		
		// if we haven't already built barracks and we have a scv unit available, spend 250 minerals to build barracks
		if (!builtBarracks){
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 250) { //barracks only 150?
					Position buildHere = getSuitablePos(unit, UnitTypes.Terran_Barracks, bwapi.getSelf().getStartLocation());
					if (null != buildHere) {
						unit.build(buildHere, UnitTypes.Terran_Barracks);
						builtBarracks = true;
					}
					break;
				}
			}
		}

		if (builtBarracks && !builtAcademy) {
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 150) {
					Position buildHere = getSuitablePos(unit, UnitTypes.Terran_Academy, bwapi.getSelf().getStartLocation());
					if (null != buildHere) {
						unit.build(buildHere, UnitTypes.Terran_Academy);
						builtAcademy = true;
					}
					break;
				}
			}
		}

		// if marine count is less than 3 and we have barracks, build another marine
		if (TerranMarine_Count < 3){
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Barracks && bwapi.getSelf().getMinerals() >= 50 ) {
					unit.train(UnitTypes.Terran_Marine);
					TerranMarine_Count ++;
				}
			}
		}



		// if marine count is 3 build medic
		// could add something like && (TerranMarine_Count / TerranMedic_Count < 3)
		if (TerranMarine_Count >= 3) {
			for (Unit unit: bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Academy && bwapi.getSelf().getMinerals() >= 50 ) {
					unit.train(UnitTypes.Terran_Medic);
					TerranMedic_Count ++;
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
	
	//TODO: build choke points
	
	// TODO: Offset to not block minerals
	// TODO: Building in proper location (radius?)
	public Position getSuitablePos (Unit builder, UnitType buildingType, Position pos){
		Position finalPos = null;
		int maxDist = 3;
		int stopDist = 40;
		
		while ((maxDist < stopDist) && (null == finalPos)){
			for (int i=pos.getPX()-maxDist; i <=pos.getPX()+maxDist; i++){
				for (int j=pos.getPY()-maxDist; j<=pos.getPY()+maxDist; j++) {
					if (bwapi.canBuildHere(builder, pos, buildingType, false)){
						boolean unitsInWay = false;
						for (Unit unit : bwapi.getAllUnits()) {
							if (unit.getID() == builder.getID()) {
								continue;
							}
							if ((Math.abs(unit.getPosition().getPX()-i) < 4 ) && (Math.abs(unit.getPosition().getPY()-j) < 4)) {
								unitsInWay = true;
							}
						}
						if (!unitsInWay) {
							return new Position(i,j);
						}
					}
					Position test = new Position(i,j);
					bwapi.drawCircle(test, 5, BWColor.Red, true, false);
				}
			}
			maxDist += 2;
		}
		
		if (null == finalPos) {
			System.out.println("Can't find suitable building position for " + buildingType.toString());
		}
		
		return finalPos;
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
