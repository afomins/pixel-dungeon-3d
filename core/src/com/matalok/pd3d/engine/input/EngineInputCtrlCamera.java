//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.SceneGame;
import com.badlogic.gdx.Input.Keys;

//------------------------------------------------------------------------------
public class EngineInputCtrlCamera
  extends EngineInput {
    //**************************************************************************
    // EngineInputCtrlCamera
    //**************************************************************************
    public EngineInputCtrlCamera(SceneGame game) {
        super(game, false, false, false);
    }

    //**************************************************************************
    // InputProcessor
    //**************************************************************************
    @Override public boolean keyDown(int keycode) {
        switch(keycode) {
        //......................................................................
        case Keys.PLUS: {
            Main.inst.level_camera.SwitchCamera(+1);
        } break;

        //......................................................................
        case Keys.MINUS: {
            Main.inst.level_camera.SwitchCamera(-1);
        } break;

        //......................................................................
        default:
            return false;
        }
        return true;
    }
}
