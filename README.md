# StarCraft_AI

**Strategy Overview (TvP)** (Derivation of the Siege Expand)

Begin by building SCVs whenever resources allow

if (supplyUsed == 9 && supplyDepot_Count < 1) {build Supply Depot near ramp; supplyDepot_Count++}
    
if (supplyUsed == 12 && !builtBarracks) {build a barracks near ramp; build refinery; builtBarracks = true}   

if (builtBarracks && marine_count != 4) {build marines, marine_count++ with each}

if (marine_count = 4 && !builtBunker) {build bunker in middle of chokepoint; move four marines to it; builtBunker = true}

if (supplyUsed == 15 && supplyDepot_Count < 2) {build Supply Depot near ramp; supplyDepot_Count++}
    
if (supplyUsed == 16 && factory_Count < 1) {build factory; factory_Count++}
    
if (factory is finished) {build machine shop}

when (machine shop is finished) {build one siege tank; move tank behind bunker}

if (supplyUsed == 24) {build supply depot}

if (supplyUsed == 25) {research Siege Mode; haveSiege = true}

if (haveSiege) {set tank in chokepoint to siege mode}

if (supplyUsed == 28) {build second factory}

if (int supplyTotal - in supplyUsed =< 1) {build supply depot; supplyDepot_Coutn++}

    -From here, stop making SCVs, and only make Siege Tanks (fac w/ MS) and Vultures (fac w/o)

when (second factory is done) {make third}

when (third factory is done) {give third machine shop}

when (machine shop is done) {build fourth}

When eight tanks are done, we attack with all tanks and vultures, and have all four factories pump out vultures



**Strategy Overview (TvZ)** (1.5 rax)

Begin by building SCVs whenever resources allow

if (supplyUsed == 9) {build supply depot}

if (supplyUsed == 11) {build barracks}

if (supplyUsed == 15) {build supply depot}

if (supplyUsed == 22) {supply depot}

if (supplyUsed == 23) {refinery}

if (supplyUsed == 25) {bunker in chokepoint}

if (supplyUsed == 30) {academy}

when (academy is built) {research stim pack}

if (supplyUsed == 32) {barracks}

if (supplyUsed == 35) {barracks}
    
if (supplyUsed == 40) {2 barracks, supply depot}

When you have at least 16 marines and 4 medics, attack.

