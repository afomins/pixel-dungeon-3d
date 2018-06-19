//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTable;

//------------------------------------------------------------------------------
public class GuiProgressBar 
  extends VisTable {
    //--------------------------------------------------------------------------
    private Cell<VisImage> m_left;
    private Cell<VisImage> m_right;
    private Texture m_tx;
    private float m_width;

    //**************************************************************************
    // GuiMinimap
    //**************************************************************************
    public GuiProgressBar(Color color_left, Color color_right, float width) {
        Pixmap pm = new Pixmap(2, 1, Format.RGBA4444);
        pm.drawPixel(0, 0, Color.rgba8888(color_left));
        pm.drawPixel(1, 0, Color.rgba8888(color_right));
        m_tx = new Texture(pm);

        m_left = add(new VisImage(new TextureRegion(m_tx, 0.0f, 0.0f, 0.5f, 1.0f)))
          .expand().fill();
        m_right = add(new VisImage(new TextureRegion(m_tx, 0.5f, 0.0f, 1.0f, 1.0f)))
          .expand().fill();
        m_width = width;
    }

    //--------------------------------------------------------------------------
    public void Update(float progress) {
        float left = m_width * progress;
        m_left.width(left);
        m_right.width(m_width - left);
        invalidate();
    }
}
