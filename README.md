# What is "Pixel Dungeon 3D"
*Pixel Dungeon 3D* is mod that adds new 3D-renderer and new GUI to original *Pixel Dungeon* `v1.9.7`. 
This mod does not alter game logic, nor it adds any new gameplay elements - it's still good ol' *Pixel Dungeon* in a new 3D wrapper

This was my hobby project that I was coding on my free time from November'2016 to May'2018, but even after 19 months of development 
it does not look the way I was imagining it and I'm not going to continue development because I'm tired of it! I'll just 
leave it over here and switch to other projects.

# How it looks
Following 2 GIFs illustrate how my 3D mod looks like - on the left side you see original *Pixel Dungeon* `v1.9.7` and on the right side
is *Pixel Dungeon 3D* `v0.2.8` that is redering same game session:

$TODO

# Technical details
$TODO

# Prepare project
*Pixel Dungeon 3D* is split between 4 git repositories + some external dependent packages managed by *Gradle*. I was using *Eclipse* and 
*IntelliJ IDEA* as my IDE, but you can use anything else as long as it supports *Gradle*.

Following instructions describe how to import *Pixel Dungeon 3D* to *IntelliJ IDEA*
1. Create root project from `pixel-dungeon-3d.git`:
   
   Create new *IntelliJ IDEA* project by cloning `git@github.com:afomins/pixel-dungeon-3d.git` repository
   
   In *"Import project"* window select `"Gradle"` and then tap `"Create separate module per source set"` checkbox
   
   When *Gradle sync* is over then you should see `pixel-dungeon-3d` project with following modules - `core`, `desktop` and `android`
   
2. Import `pixel-dungeon-3d-lib.git`:

   Clone `git@github.com:afomins/pixel-dungeon-3d-lib.git` to `./core/pixel-dungeon-3d-lib` directory (navigate `VCS -> Checkout from Version Control -> Git`)
   
   Press `No` if *IntelliJ IDEA* asks you to create new project from `pixel-dungeon-3d-lib.git`
   
3. Import `pixel-dungeon-classes.git`:

   Clone `git@github.com:afomins/pixel-dungeon-classes.git` to `./core/pixel-dungeon-classes` directory
   
   Press `No` if *IntelliJ IDEA* asks you to create new project from `pixel-dungeon-classes.git`

4. Import `pixel-dungeon.git`:

   Clone `git@github.com:afomins/pixel-dungeon.git` to `./core/pixel-dungeon` directory
   
   Press `No` if *IntelliJ IDEA* asks you to create new project from `pixel-dungeon.git`
   

When you complete above mentioned steps then your project should have following structure:
```
   pixel-dungeon-3d
     |
     +-- core
     |     | 
     |     +-- pixel-dungeon-3d-lib
     |     |
     |     +-- pixel-dungeon-classes
     |     |
     |     +-- pixel-dungeon
     |
     +-- desktop
     |
     +-- android
```


**IMPORTANT** - you should manually copy original game assets (`*.png` and `*.mp3` files) from `./pixel-dungeon-3d/core/pixel-dungeon/assets` to `./pixel-dungeon/android/assets`.

# External packages
Following external packages are managed by *Gradle*:
1. `org.json:json:20180130`
2. `org.mini2Dx:universal-tween-engine:6.3.3`
3. `org.java-websocket:Java-WebSocket:1.3.8`
4. `com.badlogicgames.gdx:gdx:1.9.8`
5. `com.kotcrab.vis:vis-ui:1.4.0`
6. `com.google.code.gson:gson:2.8.2`

Gradle should automatically download those packages and update *IntelliJ IDEA* project files

# Build instructions for Desktop
Select `./android/assets` as your workign directory and run `com.matalok.pd3d.desktop.DesktopLauncher` class

**IMPORTANT** - I've been developing for Java8 that's why it might not compile/run with different Java version.

# Build instructions for Android
Building for Android is a little bit trickier than building for Desktop because all `android.*` and `javax.*` imports from native *Pixel Dungeon* project should be renamed to `stub.android.*` and `stub.javax.*` accordigly. This step is needed to ensure that native *Pixel Dungeon* sources call `stub` methods instead of `android` methods. To automate this task I created `./scripts/stub.sh` shell script that can add and delete `stubs` when necessary. The only problem with this script is that it uses `bash` syntax which can be run from either from native Linux or from Cygwin running on Windows.

1. Navigate to `./pixel-dungeon-3d/scripts` directory
2. Run `./stub.sh add` to replace *android* method calls to *stub* method calls 
3. Run `com.matalok.pd3d.AndroidLauncher` activity
