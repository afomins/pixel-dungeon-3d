# What is "Pixel Dungeon 3D"
*Pixel Dungeon 3D* is mod that adds new 3D-renderer and new GUI to original *Pixel Dungeon* `v1.9.7`. 
This mod does not alter game logic nor it adds new gameplay elements - it's still good-old *Pixel Dungeon* in a new 3D wrapper

This was my hobby project that I was coding on my free time from November'2016 to May'2018, but even after 19 months of development 
it does not look the way I was imagining it and I'm not going to continue development because I'm tired of it! I'll just 
leave it over here and switch to other projects.

# How it looks
Following 2 GIFs illustrate how 3D mod looks like - on the left side you see original *Pixel Dungeon* `v1.9.7` and on the right side is 
*Pixel Dungeon 3D* `v0.2.8` that is redering same game session:

$TODO

# Technical details
$TODO

# Build instructions for Desktop
*Pixel Dungeon 3D* is split between multiple git repositories + some external dependencies managed by *Gradle*. I was using *Eclipse* and 
*IntelliJ IDEA* as my IDE, but you can use anything else as long as it supports *Gradle*.

Following instructions describe step needed to import *Pixel Dungeon 3D* to *IntelliJ IDEA*
1. Create `pd3d` root project:
   
   Create new *IntelliJ IDEA* project by cloning `git@github.com:afomins/pixel-dungeon-3d.git` repository
   
   In *Import project* window select `"Gradle"` and tap `"Create separate module per source set"` checkbox
   
   When *Gradle sync* is over then you should see `pd3d` project with following modules - `core`, `desktop` and `android`
   
2. Import `pixel-dungeon-3d-lib` to `core`:

   Select `VCS -> Checkout from version control -> Git` and clone `git@github.com:afomins/pixel-dungeon-3d-lib.git` to `pd3d/core/pixel-dungeon-3d-lib` directory
   
   Press `No` when *IntelliJ IDEA* ask you to create new project from `pixel-dungeon-3d-lib.git`
   
   $TODO
