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
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class GuiClickListener 
  extends ClickListener {
    //--------------------------------------------------------------------------
    private long m_long_press_time;
    private boolean m_is_long_press_processed;

    //**************************************************************************
    // GuiClickListener
    //**************************************************************************
    public void OnPressed(boolean is_long) {
    }

    //--------------------------------------------------------------------------
    public void OnReleased(boolean is_long) {
    }

    //--------------------------------------------------------------------------
    public boolean IsLongPress() {
        return (m_long_press_time > 0 && 
          Main.inst.timer.GetCur() >= m_long_press_time);
    }

    //--------------------------------------------------------------------------
    public void Process() {
        // Detect long press 
        if(!m_is_long_press_processed && IsLongPress()) {
            OnPressed(true);
            m_is_long_press_processed = true;
        }
    }

    //**************************************************************************
    // ClickListener
    //**************************************************************************
    @Override public boolean touchDown(InputEvent event, float x, float y, 
      int pointer, int button) {
        boolean rc = super.touchDown(event, x, y, pointer, button);
        if(rc) {
            OnPressed(false);
            m_is_long_press_processed = false;
            m_long_press_time = Main.inst.timer.GetCur() + 
              Utils.SecToMsec(Main.inst.cfg.gui_long_click_duration);
        }
        return rc;
    }

    //--------------------------------------------------------------------------
    @Override public void clicked(InputEvent event, float x, float y) {
        OnReleased(IsLongPress());
        m_long_press_time = -1;
    }
}
