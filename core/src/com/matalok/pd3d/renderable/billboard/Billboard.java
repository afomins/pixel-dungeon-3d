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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererTexture;
import com.matalok.pd3d.renderer.Renderer.TxCache;
import com.matalok.pd3d.renderer.RendererBillboard;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class Billboard
  extends RenderableObject {
    //**************************************************************************
    // Billboard
    //**************************************************************************
    private RendererBillboard m_billboard;
    private Object m_billboard_key;

    //--------------------------------------------------------------------------
    public Billboard(String name, RenderableObjectType type, int model_id, 
      BillboardTmpl template, Renderer.Layer renderer_type) {
        super(name, type, model_id, template, renderer_type);
        Renderer r = Main.inst.renderer;
        TxCache tx_cache = r.GetTxCache();

        // Save billboard key
        m_billboard_key = template.billboard_key;

        // Create decal
        float scale = Main.inst.cfg.billboard_scale;
        RendererTexture texture = tx_cache.Get(template.tx_name);
        Utils.Assert(texture != null, "Failed to get decal texture :: tx-name=%s", template.tx_name);
        m_billboard = new RendererBillboard(
          texture.tx.getWidth() * Math.abs(template.u_size) * scale, 
          texture.tx.getHeight() * Math.abs(template.v_size) * scale, 
          new TextureRegion(texture.tx, template.u, template.v,
            template.u + template.u_size, template.v + template.v_size));
        m_billboard.inst.setColor(template.tx_color);

        // Get bounds
        float size = 0.5f;
        m_bounds = new Bounds(new BoundingBox(
          new Vector3(-size, -size, -size), 
          new Vector3(size, size, size)));
    }

    //--------------------------------------------------------------------------
    public RendererBillboard GetBillboard() {
        return m_billboard;
    }

    //--------------------------------------------------------------------------
    public Object GetBillboardKey() {
        return m_billboard_key;
    }

    //**************************************************************************
    // GameNode
    //**************************************************************************
    @Override public long OnPreDelete(boolean delete_instantly) {
        super.OnPreDelete(delete_instantly);
        return 0;
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    private static Vector3 tmp_v3 = new Vector3();
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Update transform
        Matrix4 runtime_transform = ((BillboardTmpl)m_template).runtime_transform;
        if(IsGlobalTransformDirty() || runtime_transform != null) {
            // Position
            m_billboard.inst.setPosition(GetGlobalPos());
            m_bounds.UpdateCenterGlobal(GetGlobalPos());

            // Scaling
            GetGlobalTransform().getScale(tmp_v3);
            m_billboard.inst.setScale(tmp_v3.x, tmp_v3.y);

            // Apply runtime transformation
            if(runtime_transform != null) {
                runtime_transform.getScale(tmp_v3);
                m_billboard.inst.setScale(tmp_v3.x, tmp_v3.y);
            }
        }

        // Update alpha
        if(IsGlobalAlphaDirty()) {
            Color c = m_billboard.inst.getColor();
            m_billboard.inst.setColor(c.r, c.g, c.b, GetGlobalAlpha());
        }

        // Add to renderer
        if(IsRenderAllowed()) {
            Camera camera = ctx.GetCamera(m_renderer_layer);
            Vector3 camera_dir = camera.GetGdxCamera().direction;

            m_billboard.inst.setRotation(
              new Vector3(-camera_dir.x, -camera_dir.y, -camera_dir.z), Vector3.Y);

            ctx.AddLayerObject(m_billboard, Renderer.Layer.BILLBOARD,
              IsVisibleByCamera(camera), 0);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_billboard.OnCleanup();
        m_billboard = null;
        return true;
    }
}
