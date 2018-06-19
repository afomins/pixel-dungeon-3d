//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteNoShadow 
  extends MTmplSprite {
    //**************************************************************************
    // MTmplSpriteNoShadow
    //**************************************************************************
    public MTmplSpriteNoShadow(String tx_cache_key, DescSprite sprite, 
      Float alpha, boolean has_runtime_transform, boolean is_unit_size, Color color) {
        super(tx_cache_key, sprite, alpha, has_runtime_transform, is_unit_size, color);
    }

    //--------------------------------------------------------------------------
    protected void BuildMesh(GeomBuilder builder, long attributes, Material material, 
      float width, float height, DescRect rect) {
        int shadow_num = 0; // !!!
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, shadow_num, 0.1f, false, null);
    }
}
