//------------------------------------------------------------------------------
package com.matalok.pd3d.renderer.layer;

import com.badlogic.gdx.graphics.Camera;
//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector2;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.node.GameNode.RenderCtx;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererEnv;
import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class RendererLayerModelWater 
  extends RendererLayerModel {
    //**************************************************************************
    // RendererLayerModelWater
    //**************************************************************************
    private Vector2 m_move_offset;
    private Vector2 m_move_step;

    //------------------------------------------------------------------------------
    public RendererLayerModelWater(String name, int size, boolean do_clear_depth, 
      Renderer.CameraType camera_type, int front_face) {
        super(name, size, do_clear_depth, camera_type, front_face, null);
        m_move_offset = new Vector2();
        m_move_step = new Vector2(Main.inst.cfg.water_move_dir)
          .scl(Main.inst.cfg.water_move_speed);
    }

    //**************************************************************************
    // RendererLayer
    //**************************************************************************
    @Override public void AddLayerObject(RenderCtx ctx, Object obj, boolean is_visible, 
      int child_num) {
        super.AddLayerObject(ctx, obj, is_visible, child_num);
        if(is_visible) {
            RendererModel model = (RendererModel)obj;
            model.mat_tx_diff.offsetU = m_move_offset.x;
            model.mat_tx_diff.offsetV = m_move_offset.y;
        }
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        m_move_offset
          .set(m_move_step)
          .scl(Main.inst.timer.GetCurSec());
        return true;
    }
}
