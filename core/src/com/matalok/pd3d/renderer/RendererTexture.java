//------------------------------------------------------------------------------
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class RendererTexture
  implements IManaged {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static Pixmap CreateShadowPixmap(Pixmap pm_orig) {
        // Create new pixmap with extra space for shadow
        Pixmap pm = new Pixmap(
          pm_orig.getWidth(), pm_orig.getHeight() * 2, pm_orig.getFormat());
        pm.drawPixmap(pm_orig, 0, 0);

        // Fill new pixmap with shadow data
        int shadow_alpha = (int)(0xff * 1.0f);
        int shadow_color = Color.rgba8888(Color.DARK_GRAY) & 0xffffff00;
        for(int y = 0; y < pm_orig.getHeight(); y++) {
            for(int x = 0; x < pm_orig.getWidth(); x++) {
                int color_orig = pm_orig.getPixel(x, y);
                int alpha_orig = color_orig & 0xff;

                // Shadow
                int color = shadow_color | (color_orig & shadow_alpha);
                pm.drawPixel(x, y + pm_orig.getHeight(), color);

                // Orig
                if(alpha_orig > 0) {
                    pm.drawPixel(x, y, color_orig | 0xff);
                }
            }
        }
        return pm;
    }

    //--------------------------------------------------------------------------
    public static Pixmap CutPixmap(Pixmap pm_orig, int x, int y, int width, 
      int height) {
        Pixmap pm = new Pixmap(width, height, pm_orig.getFormat());
        pm.drawPixmap(pm_orig, 0, 0, x, y, width, height);
        return pm;
    }

    //--------------------------------------------------------------------------
    public static Pixmap CutPixmap(Pixmap pm_orig, int x, int y, int width, 
      int height, float scale_x, float scale_y) {
        Pixmap pm = new Pixmap((int)(width * scale_x), (int)(height * scale_y), 
          pm_orig.getFormat());
        pm.drawPixmap(pm_orig, x, y, width, height, 0, 0, pm.getWidth(), pm.getHeight());
        return pm;
    }

    //--------------------------------------------------------------------------
    public static Pixmap SetPixmapAlpha(Pixmap pm, float alpha) {
        Blending blending_old = pm.getBlending();
        pm.setBlending(Blending.None);

        int new_alpha = (int)(alpha * 255.0f);
        for(int y = 0; y < pm.getHeight(); y++) {
            for(int x = 0; x < pm.getWidth(); x++) {
                int color_orig = pm.getPixel(x, y) & 0xffffff00;
                pm.drawPixel(x, y, color_orig | new_alpha);
            }
        }
        pm.setBlending(blending_old);
        return pm;
    }

    //**************************************************************************
    // RendererTexture
    //**************************************************************************
    public Texture tx;
    public String name;

    //--------------------------------------------------------------------------
    public RendererTexture(String name, Texture tx, boolean do_repeat) {
        this.tx = tx;
        this.name = name;

        if(do_repeat) {
            tx.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        }
    }

    //--------------------------------------------------------------------------
    public TextureRegion GetTxRegion(DescSprite sprite, String anim_name) {
        DescRect rect = sprite.GetFirstAnimRect(anim_name);
        Utils.Assert(rect != null, "Failed to texture region :: tx=%s", sprite.texture);
        return new TextureRegion(tx, 
          rect.x, rect.y + sprite.tile_offset * rect.height, 
          rect.width, rect.height);
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        tx.dispose();
        tx = null;
        name = null;
    }
}
