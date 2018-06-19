//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteRipple 
  extends MTmplSpriteNoShadow {
    //**************************************************************************
    // MTmplSpriteRipple
    //**************************************************************************
    public MTmplSpriteRipple(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 0.4f, false, true, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.08f, 0.0f)
          .rotate(Vector3.Y, 18.0f)
          .scale(0.1f, 0.1f, 0.1f);
    }
}
