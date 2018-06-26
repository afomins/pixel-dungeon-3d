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
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteWall 
  extends MTmplSprite {
    //**************************************************************************
    // ModelSpriteWall
    //**************************************************************************
    public MTmplSpriteWall(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 1.0f, false, true, null);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        // Define cube sides
        Matrix4 front = new Matrix4().rotate(Vector3.X, 90.0f);
        Matrix4[] transform = new Matrix4[] {
            front,                                        // Front
            new Matrix4(front).rotate(Vector3.Z, 90.0f),  // Right
            new Matrix4(front).rotate(Vector3.Z, 180.0f), // Back
            new Matrix4(front).rotate(Vector3.Z, 270.0f), // Left
            new Matrix4(),                                // Top
        };

        // Create cube
        for(Matrix4 t : transform) {
            t.translate(0.0f, 0.5f, 0.0f);
            MTmplSprite.CreatePlane(builder, attributes, material, 
              width, height, rect, 0, 0.0f, false, t);
        }
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.5f, 0.0f);
    }
}
