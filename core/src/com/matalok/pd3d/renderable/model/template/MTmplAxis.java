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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.renderable.model.ModelAnim;
import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class MTmplAxis 
  extends MTmpl {
    // *************************************************************************
    // TemplateAxis
    // *************************************************************************
    public MTmplAxis(String name) {
        super(name, null, false);

        // Define axis
        Object[] params = new Object[] {
            new Object[] { Vector3.X, 
              RendererModel.CreateMaterial(Color.RED, null, false, false)},
            new Object[] { Vector3.Y,
              RendererModel.CreateMaterial(Color.GREEN, null, false, false)},
            new Object[] { Vector3.Z,
              RendererModel.CreateMaterial(Color.BLUE, null, false, false)},
        };

        // Default animation
        ModelAnim.Template anim_t = SetAnim("idle", 1, false);

        // Model builder
        ModelBuilder model_builder = new ModelBuilder();
        model_builder.begin();

        // Create axis lines
        for(Object param : params) {
            Object[] __param = (Object[]) param; 
            Vector3 axis = (Vector3)__param[0];
            Material material = (Material)__param[1];

            MeshPartBuilder part_builder = model_builder.part(
              "line", GL20.GL_LINES, Usage.Position, material);
            part_builder.line(Vector3.Zero, axis);
        }

        // Create axis model
        anim_t.AddFrame(model_builder.end(), 0);
    }
}
