//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteChar 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteChar
    //**************************************************************************
    public MTmplSpriteChar(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 1.0f, false, true, null);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, 1, 0.1f, true, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.55f, 0.0f)
          .rotate(Vector3.X, 90.0f)
          .rotate(Vector3.Z, 90.0f)
          .scale(0.8f, 0.8f, 0.8f);
    }
}
