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
import com.badlogic.gdx.graphics.Camera;
import com.matalok.pd3d.Timer;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererEnv;
import com.matalok.pd3d.renderer.RendererParticle;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class RendererLayerParticle 
  extends RendererLayer {
    //**************************************************************************
    // RendererLayerParticle
    //**************************************************************************
    private UtilsClass.PeriodicTask m_pfx_update_task;

    //--------------------------------------------------------------------------
    public RendererLayerParticle(String name, int size, boolean do_clear_depth, 
      Renderer.CameraType camera_type, int front_face, Float hack_draw_distance) {
        super(name, size, do_clear_depth, camera_type, front_face,
          hack_draw_distance);
    }

    //**************************************************************************
    // RendererLayer
    //**************************************************************************
    @Override public void AddLayerObject(RenderCtx ctx, Object obj, boolean is_visible, 
      int child_num) {
        if(is_visible) {
            RendererParticle.manager.system.add(((RendererParticle)obj).inst);
            stat_robj_visible_num++;
            stat_robj_child_visible_num += child_num;
        } else {
            stat_robj_invisible_num++;
            stat_robj_child_invisible_num += child_num;
        }
    }

    //--------------------------------------------------------------------------
    @Override public boolean UpdateLayer() {
        // Periodically update particles
        long cur_time = Timer.GetMsec();
        if(m_pfx_update_task == null) {
            m_pfx_update_task = new UtilsClass.PeriodicTask(
              cur_time, 10, 
              new UtilsClass.Callback() {
                  @Override public Object Run(Object... args) {
                      if(stat_robj_visible_num > 0) {
                          for(int i = 0; i < (int)args[0]; i++) {
                              RendererParticle.manager.system.update();
                          }
                      }
                      return null;
              }});
        }
        m_pfx_update_task.Run(cur_time);
        return (stat_robj_visible_num > 0);
    }

    //--------------------------------------------------------------------------
    @Override public void RenderLayer(RenderCtx ctx, 
      Camera gdx_camera, RendererEnv environment) {
        RendererParticle.manager.batch.setCamera(gdx_camera);
        RendererParticle.manager.system.begin();
        RendererParticle.manager.system.draw();
        RendererParticle.manager.system.end();
        ctx.model_batch.render(RendererParticle.manager.system, environment);
        ctx.model_batch.flush();
    }

    //--------------------------------------------------------------------------
    @Override public void ClearLayer() {
        RendererParticle.manager.system.removeAll();
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        ClearLayer();
        return true;
    }
}
