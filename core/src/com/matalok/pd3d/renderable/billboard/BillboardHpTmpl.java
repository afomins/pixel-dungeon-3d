//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.billboard;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class BillboardHpTmpl 
  extends BillboardTmpl {
    //**************************************************************************
    // BillboardHpTmpl
    //**************************************************************************
    public BillboardHpTmpl(Object key, String name, String tx_name, Color tx_color, float u, float v, 
      float u_size, float v_size) {
        super(key, name, tx_name, tx_color, u, v, u_size, v_size);
    }

    //**************************************************************************
    // ModelBase.ITemplate
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.setToTranslationAndScaling(
          0.0f, Main.inst.cfg.hp_vertical_offset, 0.0f,
          Main.inst.cfg.hp_scale_width, Main.inst.cfg.hp_scale_height, 1.0f);
    }
}
