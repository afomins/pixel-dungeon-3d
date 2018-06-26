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

// -----------------------------------------------------------------------------
package com.matalok.pd3d.level;

//-----------------------------------------------------------------------------
import java.util.HashMap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformUtils;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.Renderer.Layer;
import com.matalok.pd3d.renderer.RendererModel;

// -----------------------------------------------------------------------------
public class LevelSkybox 
  extends GameNode {
    // *************************************************************************
    // ENUMS
    // *************************************************************************
    public enum Side {
        //----------------------------------------------------------------------
        //  u/v    0.00    0.25     0.50    0.75
        // ------ ------- ------- -------- ------
        //  0.00   front   left    top      null
        //  0.50   back    right   bottom   null
        //
        FRONT(new Matrix4().rotate(Vector3.X, 90.0f), 0.0f, 0.0f),
        BACK(new Matrix4().rotate(Vector3.Y, 180.0f).mul(FRONT.transform), 0.0f, 0.5f),
        LEFT(new Matrix4().rotate(Vector3.Y, 90.0f).mul(FRONT.transform), 0.25f, 0.0f),
        RIGHT(new Matrix4().rotate(Vector3.Y, -90.0f).mul(FRONT.transform), 0.25f, 0.5f),
        TOP(new Matrix4().rotate(Vector3.X, 90.0f).mul(FRONT.transform), 0.5f, 0.0f),
        BOTTOM(new Matrix4(), 0.5f, 0.5f);

        //----------------------------------------------------------------------
        public Matrix4 transform;
        public float u, v, u_size, v_size;

        //----------------------------------------------------------------------
        private Side(Matrix4 transform, float u, float v) {
            this.transform = transform;
            this.u = u;
            this.v = v;
            this.u_size = 0.25f;
            this.v_size = 0.5f;
        }

        //----------------------------------------------------------------------
        public Matrix4 GetMatrix(float size) {
            return new Matrix4(transform).translate(0.0f, -size / 2, 0.0f);
        }
    }

    // *************************************************************************
    // LevelSkybox
    // *************************************************************************
    private HashMap<String, Object[]> m_models;
    private float m_rot_angle;

    //--------------------------------------------------------------------------
    public LevelSkybox() {
        super("skybox", 1.0f);
        m_models = new HashMap<String, Object[]>();
    }

    //--------------------------------------------------------------------------
    public void CreateSkybox(String name, Renderer.Layer renderer_type, 
      float size, Color color, boolean do_alpha_blending) {
        Texture texture = null;

        // Create dummy skybox texture
        if(name.equals("dummy")) {
            Pixmap pm = new Pixmap(4 * 2, 2 * 2, Pixmap.Format.RGBA8888);
            for(int y = 0; y < 2; y++) {
                for(int x = 0; x < 3; x++) {
                    int xx = x * 2, yy = y * 2;
                    pm.drawPixel(xx + 0, yy + 0, Color.rgba8888(Color.RED));
                    pm.drawPixel(xx + 1, yy + 0, Color.rgba8888(Color.GREEN));
                    pm.drawPixel(xx + 0, yy + 1, Color.rgba8888(Color.BLUE));
                    pm.drawPixel(xx + 1, yy + 1, Color.rgba8888(Color.YELLOW));
                }
            }
            texture = new Texture(pm);
            pm.dispose();

        // Generate fog texture
        } else if(name.equals("fog")) {
            Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
            pm.setColor(color);
            pm.fill();
            texture = new Texture(pm);
            pm.dispose();

        // Read skybox texture from file
        } else {
            texture = new Texture(PlatformUtils.OpenInternalFile(name, false));
        }

        // Create material
        Material material = RendererModel.CreateMaterial(
          color, texture, false, do_alpha_blending);

        // Create skybox
        GeomBuilder builder = Main.inst.geom_builder;
        int attributes = Usage.Position | Usage.TextureCoordinates;
        for(Side side : Side.values()) {
            builder.InitVertex4(size, size, 0.0f, 0.0f, 0.0f, side.GetMatrix(size));
            builder.CreatePlane(attributes, material, 
              side.u, side.v, side.u_size, side.v_size, false, null);
        }

        // Dispose old instance if present
        Object[] tuple = null;
        if(m_models.containsKey(name)) {
            tuple = m_models.get(name);
            ((Model)tuple[0]).dispose();
            ((RendererModel)tuple[1]).OnCleanup();

        // Create empty tuple if new
        } else {
            tuple = new Object[3];
            m_models.put(name, tuple);
        }

        // Build and cache model
        Model model = builder.FinalizeModel();
        tuple[0] = model;
        tuple[1] = new RendererModel(model);
        tuple[2] = renderer_type;
    }

    //--------------------------------------------------------------------------
    public void Rotate() {
        Quaternion q = new Quaternion(Vector3.Y, m_rot_angle);
        TweenRot(q, Main.inst.cfg.model_rotate_duration * 3, null);
        m_rot_angle += 1.0f;
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        Quaternion q = GetLocalRot();
        for(Object[] tuple : m_models.values()) {
            Renderer.Layer renderer_layer = (Layer)tuple[2];
            RendererModel renderer_model = (RendererModel)tuple[1];

            renderer_model.inst.transform.setToRotation(Vector3.X, 0.0f);
            renderer_model.inst.transform.rotate(q);

            ctx.AddLayerObject(renderer_model, renderer_layer, true, 2 * 6);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        for(Object[] tuple : m_models.values()) {
            ((Model)tuple[0]).dispose();
            ((RendererModel)tuple[1]).OnCleanup();
        }
        return true;
    }
}
