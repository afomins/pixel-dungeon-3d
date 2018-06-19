//------------------------------------------------------------------------------
package com.matalok.pd3d.renderer.layer;

//------------------------------------------------------------------------------
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererEnv;
import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class RendererLayerModel 
  extends RendererLayer {
    //**************************************************************************
    // RendererLayerModel
    //**************************************************************************
    private ArrayList<RenderableProvider> m_list;

    //--------------------------------------------------------------------------
    public RendererLayerModel(String name, int size, boolean do_clear_depth, 
      Renderer.CameraType camera_type, int front_face, Float hack_draw_distance) {
        super(name, size, do_clear_depth, camera_type, front_face,
          hack_draw_distance);

        m_list = new ArrayList<RenderableProvider>(size);
    }

    //**************************************************************************
    // RendererLayer
    //**************************************************************************
    @Override public void AddLayerObject(RenderCtx ctx, Object obj, boolean is_visible, 
      int child_num) {
        if(is_visible) {
            m_list.add(((RendererModel)obj).inst);
            stat_robj_visible_num++;
            stat_robj_child_visible_num += child_num;
        } else {
            stat_robj_invisible_num++;
            stat_robj_child_invisible_num += child_num;
        }
    }

    //--------------------------------------------------------------------------
    @Override public boolean UpdateLayer() {
        return (stat_robj_visible_num > 0);
    }

    //--------------------------------------------------------------------------
    @Override public void RenderLayer(RenderCtx ctx, Camera gdx_camera, 
      RendererEnv environment) {
        ctx.model_batch.render(m_list, environment);
        ctx.model_batch.flush();
    }

    //--------------------------------------------------------------------------
    @Override public void ClearLayer() {
        m_list.clear();
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        ClearLayer();
        m_list = null;
        return true;
    }
}
