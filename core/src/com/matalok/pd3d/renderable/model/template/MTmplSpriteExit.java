//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Material;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteExit 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteExit
    //**************************************************************************
    public MTmplSpriteExit(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 1.0f, false, true, null);
    }

    //--------------------------------------------------------------------------
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, 0, 0.0f, false, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .scale(1.7f, 1.0f, 1.7f);
    }
}
