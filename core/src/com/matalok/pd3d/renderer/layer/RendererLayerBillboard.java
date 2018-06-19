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
