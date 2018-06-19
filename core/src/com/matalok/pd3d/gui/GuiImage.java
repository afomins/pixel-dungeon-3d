//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.widget.VisImage;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class GuiImage 
  extends VisImage {
    //**************************************************************************
    // Guiimage
    //**************************************************************************
    private Enum<?> m_sprite;

    //--------------------------------------------------------------------------
    public GuiImage() {
        super();
    }

    //--------------------------------------------------------------------------
    public GuiImage(Enum<?> sprite) {
        this();
        SetSprite(sprite);
    }

    //--------------------------------------------------------------------------
    public VisImage SetSprite(Enum<?> sprite) {
        if(m_sprite == sprite) {
            return this;
        }

        m_sprite = sprite;
        TextureRegion tx = (sprite == null) ? null : 
          Main.inst.renderable_man.GetTextureRegion(sprite, null);
        setDrawable((tx == null) ? null : new TextureRegionDrawable(tx));
        return this;
    }
}
