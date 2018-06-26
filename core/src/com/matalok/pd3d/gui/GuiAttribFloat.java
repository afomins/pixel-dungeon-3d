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
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribFloat 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribFloat
    //**************************************************************************
    public GuiAttribFloat(RendererAttrib.AFloat.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.FLOAT, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        final RendererAttrib.AFloat.Cfg cfg = (RendererAttrib.AFloat.Cfg)m_cfg;

        add(new GuiSpinnerFloat(null, cfg.value, 0.0f, 1.0f, 0.1f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  cfg.value = value;
                  OnUpdate();
              };
        })).expandX().fillX();
    }
}
