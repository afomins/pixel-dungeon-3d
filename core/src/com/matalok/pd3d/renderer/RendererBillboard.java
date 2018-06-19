//------------------------------------------------------------------------------
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class RendererBillboard
  implements IManaged {
    //**************************************************************************
    // RendererBillboard
    //**************************************************************************
    public Decal inst;

    //--------------------------------------------------------------------------
    public RendererBillboard(float width, float height, TextureRegion tx_reg) {
        inst = Decal.newDecal(width, height, tx_reg, true);
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        inst = null;
    }
}
