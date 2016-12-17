# StarCraft_AI


**Strategy Overview**

This goal of this Terran bot is to quickly establish a basic defense at the chokepoint nearest our base before building large amounts of Vultures to attack and hopefully overwhelm the enemy's base. 

After bringing our SCV count to 6, our first goal is to build a Barracks and a Supply Depot. The Barracks will allow us to train the Marine, Terran’s basic ground unit, while the Supply Depot will increase the number of units we can have at a given time. To make sure our Barracks is built near our Command Center, but not positioned where it will block our minerals, we find the enemy’s base and build the Barracks at a slight offset from our Command Center in that direction. We can do that since the minerals are placed on the opposite side of your Command Center from the enemy’s starting location. 

From there we use our spiral function to find a suitable location.  Spiral takes in a unit to do the building, an initial start location, and the type of building to build. From here, the closest available position to build is found by spiraling out from the initial position. In the case of the Barracks, the starting location is the slight offset from the Command Center in the direction of the enemy.

After the Barracks and Supply Depot are built, we want to build a Bunker at the nearest chokepoint to help defend against an enemy attack. For this we use our artichoke function. Artichoke iterates through all the chokepoints on a map and determines which is nearest our own base. The center point of the chokepoint is then past to spiral, and a Bunker is built at this nearest possible location. While this is happening, our Barracks are building Marines. Once the Bunker is finished being constructed and four Marines are built (the Bunker’s capacity) those Marines move into the Bunker.

Now that a defense has been set-up at the chokepoint, we focus on building the Tier 2 units we’ll use to attack the enemy. We begin by building a Refinery to collect Vespene Gas. Once we have collected enough Gas, we build a Factory using spiral to find a suitable location near our Command Center. Once the Factory is built, we focus on building the Vulture unit whenever we have the required resources. The Vulture is a fast and cheap unit, and is considered one of the most cost efficient units in the game. Once we have our Factory, all production shifts to creating Vultures. As soon as a Vulture is built, it is told to attack the enemies base. Our goal with this strategy is to overwhelm the enemy’s defenses; while no individual Vulture will destroy the base, the speed with which we can create Vultures allows us to win by attrition. Meanwhile, our Marines in their Bunker will help defend against an enemy attack.



**Build Order Overview**

if (SCV count < 6) Build more SCVs

if (SCV is idle) Collect Minerals

if (!built Supply Depot) Build Supply Depot

if (Supply Total - Supplies Used <= 3) Build another Supply Depot

if (built Supply Depot && !built Barracks) Build Barracks

if (built Barracks && Marine count < 4) Build Marines

if (built Depot && we have information on the nearest Chokepoint) Send one SCV to the Chokepoint to clear the fog of war

if (built Depot && !built Bunker) A Bunker is built at a location as near the center of the chokepoint as possible

if (built Bunker && Marine count is >= 4) Send Marines to our Bunker to defend the chokepoint

if (built Supply Depot, Barracks, and Bunker && !built Refinery) Build Refinery

if (built Supply Depot, Barracks, Bunker, and Refinery && !built Factory) Build Factory

if (built Factory) Build Vultures

As Vultures are built, they are sent to attack the enemy base.


**Note:** We are requesting the bonus points for this project for our use of Chokepoints as discussed in person. Thank you!
