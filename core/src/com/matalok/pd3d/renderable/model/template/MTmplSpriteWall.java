//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;

//------------------------------------------------------------------------------
public class MTmplSpriteWall 
  extends MTmplSprite {
    //**************************************************************************
    // ModelSpriteWall
    //**************************************************************************
    public MTmplSpriteWall(String tx_cache_key, DescSprite sprite) {
        super(tx_cache_key, sprite, 1.0f, false, true, null);
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    @Override protected void BuildMesh(GeomBuilder builder, long attributes, 
      Material material, float width, float height, DescRect rect) {
        // Define cube sides
        Matrix4 front = new Matrix4().rotate(Vector3.X, 90.0f);
        Matrix4[] transform = new Matrix4[] {
            front,                                        // Front
            new Matrix4(front).rotate(Vector3.Z, 90.0f),  // Right
            new Matrix4(front).rotate(Vector3.Z, 180.0f), // Back
            new Matrix4(front).rotate(Vector3.Z, 270.0f), // Left
            new Matrix4(),                                // Top
        };

        // Create cube
        for(Matrix4 t : transform) {
            t.translate(0.0f, 0.5f, 0.0f);
            MTmplSprite.CreatePlane(builder, attributes, material, 
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
