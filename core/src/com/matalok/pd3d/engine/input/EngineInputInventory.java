//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Input.Keys;
import com.matalok.pd3d.engine.SceneGame;

//------------------------------------------------------------------------------
public class EngineInputInventory
  extends EngineInput {
    //**************************************************************************
    // EngineInputInventory
    //**************************************************************************
    public EngineInputInventory(SceneGame game) {
        super(game, true, false, false);
    }

    //**************************************************************************
    // InputProcessor
    //**************************************************************************
    @Override public boolean keyDown(int keycode) {
        switch(keycode) {
        //......................................................................
        case Keys.NUM_1:
        case Keys.NUM_2:
        case Keys.NUM_3:
        case Keys.NUM_4:
        case Keys.NUM_5:
        case Keys.NUM_6:
        case Keys.NUM_7:
        case Keys.NUM_8:
        case Keys.NUM_9: {
            int idx = keycode - Keys.NUM_1;
        } break;

        //......................................................................
        default:
            return false;
        }
        return true;
    }
}
 