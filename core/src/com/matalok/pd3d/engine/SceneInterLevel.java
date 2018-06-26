/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
