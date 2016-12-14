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
import jnibwapi.types.TechType;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.Map;
import jnibwapi.util.BWColor;
import jnibwapi.Player;
import jnibwapi.BWAPIEventListener;

import static jnibwapi.types.UnitType.UnitTypes.Terran_Bunker;

public class BuildOrderTest implements BWAPIEventListener {
    private final JNIBWAPI bwapi;

    /**
     * used for mineral splits
     */
    private final HashSet<Unit> claimedMinerals = new HashSet<>();

    /**
     * when should the next overlord be spawned?
     */
    private int supplyCap;

    public static boolean buildSCVs = true;
    public static int TerranMarine_Count = 0;
    public static int TerranMedic_Count = 0;
    public static int TerranTank_Count = 0;
    public static int depot_Count = 0;
    public static int factory_Count = 0;
    public static boolean builtRefinery = false;
    public static boolean builtBarracks = false;
    public static boolean builtBunker = false;
    public static boolean inBunker = false;
    public static boolean builtMachineShop = false;
    public static Position myBase;

    private Unit ourBuilder;

    public static void main(String[] args) {
        new BuildOrderTest();
    }

    public BuildOrderTest() {
        bwapi = new JNIBWAPI(this, false);
        bwapi.start();
    }

    @Override
    public void connected() {
    }

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

        if (buildSCVs) {
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_Command_Center && bwapi.getSelf().getMinerals() >= 50) {
                    unit.train(UnitTypes.Terran_SCV);
                }
            }
        }

        //supply depot when supply at 9
        if (bwapi.getSelf().getSupplyUsed() == 9 && depot_Count < 1) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 100) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Supply_Depot);
                    ourBuilder = null;
                    buildSCVs = true;
                    depot_Count++;
                    break;
                }
            }
        }

        //refinery when supply at 11
        if (bwapi.getSelf().getSupplyUsed() == 11 && !builtRefinery) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 100) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Supply_Depot);
                    ourBuilder = null;
                    buildSCVs = true;
                    builtRefinery = true;
                    break;
                }
            }
        }

        //barracks when supply at 12
        if (bwapi.getSelf().getSupplyUsed() == 9 && !builtBarracks) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 150) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Barracks);
                    ourBuilder = null;
                    buildSCVs = true;
                    builtBarracks = true;
                    break;
                }
            }
        }

        // if marine count is less than 3 and we have barracks, build another marine
        if (builtBarracks && TerranMarine_Count < 4) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_Barracks && bwapi.getSelf().getMinerals() >= 50) {
                    unit.train(UnitTypes.Terran_Marine);
                    TerranMarine_Count++;
                }
            }
        }

        //if you have your 4 marines, build a bunker in the chokepoint
        if (TerranMarine_Count == 4 && !builtBunker) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 150) { //chokepoint
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Bunker);
                    ourBuilder = null;
                    buildSCVs = true;
                    builtBunker = true;
                    break;
                }
            }
        }

        //move your marines into bunker
        if (builtBunker && !inBunker) {
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_Marine) {
                    unit.move(Terran_Bunker.getPosition(), false);
                    inBunker = true;
                }
            }
        }


        //supply depot when supply at 15
        if (bwapi.getSelf().getSupplyUsed() == 15 && depot_Count < 2) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 100) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Supply_Depot);
                    ourBuilder = null;
                    buildSCVs = true;
                    depot_Count++;
                    break;
                }
            }
        }

        //factory when supply at 16
        if (bwapi.getSelf().getSupplyUsed() == 16 && factory_Count < 1) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 200 && bwapi.getSelf().getGas() >= 100) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Factory);
                    ourBuilder = null;
                    buildSCVs = true;
                    factory_Count++;
                    break;
                }
            }
        }

        if (factory_Count == 1 && !builtMachineShop) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 50 && bwapi.getSelf().getGas() >= 50) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Machine_Shop);
                    ourBuilder = null;
                    buildSCVs = true;
                    builtMachineShop = true;
                    break;
                }
            }
        }

        //supply depot when supply at 24
        if (bwapi.getSelf().getSupplyUsed() == 24 && depot_Count < 3) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 100) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Supply_Depot);
                    ourBuilder = null;
                    buildSCVs = true;
                    depot_Count++;
                    break;
                }
            }
        }

        //research siege when supply at 25
        if (bwapi.getSelf().getSupplyUsed() == 25) {
            for (TechType Tank_Siege_Mode : TechType.TechTypes.getAllTechTypes()) {
                if (!bwapi.getSelf().isResearching(Tank_Siege_Mode)) {
                    // then fucking research it mate
                }
            }
        }

        //factory when supply at 28
        if (bwapi.getSelf().getSupplyUsed() == 28 && factory_Count < 2) {
            buildSCVs = false;
            for (Unit unit : bwapi.getMyUnits()) {
                if (unit.getType() == UnitTypes.Terran_SCV) {
                    ourBuilder = unit;
                    break;
                }
            }

            for (Unit unit : bwapi.getMyUnits()) {
                //put where to build here
                if (unit.getType() == UnitTypes.Zerg_Overlord && bwapi.getSelf().getMinerals() >= 200 && bwapi.getSelf().getGas() >= 100) {
                    ourBuilder.build(unit.getPosition(), UnitTypes.Terran_Factory);
                    ourBuilder = null;
                    factory_Count++;
                    break;
                }
            }
        }

        //stop making SCVs
        if (bwapi.getSelf().getSupplyUsed() >= 28) {
            buildSCVs = false;
        }


        // collect minerals
        // currently all (idle) scv units near minerals will collect minerals
        for (Unit unit : bwapi.getMyUnits()) {

            if (unit.getType() == UnitTypes.Terran_SCV) {

                if (unit.isIdle()) {
                    for (Unit minerals : bwapi.getNeutralUnits()) {
                        if (minerals.getType().isMineralField()) {
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
