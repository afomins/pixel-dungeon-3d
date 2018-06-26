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
