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
import com.matalok.pd3d.map.MapEnum;

//------------------------------------------------------------------------------
public class EngineWndInterLevel 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndInterLevel
    //**************************************************************************
    private String m_description;

    //--------------------------------------------------------------------------
    public EngineWndInterLevel() {
        super(null, false, false, 1.0f, 1.0f, 
          InputAction.CONSUME,  // OnTouchInArea
          InputAction.CONSUME,  // OnTouchOutArea
          InputAction.CONSUME,  // OnKeyPress
          InputAction.CONSUME,  // OnBack
          1.0f, MapEnum.TerrainType.EMPTY, 1.0f);
        setColor(0.5f, 0.5f, 0.5f, 1.0f);
        m_description = "...";
    }

    //--------------------------------------------------------------------------
    public void SetDescription(String text) {
        m_description = text;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        AddCellLabel(null, m_description, null, null);
        return this;
    }
}
