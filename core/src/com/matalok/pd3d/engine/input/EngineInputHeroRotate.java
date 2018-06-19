//------------------------------------------------------------------------------
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Input.Keys;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.level.object.LevelObjectChar;

//------------------------------------------------------------------------------
public class EngineInputHeroRotate
  extends EngineInput {
    //**************************************************************************
    // EngineInputMoveHero
    //**************************************************************************
    public EngineInputHeroRotate(SceneGame game) {
        super(game, false, true, true);
    }

    //--------------------------------------------------------------------------
    public void RotateHero(int dir) {
        LevelObjectChar hero = Main.inst.level.GetHero();
        if(hero == null) {
            return;
        }
        hero.Rotate(dir);
    }

    //--------------------------------------------------------------------------
    private void RotateLeft() {
        RotateHero(+1);
    }

    //--------------------------------------------------------------------------
    private void RotateRight() {
        RotateHero(-1);
    }

    //--------------------------------------------------------------------------
    private void RotateBack() {
        RotateHero(+4);
    }

    //**************************************************************************
    // InputMan.IClient
    //**************************************************************************
    @Override public void Process() {
        Event touch_evt = GetTouchEvent();

        // Rotate
        if(touch_evt == Event.SWIPE_LEFT) {
            RotateLeft();
        } else if(touch_evt == Event.SWIPE_RIGHT) {
            RotateRight();
        } else if(touch_evt == Event.SWIPE_DOWN) {
            RotateBack();
        }
        super.Process();
    }

    //**************************************************************************
    // InputProcessor
    //**************************************************************************
    @Override public boolean keyDown(int keycode) {
        switch(keycode) {
        //......................................................................
        case Keys.LEFT: {
            RotateLeft();
        } break;

        //......................................................................
        case Keys.RIGHT: {
            RotateRight();
        } break;

        //......................................................................
        case Keys.INSERT: {
            RotateBack();
        } break;

        //......................................................................
        default:
            return false;
        }
        return true;
    }
}
