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
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class RendererModel
  implements IManaged {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static Material CreateMaterial(Color color, Texture tx, 
      boolean do_backface_culling, boolean do_alpha_blending) {
        // Color
        Material m = new Material();
        if(color != null) {
            m.set(ColorAttribute.createDiffuse(color));
        }

        // Texture
        if(tx != null) {
            m.set(TextureAttribute.createDiffuse(tx));
        }

        // Backface culling
        if(do_backface_culling) {
            m.set(new IntAttribute(
              IntAttribute.CullFace, GL20.GL_BACK));
        }

        // Alpha blending
        if(do_alpha_blending) {
            m.set(new FloatAttribute(
              FloatAttribute.AlphaTest, 0.1f));
            m.set(new BlendingAttribute(
              false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f));
        }
        
//        m.set(new ColorAttribute(ColorAttribute.Specular, 1.0f, 1.0f, 1.0f, 1.0f));
        return m;
    }

    //**************************************************************************
    // RendererModel
    //**************************************************************************
    public ModelInstance inst;
    public ColorAttribute mat_color_diff;
    public TextureAttribute mat_tx_diff;
    public FloatAttribute mat_alpha_test;
    public BlendingAttribute mat_blending;

    //--------------------------------------------------------------------------
    public RendererModel(Model model) {
        SwitchModel(model);
    }

    //--------------------------------------------------------------------------
    public void SwitchModel(Model model) {
        // Save old alpha if present
        Float old_opacity = null;
        Boolean old_blended = null;
        if(HasAlphaBlending()) {
            old_opacity = mat_blending.opacity;
            old_blended = mat_blending.blended;
        }

        // Create model instance
        inst = new ModelInstance(model);

        // Read material
        Material m = inst.materials.get(0);
        mat_color_diff = (ColorAttribute)m.get(ColorAttribute.Diffuse);
        mat_tx_diff = (TextureAttribute)m.get(TextureAttribute.Diffuse);
        mat_alpha_test = (FloatAttribute)m.get(FloatAttribute.AlphaTest);
        mat_blending = (BlendingAttribute)m.get(BlendingAttribute.Type);

        // Restore old alpha on new model if present
        if(old_opacity != null) {
            SetAlphaBlending(old_blended, old_opacity);
        }
    }

    //--------------------------------------------------------------------------
    public boolean HasAlphaBlending() {
        return (mat_blending != null);
    }

    //--------------------------------------------------------------------------
    public void SetAlphaBlending(float alpha) {
        if(mat_blending == null) {
            return;
        }
        mat_blending.opacity = alpha;
        mat_blending.blended = (alpha < 1.0f);
    }

    //--------------------------------------------------------------------------
    public void SetAlphaBlending(boolean do_alpha_blending, float alpha) {
        if(mat_blending == null) {
            return;
        }
        mat_blending.opacity = alpha;
        mat_blending.blended = do_alpha_blending;
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        mat_color_diff = null;
        mat_tx_diff = null;
        mat_alpha_test = null;
        mat_blending = null;
        inst = null;
    }
}
