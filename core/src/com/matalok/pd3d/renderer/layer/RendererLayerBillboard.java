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
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererBillboard;
import com.matalok.pd3d.renderer.RendererEnv;

//------------------------------------------------------------------------------
public class RendererLayerBillboard 
  extends RendererLayer {
    //**************************************************************************
    // RendererLayerBillboard
    //**************************************************************************
    public RendererLayerBillboard(String name, int size, boolean do_clear_depth, 
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
            ctx.decal_batch.add(((RendererBillboard)obj).inst);
            stat_robj_visible_num++;
            stat_robj_child_visible_num += child_num;
        } else {
            stat_robj_invisible_num++;
            stat_robj_child_invisible_num += child_num;
        }
    }

    //--------------------------------------------------------------------------
    @Override public boolean UpdateLayer() {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public void RenderLayer(RenderCtx ctx, Camera gdx_camera, 
      RendererEnv environment) {
    }

    //--------------------------------------------------------------------------
    @Override public void ClearLayer() {
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
