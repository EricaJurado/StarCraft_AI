# StarCraft_AI






**Strategy Overview (Chokepoint yes)**

Additional SCVs are built when resources are available for a total of 10 (begin with 4)

The following buildings are built as their preconditions (including resource amounts) are met:
    
   -Supply Depots are built when the unit cap is reached.
   
        -Supply Depots raise the total number of units we can have.
   
   -A Barrack is built as soon as possible after the 10th SCV.
   
        -Barrack train Marines and Medics.
     
   -Once the barrack is built, an academy is built 
   
        -Academy allows us to build Medics.
   
   -Once the barrack is built, 2 Bunkers are built flanking the checkpoint 
   
        -Two bunkers flank the chokepoint to make a funnel
        -Marines and Medics will go in these bunkers to fire into the chokepoint
   
   -Once the academy is built,  factory is built
   
        -Factories allow us to build Vultures and Siege Tanks
   
   -Once a factory is built, a machine shop is built 
   
        -Machine Shops allow factories to build Siege Tanks
   
   -Once a machine shop is built, another factory and machine shop are built
   
   
   
The following units are built as preconditions (including resource amounts) are met:
   
   -Marines are built once there is a barracks. Three marines are built at a time.
   
   -Once three marines are built, one medic is built.
   
        -These four units are then placed in an array/list so they can be treated as a group [called M&M]
        
        -This build order of three marines and one medic then restarts
     
        -2 M&Ms enter empty bunkers; if there are no empty bunkers, they will cheese the enemy base
    
   -Once the Factory is built, build Vultures
   
        -Once two Vultures are built, they cheese the enemy 
   
   -Once the Machine Shop is built, build Siege Tanks
   
   -Once two Siege Tanks are built and the either
    
    
**Strategy Overview (Chokepoint no)**

Additional SCVs are built when resources are available for a total of 10 (begin with 4)

