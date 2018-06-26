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
public class GuiAttribDirLight 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribDirLight
    //**************************************************************************
    private Cell<GuiButtonChangeColor> m_color;

    //--------------------------------------------------------------------------
    public GuiAttribDirLight(RendererAttrib.ADirLight.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.DIR_LIGHT, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        add(new GuiButtonChangeVector3(
          ((RendererAttrib.ADirLight.Cfg)m_cfg).dir, this));
        m_color = add(new GuiButtonChangeColor(
          ((RendererAttrib.ADirLight.Cfg)m_cfg).color, this));
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public void layout() {
        float h = getHeight();
        if(h > 0.0f) {
            m_color.size(h);
        }
        super.layout();
    }
}
