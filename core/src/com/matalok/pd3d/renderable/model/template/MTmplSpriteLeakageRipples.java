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
import com.badlogic.gdx.math.MathUtils;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class MTmplSpriteLeakageRipples 
  extends MTmplSpriteNoShadow {
    //**************************************************************************
    // MTmplSpriteRippleEx
    //**************************************************************************
    private UtilsClass.FFloat m_runtime_scaling;
    private Tweener m_tweener;
    private Tweener.Callback m_restart_cb;
    private float m_offset_x, m_offset_z;

    //--------------------------------------------------------------------------
    public MTmplSpriteLeakageRipples(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 0.2f, true, true, null);

        m_tweener = new Tweener();
        m_runtime_scaling = new UtilsClass.FFloat(0.0f);
        m_restart_cb = new Tweener.Callback(null) {
            @Override public void OnComplete() {
                m_offset_x = MathUtils.random(0.2f);
                m_offset_z = MathUtils.random(0.2f);

                m_runtime_scaling.v = 0.1f;
                m_tweener.Start(m_runtime_scaling, null, 0, 0.7f, 
                  null, m_restart_cb, MathUtils.random(0.7f, 1.0f));
            }
        };
        m_restart_cb.OnComplete();
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.08f, 0.8f);
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
        m_tweener.Update();
        runtime_transform.setToTranslationAndScaling(
          m_offset_x, 0.0f, m_offset_z, 
          m_runtime_scaling.v, 1.0f, m_runtime_scaling.v);
    }
}
