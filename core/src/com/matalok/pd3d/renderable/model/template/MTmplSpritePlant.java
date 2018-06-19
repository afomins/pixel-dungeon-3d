//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpritePlant 
  extends MTmplSpriteGrassHigh {
    //**************************************************************************
    // TemplateSpritePlant
    //**************************************************************************
    public MTmplSpritePlant(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.scl(0.7f);
        super.UpdateInitialTransform();
    }
}
