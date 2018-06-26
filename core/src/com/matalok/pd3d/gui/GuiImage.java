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
