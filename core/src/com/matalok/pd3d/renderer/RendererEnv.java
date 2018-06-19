//------------------------------------------------------------------------------
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;

//------------------------------------------------------------------------------
public class RendererEnv 
  extends Environment 
  implements RendererAttribStack.IOwner {
    //**************************************************************************
    // RendererEnv
    //**************************************************************************
    private RendererAttribStack m_attrib_container;

    //--------------------------------------------------------------------------
    public RendererEnv(RendererAttribStack.Cfg cfg) {
        m_attrib_container = new RendererAttribStack(this, cfg);
    }

    //**************************************************************************
    // RendererAttribContainer.IOwner
    //**************************************************************************
    @SuppressWarnings("rawtypes")
    @Override public void SetAttrib(Object attrib) {
        if(attrib instanceof Attribute) {
            set((Attribute)attrib);
        } else if(attrib instanceof BaseLight) {
            add((BaseLight)attrib);
        }
    }

    //--------------------------------------------------------------------------
    @Override public RendererAttribStack GetAttribStack() {
        return m_attrib_container;
    }

    //--------------------------------------------------------------------------
    @Override public void ClearAttrib(long mask) {
        remove(mask);
    }
}
