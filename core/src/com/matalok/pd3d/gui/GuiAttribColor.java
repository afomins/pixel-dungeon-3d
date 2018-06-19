//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribColor 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribColor
    //**************************************************************************
    private Cell<GuiButtonChangeColor> m_color_picker;

    //--------------------------------------------------------------------------
    public GuiAttribColor(RendererAttrib.AColor.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.COLOR, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        m_color_picker = add(new GuiButtonChangeColor(
          ((RendererAttrib.AColor.Cfg)m_cfg).color, this));
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public void layout() {
        float h = getHeight();
        if(h > 0.0f) {
            m_color_picker.size(h);
        }
        super.layout();
    }
}
