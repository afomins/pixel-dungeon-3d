//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import java.util.EnumSet;

import com.badlogic.gdx.graphics.Color;
import com.matalok.pd3d.GeomBuilder;

//------------------------------------------------------------------------------
public class MTmplBoxEx 
  extends MTmplBox {
    //**************************************************************************
    // TemplateBoxEx
    //**************************************************************************
    private float m_scale;

    //--------------------------------------------------------------------------
    public MTmplBoxEx(String name, boolean is_solid, 
      EnumSet<GeomBuilder.CubeSide> sides, Color color, float scale, Float alpha) {
        super(name, is_solid, sides, 1.0f, 1.0f, 1.0f, color, alpha);
        m_scale = scale;
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .idt()
          .scl(m_scale)
          .translate(0.0f, m_scale / 2, 0.0f);
    }
}
