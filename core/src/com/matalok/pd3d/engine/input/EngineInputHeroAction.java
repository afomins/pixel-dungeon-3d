//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Input.Keys;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.engine.SceneGame.Step;
import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.msg.MsgGetInventory;
import com.matalok.pd3d.msg.MsgHeroInteract;

//------------------------------------------------------------------------------
public class EngineInputHeroAction
  extends EngineInput {
    //**************************************************************************
    // EngineInputHeroAction
    //**************************************************************************
    public EngineInputHeroAction(SceneGame game) {
        super(game, false, false, true);
    }

    //**************************************************************************
    // InputProcessor
    //**************************************************************************
    @Override public boolean keyDown(int keycode) {
        // Don't allow actions unless step is idle
        if(m_game.GetStep().state != Step.State.IDLE) {
            return true;
        }

        Msg msg = null;
        switch(keycode) {
        //......................................................................
        case Keys.SPACE: {
            msg = MsgHeroInteract.CreateRequest("search", null);
        } break;

        //......................................................................
        case Keys.BACKSPACE: {
            msg = MsgHeroInteract.CreateRequest("rest", null);
        } break;

        //......................................................................
        case Keys.I: {
            msg = MsgGetInventory.CreateRequest(null, "all", "generic", null);
        } break;
        }

        if(msg == null) {
            return false;
        } else {
            Main.inst.proxy_client.Send(msg);
            return true;
        }
    }
}
