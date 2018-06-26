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
import com.badlogic.gdx.math.Matrix4;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class BillboardEmotionTmpl 
  extends BillboardTmpl {
    //**************************************************************************
    // BillboardScalingTmpl
    //**************************************************************************
    private UtilsClass.FFloat m_runtime_scaling;
    private Tweener m_tweener;

    //--------------------------------------------------------------------------
    public BillboardEmotionTmpl(Object key, String name, String tx_name, Color tx_color, float u, float v, 
      float u_size, float v_size) {
        super(key, name, tx_name, tx_color, u, v, u_size, v_size);
        runtime_transform = new Matrix4();
        m_runtime_scaling = new UtilsClass.FFloat(1.0f);
        m_tweener = new Tweener();
        m_tweener.Start(m_runtime_scaling, null, 0, 
          Main.inst.cfg.emotion_scale_duration, 
          new Integer(-1), null, // Infinite yo-yo scaling 
          Main.inst.cfg.emotion_scale_size);
    }

    //**************************************************************************
    // ModelBase.ITemplate
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.setToTranslation(
          0.0f, Main.inst.cfg.emotion_vertical_offset, 0.0f);
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
        m_tweener.Update();
        runtime_transform.setToScaling(
          m_runtime_scaling.v, m_runtime_scaling.v, m_runtime_scaling.v);
    }
}
