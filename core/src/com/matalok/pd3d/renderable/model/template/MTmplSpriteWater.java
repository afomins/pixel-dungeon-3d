//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Material;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteWater 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteWater
    //**************************************************************************
    public MTmplSpriteWater(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, Main.inst.cfg.water_alpha, false, true, null);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override public void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, 0, 0.0f, false, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.05f, 0.0f);
    }
}
