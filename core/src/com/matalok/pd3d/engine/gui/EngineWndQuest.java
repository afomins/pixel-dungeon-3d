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
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import java.util.Map;

//------------------------------------------------------------------------------
public class EngineWndQuest 
  extends EngineWndInfo {
    //**************************************************************************
    // EngineWndQuest
    //**************************************************************************
    protected String m_title;
    protected String m_text;
    protected Enum<?> m_sprite; 
    protected Map<String, ClickListener> m_buttons;
    protected int m_btn_row_size;

    //--------------------------------------------------------------------------
    public EngineWndQuest() {
        super(false);
    }

    //--------------------------------------------------------------------------
    public EngineWndQuest Init(String title, String text, Enum<?> sprite, 
      Map<String, ClickListener> buttons, int btn_row_size) {
        m_title = title;
        m_text = text;
        m_sprite = sprite;
        m_buttons = buttons;
        m_btn_row_size = btn_row_size;
        return this;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        super.OnPostReset();

        Set(m_title, m_sprite, m_text, m_buttons, m_btn_row_size);
        return this;
    }
}
 