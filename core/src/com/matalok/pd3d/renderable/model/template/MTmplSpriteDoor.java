//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteDoor 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteDoor
    //**************************************************************************
    private boolean m_is_open;

    //--------------------------------------------------------------------------
    public MTmplSpriteDoor(String tx_cache_key, DescSprite sprite, 
      boolean is_open) {
        super(tx_cache_key, sprite, 1.0f, false, true, null);
        m_is_open = is_open;
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        // Make vertical
        init_transform
          .translate(0.0f, 0.5f, 0.2f)
          .rotate(Vector3.X, 90.0f);

        // Rotate opened door
        if(m_is_open) {
            init_transform
              .rotate(Vector3.Z, -70.0f)
              .translate(0.35f, -0.4f, 0.0f);
        }
    }
}
