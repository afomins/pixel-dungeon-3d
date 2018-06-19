//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribBlending 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribBlending
    //**************************************************************************
    public GuiAttribBlending(RendererAttrib.ABlending.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.BLENDING, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        add(new GuiButtonChangeBlending(this));
    }
}
