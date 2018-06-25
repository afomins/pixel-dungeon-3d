# What is "Pixel Dungeon 3D"
*Pixel Dungeon 3D* is mod that adds new 3D-renderer and new GUI to original *Pixel Dungeon* `v1.9.7`. This mod does not alter game logic, nor it adds any new gameplay elements - it's still good ol' *Pixel Dungeon* with new 3D visuals and different controls.

This was my hobby project that I was coding on my free time for 18 months. It is still half-finished and needs a lot of polishing, but I'm not going to continue development right now because I'm really tired of it! 

I'll just leave it over here and switch to other projects.

# How it looks
Following GIFs illustrate how 3D mod looks like comparing to original - **Desktop** version of *Pixel Dungeon 3D* `v0.2.8` (*client*) attaches to original *Pixel Dungeon* `v1.9.7` running in **Android emulator** (server) and renders **same** game session:

| Rotating in first room of first level | Exploring first level |
| --|--|
| <img src="https://github.com/afomins/pixel-dungeon-3d/blob/dev-pd3d/assets-raw/screenshots/pd_vs_pd3d_000.gif" width="400"> | <img src="https://github.com/afomins/pixel-dungeon-3d/blob/dev-pd3d/assets-raw/screenshots/pd_vs_pd3d_001.gif" width="400"> |

| Fighting rats on first level | Fighting blob |
| --|--|
| <img src="https://github.com/afomins/pixel-dungeon-3d/blob/dev-pd3d/assets-raw/screenshots/pd_vs_pd3d_002.gif" width="400"> | <img src="https://github.com/afomins/pixel-dungeon-3d/blob/dev-pd3d/assets-raw/screenshots/pd_vs_pd3d_003.gif" width="400"> |

# External links
https://twitter.com/matalokgames - twitter account where I used to share progress while developing [#pixeldungeon3d](https://twitter.com/search?f=tweets&vertical=default&q=%23pixeldungeon3d&src=typd)

https://www.youtube.com/channel/UCovOZS4bKNiW_aX1yoktwOA - youtube channel where I used to publish gameplay videos

https://redd.it/72642r - reddit post #1 where I first announced this 3D mod

https://redd.it/7apfog - reddit post #2 where I published `v0.1.0` alpha version

https://redd.it/8dvpcr - reddit post #3 where I published `v0.2.8` beta version

$TODO

# Technical details
$TODO

# How to checkout sources and prepare project files
*Pixel Dungeon 3D* is split between 4 git repositories + some external dependent packages managed by *Gradle*. I was using *Eclipse* and 
*IntelliJ IDEA* (aka *Android Studio*) as my IDE, but you can use anything else as long as it supports *Gradle*.

Following instructions describe how to import *Pixel Dungeon 3D* to *IntelliJ IDEA*
1. Create root project from `pixel-dungeon-3d.git`:
   
   Create new *IntelliJ IDEA* project by cloning `git@github.com:afomins/pixel-dungeon-3d.git` repository into `pd3d` directory.
   
   In *"Import project"* window select `"Gradle"` and then tap `"Create separate module per source set"` checkbox.
   
   When *Gradle sync* is over then you should see `pd3d` project with `core`, `desktop` and `android` modules.
   
2. Import `pixel-dungeon-3d-lib.git`:

   Clone `git@github.com:afomins/pixel-dungeon-3d-lib.git` to `./pd3d/core/pixel-dungeon-3d-lib` directory (you can do this by navigating to `VCS -> Checkout from Version Control -> Git` menu)
   
   (Press `No` if *IntelliJ IDEA* asks you to create new project)
   
3. Import `pixel-dungeon-classes.git`:

   Clone `git@github.com:afomins/pixel-dungeon-classes.git` to `./pd3d/core/pixel-dungeon-classes` directory.
   
   (Press `No` if *IntelliJ IDEA* asks you to create new project)

4. Import `pixel-dungeon.git`:

   Clone `git@github.com:afomins/pixel-dungeon.git` to `./pd3d/core/pixel-dungeon` directory.
   
   (Press `No` if *IntelliJ IDEA* asks you to create new project)
   

When you complete above mentioned steps then your project should have following structure:
```
   pd3d
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


**IMPORTANT** - you should manually copy original game assets (`*.png` and `*.mp3` files) from `./pd3d/core/pixel-dungeon/assets` to `./pd3d/android/assets`. (**TODO:** Automate this step with *Gradle*)

# External packages
Following external packages are managed by *Gradle*:
1. `org.json:json:20180130`
2. `org.mini2Dx:universal-tween-engine:6.3.3`
3. `org.java-websocket:Java-WebSocket:1.3.8`
4. `com.badlogicgames.gdx:gdx:1.9.8`
5. `com.kotcrab.vis:vis-ui:1.4.0`
6. `com.google.code.gson:gson:2.8.2`

You should not care about external packages because *Gradle* will automatically download them and update *IntelliJ IDEA* project files.

# Build instructions for Desktop
Select `./pd3d/android/assets` as your working directory and run `com.matalok.pd3d.desktop.DesktopLauncher` class.

**IMPORTANT** - I've been developing for Java8 that's why it might not compile/run with different Java version.

# Build instructions for Android
Building for Android is a little bit trickier than building for Desktop because all `android.*` and `javax.*` imports from native *Pixel Dungeon* project should be renamed to `stub.android.*` and `stub.javax.*` accordigly.

For instance:
```
import javax.microedition.khronos.opengles.GL10;  -> import stub.javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;                     -> import stub.android.opengl.GLES20;
```

This step is *necessary evil* which ensures that native *Pixel Dungeon* sources call custom methods from `stub.android.*` packages instead of `android.*`. 

To automate this task I created `./pd3d/scripts/stubs.sh` shell script that can add/delete this **stub hack** when necessary. 

The only problem with this script is that it uses `/bin/bash` syntax which can be run either on native Linux or Cygwin.

1. Navigate to `./pd3d/scripts` directory
2. Run `./stubs.sh add` to replace `android.*` package import with `stub.android.*`
3. Run `com.matalok.pd3d.AndroidLauncher` activity
4. Run `./stubs.sh del` to revert `stub.adnroid.*` back to `android.*` 
