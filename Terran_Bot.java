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

		builtBarracks = false;

		for (Unit unit : bwapi.getMyUnits()){
			if (unit.getType() == UnitTypes.Terran_SCV) {
				TerranSCV_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Marine) {
				TerranMarine_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Siege_Tank_Tank_Mode){
				Tank_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Vulture){
				Vulture_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Factory){
				Factory_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Supply_Depot){
				SupplyDepot_Count ++;
			} else if (unit.getType() == UnitTypes.Terran_Barracks){
				builtBarracks = true;
			}
			
			if (true == unit.isBeingConstructed()){
				underConstruction.add(unit);
			}
		}

		/*
		if ( 9 == bwapi.getPlayer().supplyUsed() && SupplyDepot_Count <1){
			//build Depot near ramp
			SupplyDepot_Count ++;
		}

		if (12 == bwapi.getPlayer().supplyUsed() && !builtBarracks){
			//build barracks near ramp
			//build refinery
			builtBarracks = true;
		}

		 */
		
		// check-- want !=4 or not groups of 4?
		if (builtBarracks && TerranMarine_Count!=4) {
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Barracks && bwapi.getSelf().getMinerals() >= 50 ) {
					unit.train(UnitTypes.Terran_Marine);
					TerranMarine_Count ++;
				}
			}
			TerranMarine_Count ++;
		}
		/*
		if (Marine_Count == 4 && !builtBunker){
			//build bunker in middle of chokepoint
			//move 4 marines to it
			builtBunker = true;
		}

		if (15 == bwapi.getPlayer().supplyUsed() && SupplyDepot_Count < 2) {
			//build Supply Depot near ramp
			supplyDepot_Count++;
		}

		if (16 == bwapi.getPlayer().supplyUsed() && Factory_Count < 1) {
			//build factory
			Factory_Count++;
		}

		// look in doc to see how to check what you're still building
		 */
		
		boolean factoryFinished = true;
		for (Unit building : underConstruction){
			if ( UnitTypes.Terran_Factory == building.getType()){
				factoryFinished = false;
			}
		}
		
		/*
		if (factoryFinished) {
			build machine shop
		}
		*/
		
		boolean machineShopFinished = true;
		for (Unit building : underConstruction){
			if ( UnitTypes.Terran_Machine_Shop == building.getType()){
				machineShopFinished  = false;
			}
		}
		
		/*
		if (machineShopFinished) {
			build one siege tank
			move tank behind bunker
		}

		if (24 == bwapi.getPlayer().supplyUsed()) {
			build supply depot
		}

		if (25 == bwapi.getPlayer().supplyUsed()) {
			research Siege Mode;
			haveSiege = true
		}

		if (0 < Tank_Count) {
			set tank in chokepoint to siege mode
		}

		if (28 == bwapi.getPlayer().supplyUsed()) {
			build second factory
		}

		// check
		if (int supplyTotal - in supplyUsed =< 1) {
			build supply depot
			supplyDepot_Count++
		}
		*/

		//From here, stop making SCVs, and only make Siege Tanks (fac w/ MS) and Vultures (fac w/o)
		if (machineShopFinished){
			if (Factory_Count == 2){
			// 	when second factory is done make third
			} else if (Factory_Count == 3 ){
			//	when third factory is done make fourth
			}
		}

		//When eight tanks are done, we attack with all tanks and vultures, and have all four factories pump out vultures

		//<------------------------------------------------------>

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
