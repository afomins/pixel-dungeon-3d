//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.level.object.LevelObjectCell;
import com.matalok.pd3d.level.object.LevelObjectChar;
import com.matalok.pd3d.msg.MsgRunItemAction;

//------------------------------------------------------------------------------
public class EngineInputItemThrow
  extends EngineInput {
    //**************************************************************************
    // EngineInputItemThrow
    //**************************************************************************
    private int m_item_idx;
    private String m_item_action;

    //--------------------------------------------------------------------------
    public EngineInputItemThrow(SceneGame game, String item_action, int item_idx) {
        super(game, false, false, true);
        m_item_action = item_action;
        m_item_idx = item_idx;
    }

    //--------------------------------------------------------------------------
    public void Throw(LevelObjectCell target) {
        // Rotate hero towards the selected cell
        LevelObjectChar hero = Main.inst.level.GetHero();
        if(hero == null) {
            return;
        }
        hero.Rotate(target);

        // Throw item to cell
        Main.inst.proxy_client.Send(
          MsgRunItemAction.CreateRequest(
            m_item_idx, m_item_action, target.GetPdId()));

        // Return to previous state
        m_game.PopState();
    }

    //**************************************************************************
    // GestureListener
    //**************************************************************************
    @Override public boolean tap(float x, float y, int count, int button) {
        if(count > 1) {
            return false;
        }

        // Get target cell
        LevelObjectCell target_cell = 
          Main.inst.level.GetSelectedCell((int)x, (int)y, true, false);
        if(target_cell == null) {
            return true;
        }

        // Throw at target cell
        Throw(target_cell);
        return true;
    }
}
