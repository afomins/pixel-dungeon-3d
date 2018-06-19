//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteText 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteText
    //**************************************************************************
    public MTmplSpriteText(String tx_cache_key, DescSprite sprite, Color color) {
        super(tx_cache_key, sprite, 1.0f, false, false, color);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, 0, 0.1f, true, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .scl(Main.inst.cfg.model_text_scale);
    }
}
