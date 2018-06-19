//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.billboard;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.matalok.pd3d.renderable.RenderableTemplate;

//------------------------------------------------------------------------------
public class BillboardTmpl 
  extends RenderableTemplate {
    //**************************************************************************
    // BillboardTmpl
    //**************************************************************************
    public Object billboard_key;

    public String tx_name;
    public Color tx_color;
    public float u, v, u_size, v_size;

    //--------------------------------------------------------------------------
    public BillboardTmpl(Object key, String name, String tx_name, Color tx_color, float u, float v, 
      float u_size, float v_size) {
        super(name, 1.0f, false);
        this.billboard_key = key;

        this.tx_name = tx_name;
        this.tx_color = tx_color;
        this.u = u; 
        this.v = v; 
        this.u_size = u_size; 
        this.v_size = v_size;
    }

    //**************************************************************************
    // ModelBase.ITemplate
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.idt();
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        super.OnCleanup();
    }
}
