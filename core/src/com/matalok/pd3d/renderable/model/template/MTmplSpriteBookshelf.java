//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class MTmplSpriteBookshelf 
  extends MTmplSprite {
    //**************************************************************************
    // TemplateSpriteBookshelf
    //**************************************************************************
    public MTmplSpriteBookshelf(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 1.0f, false, true, null);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material mat_books, float width, float height, DescRect rect) {
        Material mat_border = RendererModel.CreateMaterial(
          new Color(0.24f, 0.22f, 0.12f, 1.0f), null, true, true);

        // Define bookshelf sides sides
        float scale = 0.95f;
        Matrix4 front = new Matrix4().scl(scale).rotate(Vector3.X, 90.0f);
        Object[] transform = new Object[] {
            mat_books, front,                                        // Front
            mat_books, new Matrix4(front).rotate(Vector3.Z, 90.0f),  // Right
            mat_books, new Matrix4(front).rotate(Vector3.Z, 180.0f), // Back
            mat_books, new Matrix4(front).rotate(Vector3.Z, 270.0f), // Left
            mat_border, new Matrix4().scl(scale),                    // Top
        };

        // Create bookshelf cube
        for(int i = 0; i < transform.length / 2; i++) {
            Material m = (Material)transform[i * 2];
            Matrix4 t = (Matrix4)transform[i * 2 + 1];
            t.translate(0.0f, 0.5f, 0.0f);
            MTmplSprite.CreatePlane(builder, attributes, m, 
              width, height, rect, 0, 0.0f, false, t);
        }
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform
          .translate(0.0f, 0.5f, 0.0f);
    }
}
