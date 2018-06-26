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
package com.matalok.pd3d.renderable.billboard;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class BillboardHpTmpl 
  extends BillboardTmpl {
    //**************************************************************************
    // BillboardHpTmpl
    //**************************************************************************
    public BillboardHpTmpl(Object key, String name, String tx_name, Color tx_color, float u, float v, 
      float u_size, float v_size) {
        super(key, name, tx_name, tx_color, u, v, u_size, v_size);
    }

    //**************************************************************************
    // ModelBase.ITemplate
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.setToTranslationAndScaling(
          0.0f, Main.inst.cfg.hp_vertical_offset, 0.0f,
          Main.inst.cfg.hp_scale_width, Main.inst.cfg.hp_scale_height, 1.0f);
    }
}
