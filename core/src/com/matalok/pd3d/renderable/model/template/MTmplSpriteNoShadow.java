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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteNoShadow 
  extends MTmplSprite {
    //**************************************************************************
    // MTmplSpriteNoShadow
    //**************************************************************************
    public MTmplSpriteNoShadow(String tx_cache_key, DescSprite sprite, 
      Float alpha, boolean has_runtime_transform, boolean is_unit_size, Color color) {
        super(tx_cache_key, sprite, alpha, has_runtime_transform, is_unit_size, color);
    }

    //--------------------------------------------------------------------------
    protected void BuildMesh(GeomBuilder builder, long attributes, Material material, 
      float width, float height, DescRect rect) {
        int shadow_num = 0; // !!!
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, shadow_num, 0.1f, false, null);
    }
}
