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
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class MTmplSpriteGrassHigh 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteGrassHigh
    //**************************************************************************
    private UtilsClass.FFloat m_runtime_rotating;
    private Tweener m_tweener;

    //--------------------------------------------------------------------------
    public MTmplSpriteGrassHigh(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 1.0f, true, true, null);
        m_runtime_rotating = new UtilsClass.FFloat(0.0f);
        m_tweener = new Tweener();
        m_tweener.Start(m_runtime_rotating, null, 0, 
          Main.inst.cfg.grass_rotate_angle,
          new Integer(-1), null, // Infinite yo-yo scaling 
          Main.inst.cfg.grass_rotate_duration);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        Matrix4 transform = new Matrix4().rotate(Vector3.Z, 30.0f);
        for(int i = 0; i < 2; i++) {
          transform.rotate(Vector3.Z, 85.0f);
          MTmplSprite.CreatePlane(builder, attributes, material, 
            width, height, rect, 1, 0.1f, true, transform);
        }
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.5f, 0.0f)
          .rotate(Vector3.X, 90.0f)
          .rotate(Vector3.Z, 90.0f);
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
        m_tweener.Update();
        runtime_transform.setToRotation(Vector3.Z, m_runtime_rotating.v);
        runtime_transform.translate(m_runtime_rotating.v / 200.0f, 0.0f, 0.0f);
    }
}
