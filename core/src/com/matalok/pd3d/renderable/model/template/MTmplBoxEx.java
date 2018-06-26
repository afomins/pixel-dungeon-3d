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
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import java.util.EnumSet;

import com.badlogic.gdx.graphics.Color;
import com.matalok.pd3d.GeomBuilder;

//------------------------------------------------------------------------------
public class MTmplBoxEx 
  extends MTmplBox {
    //**************************************************************************
    // TemplateBoxEx
    //**************************************************************************
    private float m_scale;

    //--------------------------------------------------------------------------
    public MTmplBoxEx(String name, boolean is_solid, 
      EnumSet<GeomBuilder.CubeSide> sides, Color color, float scale, Float alpha) {
        super(name, is_solid, sides, 1.0f, 1.0f, 1.0f, color, alpha);
        m_scale = scale;
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .idt()
          .scl(m_scale)
          .translate(0.0f, m_scale / 2, 0.0f);
    }
}
