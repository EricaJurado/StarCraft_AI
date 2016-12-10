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
	public static Position initEnemyBase = null;
	
	
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
		if (initEnemyBase == null){
			for (Unit unit : bwapi.getEnemyUnits()){
				if (unit.getType() == UnitTypes.Zerg_Hatchery){
					bwapi.drawCircle(unit.getPosition(),5, BWColor.Blue, true, false);
					initEnemyBase = unit.getPosition();
				}
				if (unit.getType() == UnitTypes.Protoss_Nexus){
					bwapi.drawCircle(unit.getPosition(),5, BWColor.Green, true, false);
					initEnemyBase = unit.getPosition();
				}
				if (unit.getType() == UnitTypes.Terran_Command_Center){
					bwapi.drawCircle(unit.getPosition(),5, BWColor.Red, true, false);
					initEnemyBase = unit.getPosition();
				}
			}
		}
		
		for (Unit unit : bwapi.getMyUnits()) {
			if (unit.getType() == UnitTypes.Terran_SCV && unit.isIdle()) {
				for (Unit enemy : bwapi.getEnemyUnits()) {
					unit.attack(enemy.getPosition(), false);
					break;
				}
			}
		}
		
		/*
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
				if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 250) {
					Position buildHere = getSuitablePos(unit, UnitTypes.Terran_Barracks, bwapi.getSelf().getStartLocation());
					if (null != buildHere) {
						unit.build(buildHere, UnitTypes.Terran_Barracks);
						builtBarracks = true;
					}
					break;
				}
			}
		}
		*/
		
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