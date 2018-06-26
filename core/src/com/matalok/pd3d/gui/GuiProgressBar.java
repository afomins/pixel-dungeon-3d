/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
