//------------------------------------------------------------------------------
package com.matalok.pd3d.engine;

//------------------------------------------------------------------------------
import java.util.LinkedList;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescHero;
import com.matalok.pd3d.desc.DescSceneStart;
import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.msg.MsgRunGame;
import com.matalok.pd3d.msg.MsgSwitchScene;
import com.matalok.pd3d.msg.MsgUpdateScene;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class SceneStart 
  extends Scene {
    //**************************************************************************
    // StateInit
    //**************************************************************************
    public class StateInit 
      extends Scene.StateInit {
        //----------------------------------------------------------------------
        public StateInit() {
            // No window by default
            super(null);

            // Go to title scene by default
            SetDestructor(new UtilsClass.Callback() {
                @Override public Object Run(Object... args) {
                    Main.inst.proxy_client.Send(
                      MsgSwitchScene.CreateRequest("scene-title"));
                    return null;
                }
            });
        }

        //----------------------------------------------------------------------
        public void SetHeros(LinkedList<DescHero> heroes) {
            SetWindow(Main.inst.engine.wnd_start
              .Init(heroes), true);
        }
    }

    //**************************************************************************
    // StateShowHeroInfo
    //**************************************************************************
    public static class StateShowHeroInfo
      extends State {
        //----------------------------------------------------------------------
        public StateShowHeroInfo(DescHero hero) {
            super(Main.inst.engine.wnd_hero_desc
              .Init(hero), true);
        }
    }

    //**************************************************************************
    // SceneStart
    //**************************************************************************
    public SceneStart() {
        super("scene-start");
    }

    //--------------------------------------------------------------------------
    @Override protected void OnServerResponse(Msg msg) {
        //......................................................................
        // UPDATE-SCENE
        Class<? extends Msg> msg_class = msg.getClass();
        if(msg_class == MsgUpdateScene.class) {
            DescSceneStart desc = ((MsgUpdateScene)msg).start_scene;

            // Set heroes and enable hero selection window
            ((StateInit)GetStateInit()).SetHeros(desc.heros);

            // Go back to init state and show hero selection window
            PopState();

        //......................................................................
        // RUN-GAME
        } else if(msg_class == MsgRunGame.class) {
            // Save current hero in case we want to restart game
            Main.inst.engine.SetCurHero(
              ((MsgRunGame)msg).hero_name);
        }

        // Run default response handler
        super.OnServerResponse(msg);
    }

    //--------------------------------------------------------------------------
    @Override public void OnActivateScene() {
        super.OnActivateScene();

        // Show hero selection window
        PushState(new StateInit());

        // Stay in dummy state while waiting for update from server
        PushState(new Scene.StateDummy());
    }
}
