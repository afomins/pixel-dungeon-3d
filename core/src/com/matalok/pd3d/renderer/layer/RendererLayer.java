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
package com.matalok.pd3d.renderer.layer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererEnv;
import com.matalok.pd3d.renderer.RendererProfiler;

//------------------------------------------------------------------------------
public abstract class RendererLayer 
  extends GameNode {
    //**************************************************************************
    // RendererLayer
    //**************************************************************************
    private Renderer.CameraType m_camera_type;
    private int m_front_face;
    private boolean m_do_clear_depth;
    private Float m_hack_draw_distance;

    public int stat_robj_visible_num;
    public int stat_robj_invisible_num;
    public int stat_robj_child_visible_num;
    public int stat_robj_child_invisible_num;
    public RendererProfiler.Stats stat_profiler;

    //--------------------------------------------------------------------------
    public RendererLayer(String name, int size, boolean do_clear_depth, 
      Renderer.CameraType camera_type, int front_face, Float hack_draw_distance) {
        super(name, 1.0f);
        m_do_clear_depth = do_clear_depth;
        m_camera_type = camera_type;
        stat_profiler = new RendererProfiler.Stats(name);
        m_front_face = front_face;
        m_hack_draw_distance = hack_draw_distance;
    }

    //--------------------------------------------------------------------------
    public Renderer.CameraType GetCameraType() {
        return m_camera_type;
    }

    //--------------------------------------------------------------------------
    public abstract void AddLayerObject(RenderCtx ctx, Object obj, boolean is_visible, int child_num);
    public abstract boolean UpdateLayer();
    public abstract void RenderLayer(RenderCtx ctx, Camera gdx_camera, RendererEnv env);
    public abstract void ClearLayer();

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Clear counters
        stat_robj_visible_num = stat_robj_invisible_num = 
          stat_robj_child_visible_num = stat_robj_child_invisible_num = 0;

        // Clear layer objects
        ClearLayer();
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        super.OnMethodPostRender(ctx, pre_children);

        // Ignore post-children
        if(!pre_children) {
            return true;
        }

        // Switch camera
        Camera gdx_camera = ctx.GetCamera(m_camera_type).GetGdxCamera();
        if(ctx.model_batch.getCamera() != gdx_camera) {
            ctx.model_batch.setCamera(gdx_camera);
        }

        // Exit if layer is empty
        if(!UpdateLayer()) {
            return true;
        }

        // Clear depth buffer
        if(m_do_clear_depth) {
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        }

        // Set front face
        Gdx.gl.glFrontFace(m_front_face);

        // This hack adjusts drawing distance when rendering skybox
        Float old_draw_distance = null;
        if(m_hack_draw_distance != null) {
            old_draw_distance = gdx_camera.far;
            gdx_camera.far = m_hack_draw_distance;
            gdx_camera.update();
        }

        // Get environment for current layer
        String env_profile_name = Main.inst.cfg.render_env_layers.get(SgGetName());
        RendererEnv env = Main.inst.renderer.GetEnvironment(env_profile_name); 

        // Render layer
        RenderLayer(ctx, gdx_camera, env);
        ctx.model_batch.flush();

        // Run profiler
        ctx.profiler.Update(stat_profiler);

        // Restore original drawing distance
        if(old_draw_distance != null) {
            gdx_camera.far = old_draw_distance;
            gdx_camera.update();
        }
        return true;
    }
}
