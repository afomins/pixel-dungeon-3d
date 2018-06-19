//------------------------------------------------------------------------------
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;

//------------------------------------------------------------------------------
public class RendererMaterial 
  extends Material 
  implements RendererAttribStack.IOwner {
    //**************************************************************************
    // RendererMaterial
    //**************************************************************************
    private RendererAttribStack m_attrib_container;

    //--------------------------------------------------------------------------
    public RendererMaterial(RendererAttribStack.Cfg cfg) {
        m_attrib_container = new RendererAttribStack(this, cfg);
    }

    //**************************************************************************
    // RendererAttribContainer.IOwner
    //**************************************************************************
    @Override public void SetAttrib(Object attrib) {
        if(attrib instanceof Attribute) {
            set((Attribute)attrib);
        }
    }

    //--------------------------------------------------------------------------
    @Override public RendererAttribStack GetAttribStack() {
        return m_attrib_container;
    }

    //--------------------------------------------------------------------------
    @Override public void ClearAttrib(long attrib_mask) {
        remove(mask);
    }
}
