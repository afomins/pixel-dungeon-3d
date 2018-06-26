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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribColor 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribColor
    //**************************************************************************
    private Cell<GuiButtonChangeColor> m_color_picker;

    //--------------------------------------------------------------------------
    public GuiAttribColor(RendererAttrib.AColor.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.COLOR, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        m_color_picker = add(new GuiButtonChangeColor(
          ((RendererAttrib.AColor.Cfg)m_cfg).color, this));
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public void layout() {
        float h = getHeight();
        if(h > 0.0f) {
            m_color_picker.size(h);
        }
        super.layout();
    }
}
