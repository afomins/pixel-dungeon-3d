//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribFloat 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribFloat
    //**************************************************************************
    public GuiAttribFloat(RendererAttrib.AFloat.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.FLOAT, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        final RendererAttrib.AFloat.Cfg cfg = (RendererAttrib.AFloat.Cfg)m_cfg;

        add(new GuiSpinnerFloat(null, cfg.value, 0.0f, 1.0f, 0.1f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  cfg.value = value;
                  OnUpdate();
              };
        })).expandX().fillX();
    }
}
