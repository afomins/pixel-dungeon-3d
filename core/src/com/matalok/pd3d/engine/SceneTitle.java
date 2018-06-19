//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Scheduler.Event;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class SceneTitle 
  extends Scene {
    //**************************************************************************
    // StateInit
    //**************************************************************************
    public class StateInit
      extends Scene.StateInit {
        //----------------------------------------------------------------------
        public StateInit() {
            super(Main.inst.engine.wnd_title);

            // Default destructor quits game
            SetDestructor(new UtilsClass.Callback() {
                @Override public Object Run(Object... args) {
                    Main.inst.scheduler.ScheduleEvent(Event.QUIT);
                    return null;
                }
            });
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static boolean is_first_activation = true;

    //**************************************************************************
    // SceneTitle
    //**************************************************************************
    public SceneTitle() {
        super("scene-title");
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    @Override public void OnActivateScene() {
        super.OnActivateScene();

        // Start snapshot auto-switching during first activation
        if(is_first_activation) {
            Main.inst.snapshot.StartAutoswitch();
            is_first_activation = false;
        }

        // Start rotation around hero
        Main.inst.level_camera.StartCircleRotation(false);

        // Show title window
        PushState(new StateInit());
    }
}
