//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescSceneInterlevel;
import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.msg.MsgUpdateScene;
import com.matalok.pd3d.engine.gui.EngineWndInterLevel;

//------------------------------------------------------------------------------
public class SceneInterLevel 
  extends Scene {
    //**************************************************************************
    // StateInit
    //**************************************************************************
    public class StateInit 
      extends Scene.StateInit {
        //----------------------------------------------------------------------
        public StateInit() {
            super(Main.inst.engine.wnd_inter_level);
            ((EngineWndInterLevel)wnd).SetDescription("...");
        }

        //----------------------------------------------------------------------
        public void SetDescription(String text) {
            ((EngineWndInterLevel)wnd).SetDescription(text);
            ((EngineWndInterLevel)wnd).Rebuild();
        }
    }

    //**************************************************************************
    // SceneInterLevel
    //**************************************************************************
    public SceneInterLevel() {
        super("scene-inter-level");
    }

    //**************************************************************************
    // Scene
    //**************************************************************************
    @Override protected void OnServerResponse(Msg msg) {
        Class<? extends Msg> msg_class = msg.getClass();

        //......................................................................
        // UPDATE-SCENE
        if(msg_class == MsgUpdateScene.class) {
            DescSceneInterlevel desc = ((MsgUpdateScene)msg).interlevel_scene;

            // Set description
            ((StateInit)GetStateInit()).SetDescription(desc.description);

            // Go back to init state and show description
            PopState();
        }

        // Run default response handler
        super.OnServerResponse(msg);
    }

    //--------------------------------------------------------------------------
    @Override public void OnActivateScene() {
        super.OnActivateScene();

        // Show inter-level window
        PushState(new StateInit());

        // Stay in dummy state while waiting for update from server
        PushState(new StateDummy());
    }
}
