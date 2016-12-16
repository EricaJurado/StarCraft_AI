package bot;

		import java.util.ArrayList;
		import java.util.HashSet;
		import java.util.List;
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
	private final HashSet<Unit> claimedMinerals = new HashSet<>();

	//hold count of units to be recounter each match frame
	public static int TerranSCV_Count = 0;
	public static int TerranMarine_Count = 0;
	public static int Tank_Count = 0;
	public static int Vulture_Count = 0;
	public static int Factory_Count = 0;
	public static int SupplyDepot_Count = 0;
	public static int Bunker_Count = 0;

	//booleans used to determine if we have building of specified time (or quantity)
	public static boolean builtBarracks = false;
	public static boolean builtDepot = false;
	public static boolean builtRefinery = false;
	public static boolean builtMaxBunkers = false;
	public static boolean builtFactory = false;
	//boolean used to determine if we issues command to scv unit
	public static boolean issuedMove = false;

	//public static BaseLocation baseLocation;

	//public static Position myBase;
	public static Position initEnemybasePosition = null;
	public static Position initBasePosition = null;
	//will hold position of choke point closest to our inital base
	public static Position closestCP = null;
	//hold position of region 1 and 2 of choke point respectively
	public static Position r1 = null;
	public static Position r2 = null;
	public static Position barracksPos = null;

	//"scout" scv unit to remove fog of war at choke point
	public static Unit scv = null;

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
		claimedMinerals.clear();
	}

	@Override
	public void matchFrame() {
		//Recounts all of our units every frame
		countUnits();

		//If we don't have our own initial base position, go get it.
		if(initBasePosition == null) {
			getInitBaseLocation();
		}

		//If we haven't built barracks yet, go build it.
		if (!builtBarracks){
			buildBaseBarrack();
		}

		//Checks if we have barracks and depots and sets bools accordingly.
		builtBarracks = checkIfBuilt(UnitTypes.Terran_Barracks);
		builtDepot = checkIfBuilt(UnitTypes.Terran_Supply_Depot);
		builtRefinery = checkIfBuilt(UnitTypes.Terran_Refinery);
		builtFactory = checkIfBuilt(UnitTypes.Terran_Factory);

		//If our scv to stand at chokepoint is null, go designate one.
		if (scv==null){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					scv = unit;
				}
			}
		}

		//If we've got our depot and the chokepoint we'd like to fortify has been set, ask the scv to stand there so the fog of war will be removed in the area.
		if(builtDepot && closestCP!=null){
			scv.move(closestCP, false);
			scv.move(initBasePosition, false);
		}

		//If we haven't built a depot, go build one near our base
		if (!builtDepot){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					Position here = Spiral(unit, initBasePosition, UnitTypes.Terran_Supply_Depot);
					bwapi.drawCircle(here, 5, BWColor.Green, true, false);
					break;
				}
			}
		}

		//If we haven't built a barracks but we've built a depot, built barracks next
		if(builtDepot && !builtBarracks){
			buildBaseBarrack();
		}

		//If we've built our depot, barracks, and have fortified our chokepoint with bunkers, and have built a refinery, go built a factory near our base.
		if (builtDepot && builtBarracks && builtMaxBunkers && builtRefinery){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					Position here = Spiral(unit, initBasePosition ,UnitTypes.Terran_Factory);
					break;
				}
			}
		}

		//If we've built a depot, barracks, fortified out chokepoint with bunkers and we haven't built a refinery, go build a refinery.
		if(builtDepot && builtBarracks && builtMaxBunkers && !builtRefinery){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					Position here = Spiral(unit, initBasePosition ,UnitTypes.Terran_Refinery);
					break;
				}
			}
		}

		//If we've built our depot and we haven't maxed out our bunker count
		if (builtDepot && builtBarracks && !builtMaxBunkers){
			for (Unit unit : bwapi.getMyUnits()){
				if (unit.getType() == UnitTypes.Terran_SCV){
					if (closestCP == null){
						//artichoke will get information about choke point we want to fortify
						List<Position> choke = artichoke();
						closestCP = choke.get(0);
						r1 = choke.get(1);
						r2 = choke.get(2);
					}
					//Finds suitable position for bunker and builds it there
					Position here = Spiral(unit, closestCP, UnitTypes.Terran_Bunker);
					bwapi.drawCircle(here, 5, BWColor.Green, true, false);
					break;
				}
			}
		}

		//If we've built a depot, barack, and maxed out bunkers, determine if we need bunkers. If we do then build another one near our base.
		if(builtDepot && builtBarracks && builtMaxBunkers){
			if (determineNeedDepots()){
				for (Unit unit: bwapi.getMyUnits()){
					if (unit.getType() == UnitTypes.Terran_SCV){
						Position here = Spiral(unit, initBasePosition, UnitTypes.Terran_Supply_Depot);
						break;
					}
				}
			}
		}

		// if marine count is less than 4 and we have barracks, build another marine.
		if (TerranMarine_Count < 4){
			for (Unit unit : bwapi.getMyUnits()) {
				if (unit.getType() == UnitTypes.Terran_Barracks && bwapi.getSelf().getMinerals() >= 50) {
					unit.train(UnitTypes.Terran_Marine);
					break;
				}
			}
		}

		//If we've built a factory, go pump out vultures.
		if(builtFactory){
			for(Unit unit : bwapi.getMyUnits()){
				if(unit.getType() == UnitTypes.Terran_Factory && bwapi.getSelf().getMinerals() >= 75){
					unit.train(UnitTypes.Terran_Vulture);
				}
			}
		}

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

		//If our marine count is >= 4 and we've maxed out our bunkers, send marines to bunker.
		if(TerranMarine_Count >= 4 && builtMaxBunkers){
			for (Unit unit : bwapi.getMyUnits()){
				if(unit.getType() == UnitTypes.Terran_Marine){
					for(Unit bunker : bwapi.getMyUnits()){
						if(bunker.getType() == UnitTypes.Terran_Bunker){
							unit.rightClick(bunker, false);
						}
					}
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

							if (dist < 400) {
								unit.rightClick(minerals, false);
								claimedMinerals.add(minerals);
								break;
							}
						}

					}

				}

			}
		}

		//Spam our enemies with all our vultures
		for (Unit unit : bwapi.getMyUnits()){
			if(unit.getType() == UnitTypes.Terran_Vulture){
				unit.rightClick(initEnemybasePosition, false);
			}
		}

	}

	//Determine if diff b/w supply total and used is <=3 to build more bunker
	public boolean determineNeedDepots(){
		if(bwapi.getSelf().getSupplyTotal() - bwapi.getSelf().getSupplyUsed() <= 3){
			return true;
		} else {
			return false;
		}
	}

	public Position Spiral(Unit unit, Position pos, UnitType building){
		/*
		Spiral takes in unit (which will do the building), an initial start position,
		and the type of building to build (since not all buildings are the same size).
		It will find the closest available position to build by spiraling outwards
		from the pos you pass in.
		 */

		int radius = 2;
		boolean canBuild = false;
		Position point = null;

		//While you haven't found a position you can build at, keep looking!
		while (!canBuild){
			//Spiral route
			for(int x = -radius; x <= radius; x++){
				for(int y = -radius; y <= radius; y++){
					if((x == 0 && y == 0) || Math.abs(x) == 1 || Math.abs(y) == 1){
						continue;
					}

					int checkX = pos.getBX() + x;
					int checkY = pos.getBY() + y;

					//build tile to be checked
					point = new Position (checkX, checkY, PosType.BUILD);

					//Checks if building can be built at found point
					if (bwapi.canBuildHere(point, building, true)){
						unit.build(point, building);
						canBuild = true;
					}
				}
			}
			//Increases radius to look out further from initial point.
			radius = radius + 2;
		}

		//Return point that it was built out
		return point;
	}

	public List<Position> artichoke(){
		/*
		 Artichoke determines the closest chokepoint near our own base
		 using our base's region and interating through all chokepoints
		 to see what's closest. It will return the chokepoint and region
		 1 and 2 position of that chokepoint. (Region info not currently
		 being used.)
		*/
		List<Region> regions = bwapi.getMap().getRegions();
		List<ChokePoint> chokepoints = bwapi.getMap().getChokePoints();
		//RegionPos and chokePos will hold the positions of all regions and chokepoints so we can determine dist from our base.
		List<Position> regionPos = new ArrayList<Position>(regions.size());
		List<Position> chokePos = new ArrayList<Position>(chokepoints.size());

		//Adds all regions (center) to regionPos list
		for (Region r : regions){
			Position p = r.getCenter();
			regionPos.add(p);
		}

		Position closestR = null; //closest region pos to my start pos
		double smallestDist = -1;
		int	index = 0;
		for (Position rp : regionPos){
			double diffX = Math.abs(initBasePosition.getPX() - rp.getPX());
			double diffY = Math.abs(initBasePosition.getPY() - rp.getPY());
			double Z = Math.sqrt((diffX*diffX+diffY*diffY));
			if (Z<= smallestDist || smallestDist == -1){
				smallestDist = Z;
				closestR = rp;
			}
			index ++;
		}

		//Adds all chokepoints (center) to chokePos list
		for (ChokePoint cp : chokepoints){
			Position p = cp.getCenter();
			chokePos.add(p);
		}

		Position closestCP = null; //closest chokepoint to my start pos
		smallestDist = -1;
		int smallestIndexCP = 0;
		index = 0;
		for (Position cp : chokePos){
			double diffX = Math.abs(initBasePosition.getWX() - cp.getWX());
			double diffY = Math.abs(initBasePosition.getWY() - cp.getWY());
			double Z = Math.sqrt((diffX*diffX+diffY*diffY));
			if (Z<= smallestDist || smallestDist == -1){
				smallestDist = Z;
				smallestIndexCP = index;
				closestCP = new Position(cp.getWX(), cp.getWY(), PosType.WALK);
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

	public boolean checkIfBuilt(UnitType building){
		//Generic function to see if we've built a particular kind of building or not
		for (Unit unit : bwapi.getMyUnits()){
			if (unit.getType() == building){
				return true;
			}
		}
		return false;
	}

	public void countUnits(){
		//Counts our units. Called each match frame.
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

		if (Bunker_Count >=1){
			builtMaxBunkers = true;
		}
	}

	public void buildBaseBarrack(){
		//Builds the base barracks between our base and the enemy base
		if (initEnemybasePosition == null){
			//If the enemy's base position is null, you need to go get it!
			getEnemybasePosition();
		}
		for (Unit unit : bwapi.getMyUnits()) {
			//Checking to see if it's possible ot build
			if (unit.getType() == UnitTypes.Terran_SCV && bwapi.getSelf().getMinerals() >= 150) {
				//Note that all are in build tiles
				int differenceX = (initBasePosition.getBX() - initEnemybasePosition.getBX());
				int differenceY = (initBasePosition.getBY() - initEnemybasePosition.getBY());
				float magnitude = (float)Math.sqrt(Math.pow(differenceX, 2) + Math.pow(differenceY, 2));
				float normalizedDifferenceX = differenceX/magnitude;
				float normalizedDifferenceY = differenceY/magnitude;
				int distance = 5;
				//Applies calculated offset to our base location to set ideal position for barrack
				int buildHereX = initBasePosition.getBX() - (int)(normalizedDifferenceX * distance);
				int buildHereY = initBasePosition.getBY() - (int)(normalizedDifferenceY * distance) ;
				Position buildHere = new Position(buildHereX, buildHereY, PosType.BUILD);
				bwapi.drawCircle(buildHere, 10, BWColor.Red, true, false);
				if (null != buildHere) {
					//Spiral is called so if the ideal postion is not buildable, it'll determine the next available space and build the barracks there
					Position here = Spiral(unit, buildHere, UnitTypes.Terran_Barracks);
					barracksPos = here;
					bwapi.drawCircle(here, 5, BWColor.Grey, true, false);
					break;
				}
			}
		}
	}

	public void getInitBaseLocation(){
		//Gets our base position
		for(Unit unit : bwapi.getMyUnits()) {
			if (unit.getType() == UnitTypes.Terran_Command_Center) {
				initBasePosition = unit.getPosition();
			}
		}
		return;
	}

	public void getEnemybasePosition(){
		//Gets initial enemy base positon by looking for the initial building they will have
		for(Unit unit : bwapi.getEnemyUnits()){
			if(unit.getType() == UnitTypes.Zerg_Hatchery){
				initEnemybasePosition = unit.getPosition();
			}
			else if (unit.getType() == UnitTypes.Protoss_Nexus){
				initEnemybasePosition = unit.getPosition();
			}
			else if (unit.getType() == UnitTypes.Terran_Command_Center){
				initEnemybasePosition = unit.getPosition();
			}
		}
		return;
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