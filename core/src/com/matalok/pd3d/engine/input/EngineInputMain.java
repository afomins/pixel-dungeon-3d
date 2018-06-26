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
package com.matalok.pd3d.engine.input;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Input.Keys;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Scheduler;
import com.matalok.pd3d.desc.DescItem;
import com.matalok.pd3d.engine.Scene;
import com.matalok.pd3d.engine.SceneGame;

//------------------------------------------------------------------------------
public class EngineInputMain
  extends EngineInput {
    //**************************************************************************
    // EngineInputMain
    //**************************************************************************
    public EngineInputMain(SceneGame game) {
        super(game, false, true, true);
    }

    //**************************************************************************
    // InputMan.IClient
    //**************************************************************************
    @Override public void Process() {
        Event touch_evt = GetTouchEvent();

        // Select next target when swiping up
        if(touch_evt == Event.SWIPE_UP) {
            Main.inst.level.SelectNextTarget();
            Main.inst.engine.wnd_game.UpdateTarget();
        }
        super.Process();
    }

    //**************************************************************************
    // InputProcessor
    //**************************************************************************
    @Override public boolean keyDown(int keycode) {
        switch(keycode) {
        //......................................................................
        case Keys.ESCAPE:
        case Keys.BACK: {
            // Show main menu if we are playing
            if(m_game.GetStateType() == SceneGame.StateAlive.class || 
               m_game.GetStateType() == SceneGame.StateDead.class) {
                m_game.PushState(new Scene.StateShowMainMenu());

            // Return to previous state if we are not playing
            } else {
                m_game.PopState();
            }
        } break;

        //......................................................................
        case Keys.ENTER: {
            Main.inst.level.SelectNextTarget();
            Main.inst.engine.wnd_game.UpdateTarget();
        } break;

        //......................................................................
        case Keys.NUM_1:
        case Keys.NUM_2: {
            // Throw item at selection
            if(m_game.GetStateType() == SceneGame.StateThrowItem.class) {
                m_game.ThrowAtSelection();

            // Run default action of item from quickslot
            } else {
                int slot_idx = (keycode == Keys.NUM_1) ? 0 : 1;
                DescItem item = m_game.GetQuickSlotItem(slot_idx);
                if(item != null) {
                    int quickslot_item_idx = -slot_idx - 1;
                    m_game.RunItemAction(item, quickslot_item_idx, slot_idx, null);
                }
            }
        } break;

        //......................................................................
        case Keys.F1: {
            Main.inst.engine.wnd_debug.Toggle();
        } break;

        //......................................................................
        case Keys.F5: {
            Main.inst.save_game.SaveStart(
              Main.inst.engine.GetCurHero(), "__quick-save");
        } break;

        //......................................................................
        case Keys.F9: {
            Main.inst.save_game.Load("__quick-save");
        } break;

        //......................................................................
        case Keys.F11: {
            Main.inst.engine.wnd_game.ToggleAlpha();
        } break;

        //......................................................................
        case Keys.F12: {
            Main.inst.scheduler.ScheduleEvent(
              Scheduler.Event.LOAD_NEXT_SNAPSHOT);
        } break;

        //......................................................................
        default:
            return false;
        }
        return true;
    }
}
 