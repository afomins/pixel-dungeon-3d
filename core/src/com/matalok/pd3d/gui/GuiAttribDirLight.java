//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribDirLight 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribDirLight
    //**************************************************************************
    private Cell<GuiButtonChangeColor> m_color;

    //--------------------------------------------------------------------------
    public GuiAttribDirLight(RendererAttrib.ADirLight.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.DIR_LIGHT, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        add(new GuiButtonChangeVector3(
          ((RendererAttrib.ADirLight.Cfg)m_cfg).dir, this));
        m_color = add(new GuiButtonChangeColor(
          ((RendererAttrib.ADirLight.Cfg)m_cfg).color, this));
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public void layout() {
        float h = getHeight();
        if(h > 0.0f) {
            m_color.size(h);
        }
        super.layout();
    }
}
