//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//-------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteChest
  extends MTmplSpriteVertical {
    //**************************************************************************
    // TemplateSpriteChest
    //**************************************************************************
    public MTmplSpriteChest(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, 4, 0.2f, true, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.5f, 0.0f)
          .rotate(Vector3.X, 90.0f)
          .scale(0.7f, 0.7f, 0.7f);
    }
}
