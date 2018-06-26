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
package com.matalok.pd3d.renderer;

//-----------------------------------------------------------------------------
import java.util.HashMap;
import java.util.Map.Entry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.decals.GroupStrategy;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformUtils;
import com.matalok.pd3d.Timer;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.RendererAttrib.ADirLight.Cfg;
import com.matalok.pd3d.renderer.layer.RendererLayer;
import com.matalok.pd3d.renderer.layer.RendererLayerBillboard;
import com.matalok.pd3d.renderer.layer.RendererLayerModel;
import com.matalok.pd3d.renderer.layer.RendererLayerModelWater;
import com.matalok.pd3d.renderer.layer.RendererLayerParticle;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.UtilsClass;

// -----------------------------------------------------------------------------
public class Renderer 
  extends GameNode {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static final float skybox_distance_hack = 1000.0f;

    // Renderer layers
    // https://stackoverflow.com/questions/31605140/transparency-issues-with-3d-particles-and-3d-models-libgdx
    public enum Layer {
        //......................................................................
        OPAQUE_TERRAIN,         // Opaque ordered from FRONT to BACK
        OPAQUE_WATER,
        OPAQUE_SKY,
        TRANSPARENT_TERRAIN,    // Transparent ordered from BACK to FRONT
        TRANSPARENT_ITEM,
        PARTICLE,               // Fire, smoke..
        TRANSPARENT_FOG,
        BILLBOARD,
        OVERLAY_DBG,            // Overlay
    };

    // Camera
    public enum CameraType {
        //......................................................................
        LEVEL,
        GUI,
    }

    // *************************************************************************
    // TxCache
    // *************************************************************************
    public class TxCache 
      extends UtilsClass.Cache<String, RendererTexture> {
        //----------------------------------------------------------------------
        public TxCache() { 
            super("texture", true); 
        }

        //----------------------------------------------------------------------
        @Override protected void Dispose(RendererTexture obj) {
            obj.tx.dispose();
        }
    };

    // *************************************************************************
    // Renderer
    // *************************************************************************
    private RendererLayer[] m_layers;
    private Camera[] m_cameras;
    private TxCache m_tx_cache;
    private ModelBatch m_model_batch;
    private DecalBatch m_decal_batch;
    private GroupStrategy m_decal_group_strategy;
    private RendererProfiler m_profiler;
    private int m_model_num, m_model_visible_num;
    private int m_triangle_num, m_triangle_visible_num;
    private int m_particle_num, m_particle_visible_num;
    private int m_billboard_num, m_billboard_visible_num;
    private long m_frame_prev, m_frame_cur;
    private HashMap<String, RendererEnv> m_env_profiles;

    // -------------------------------------------------------------------------
    public Renderer() {
        super("renderer", 1.0f);

        // Enable profiler
        m_profiler = new RendererProfiler();

        // Environment profiles
        m_env_profiles = new HashMap<String, RendererEnv>();
        for(Entry<String, RendererAttribStack.Cfg> e : 
          Main.inst.cfg.renderer_attrib_stacks.entrySet()) {
            m_env_profiles.put(e.getKey(), new RendererEnv(e.getValue()));
        }

        // Renderer layers
        m_layers = new RendererLayer[] {
          (RendererLayer)SgAddChild(
            new RendererLayerModel("render-opaque-terrain", 512, false, CameraType.LEVEL, GL20.GL_CCW, null)),
          (RendererLayer)SgAddChild(
            new RendererLayerModelWater("render-opaque-water", 64, false, CameraType.LEVEL, GL20.GL_CCW)),
          (RendererLayer)SgAddChild(
            new RendererLayerModel("render-opaque-sky", 8, false, CameraType.LEVEL, GL20.GL_CCW, skybox_distance_hack)),
          (RendererLayer)SgAddChild(
            new RendererLayerModel("render-transparent-terrain", 256, false, CameraType.LEVEL, GL20.GL_CCW, null)),
          (RendererLayer)SgAddChild(
            new RendererLayerModel("render-transparent-item", 256, false, CameraType.LEVEL, GL20.GL_CCW, null)),
          (RendererLayer)SgAddChild(
            new RendererLayerParticle("render-particles", 128, false, CameraType.LEVEL, GL20.GL_CCW, null)),
          (RendererLayer)SgAddChild(
            new RendererLayerModel("render-transparent-fog", 128, false, CameraType.LEVEL, GL20.GL_CCW, skybox_distance_hack)),
          (RendererLayer)SgAddChild(
            new RendererLayerBillboard("render-billboard", 32, false, CameraType.LEVEL, GL20.GL_CCW, null)),
          (RendererLayer)SgAddChild(
            new RendererLayerModel("render-overlay-dbg", 64, true, CameraType.LEVEL, GL20.GL_CCW, null)),
        };

        // Render cameras
        m_cameras = new Camera[CameraType.values().length];

        // Model batch for faster rendering
        m_model_batch = new ModelBatch();

        // Decal batch
        m_decal_batch = new DecalBatch(null);

        // Texture cache
        m_tx_cache = new TxCache();
    }

    // -------------------------------------------------------------------------
    public RendererEnv GetEnvironment(String name) {
        return m_env_profiles.get(name);
    }

    // -------------------------------------------------------------------------
    public int GetWidth() {
        return Gdx.graphics.getWidth();
    }

    // -------------------------------------------------------------------------
    public int GetHeight() {
        return Gdx.graphics.getHeight();
    }

    // -------------------------------------------------------------------------
    public float GetRelWidth(float val) {
        return GetWidth() * val;
    }

    // -------------------------------------------------------------------------
    public float GetRelHeight(float val) {
        return GetHeight() * val;
    }

    // -------------------------------------------------------------------------
    public float GetRelSmallest(float val) {
        return IsLandscape() ? GetRelHeight(val) : GetRelWidth(val);
    }

    // -------------------------------------------------------------------------
    public float GetRelBiggest(float val) {
        return !IsLandscape() ? GetRelHeight(val) : GetRelWidth(val);
    }

    // -------------------------------------------------------------------------
    public boolean IsLandscape() {
        return (GetWidth() > GetHeight());
    }

    // -------------------------------------------------------------------------
    public int GetFps() {
        return Gdx.graphics.getFramesPerSecond();
    }

    // -------------------------------------------------------------------------
    public int GetModelNum(boolean visible) {
        return (visible) ? m_model_visible_num : m_model_num;
    }

    // -------------------------------------------------------------------------
    public int GetTriangleNum(boolean visible) {
        return (visible) ? m_triangle_visible_num : m_triangle_num;
    }

    // -------------------------------------------------------------------------
    public int GetParticleNum(boolean visible) {
        return (visible) ? m_particle_visible_num : m_particle_num;
    }

    // -------------------------------------------------------------------------
    public int GetBillboardNum(boolean visible) {
        return (visible) ? m_billboard_visible_num : m_billboard_num;
    }

    // -------------------------------------------------------------------------
    public void RotateScreen() {
        int w = GetWidth(), h = GetHeight();
        boolean rc = Gdx.graphics.setWindowedMode(h, w);
        Logger.d("Rotating screen :: width=%d height=%d rc=%s", 
          h, w, Boolean.toString(rc));

        if(h > w) {
            PlatformUtils.api.SetScreenLandscape();
        } else {
            PlatformUtils.api.SetScreenPortrait();
        }
    }

    // -------------------------------------------------------------------------
    public void SetPortraitScreen() {
        if(!IsLandscape()) {
            return;
        }
        RotateScreen();
    }

    // -------------------------------------------------------------------------
    public void SetLandscapeScreen() {
        if(IsLandscape()) {
            return;
        }
        RotateScreen();
    }

    // -------------------------------------------------------------------------
    public TxCache GetTxCache() {
        return m_tx_cache;
    }

    // -------------------------------------------------------------------------
    public RendererProfiler.Stats GetProfilerStats() {
        return m_profiler.stats_cur;
    }

    // -------------------------------------------------------------------------
    public RendererLayer[] GetRenderables() {
        return m_layers;
    }

    // -------------------------------------------------------------------------
    public long GetFrameDuration() {
        return m_frame_cur - m_frame_prev;
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        ctx.layers = m_layers;
        ctx.cameras = m_cameras;
        ctx.model_batch = m_model_batch;
        ctx.decal_batch = m_decal_batch;
        ctx.profiler = m_profiler;

        m_frame_prev = m_frame_cur;
        m_frame_cur = Timer.GetMsec();
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        super.OnMethodPostRender(ctx, pre_children);

        // Before processing layers
        if(pre_children) {
            m_profiler.Reset();

            // Update environment
            Color c = Main.inst.cfg.render_clear_color;
            Gdx.gl.glClearColor(c.r, c.g, c.b, c.a);

            // Reset GL
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            // Begin rendering with default camera
            Camera camera = ctx.GetCamera(CameraType.LEVEL);
            m_model_batch.begin(camera.GetGdxCamera());

            // Begin billboard
            if(m_decal_group_strategy == null) {
                m_decal_group_strategy = 
                  new CameraGroupStrategy(camera.GetGdxCamera());
                m_decal_batch.setGroupStrategy(m_decal_group_strategy);
            }

            // Update environment config
            for(RendererEnv env : m_env_profiles.values()) {
                env.GetAttribStack().ApplyUpdates();
            }

            // Update rendering environment
            RendererEnv env = m_env_profiles.get("basic-specular");
            if(env != null) {
                RendererAttribStack stack = env.GetAttribStack();
                RendererAttrib.ADirLight.Cfg cfg = 
                  (Cfg)stack.GetAttribCfg(RendererAttrib.Type.DIR_LIGHT, 10);

                if(cfg != null) {
                    cfg.dir.rotate(Vector3.X, 20f * Main.inst.timer.GetDeltaSec());
                    stack.UpdateAttrib(RendererAttrib.Type.DIR_LIGHT, cfg);
                }
            }

        // After processing layers
        } else {
            // Finish model rendering
            m_model_batch.end();

            // Finish billboard rendering
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            m_decal_batch.flush();

            // Run profiler
            m_profiler.Update(null);

            // Get number of rendered objects
            m_model_num = m_model_visible_num = 
            m_triangle_num = m_triangle_visible_num = 
            m_particle_num = m_particle_visible_num = 
            m_billboard_num = m_billboard_visible_num = 0;
            for(int i = 0; i < ctx.layers.length; i++) {
                // Models
                RendererLayer layer = ctx.layers[i];
                if(layer instanceof RendererLayerModel) {
                    m_model_visible_num += layer.stat_robj_visible_num;
                    m_triangle_visible_num += layer.stat_robj_child_visible_num;

                    m_model_num += m_model_visible_num + layer.stat_robj_invisible_num;
                    m_triangle_num += m_triangle_visible_num + layer.stat_robj_child_invisible_num;

                // Particles
                } else if(layer instanceof RendererLayerParticle) {
                    m_particle_visible_num += layer.stat_robj_visible_num;
                    m_particle_num += m_particle_visible_num + layer.stat_robj_invisible_num;

                // Billboards
                } else if(layer instanceof RendererLayerBillboard) {
                    m_billboard_visible_num += layer.stat_robj_visible_num;
                    m_billboard_num += m_billboard_visible_num + layer.stat_robj_invisible_num;
                }
            }

            // Reset configuration update 
            for(RendererAttribStack.Cfg attrib_stack_cfg : 
              Main.inst.cfg.renderer_attrib_stacks.values()) {
                attrib_stack_cfg.ResetUpdates();
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_layers = null;

        m_model_batch.dispose();
        m_model_batch = null;

        m_tx_cache.Clear();
        m_tx_cache = null;
        return true;
    }
}
