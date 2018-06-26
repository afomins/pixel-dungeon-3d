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
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class GuiTextBox 
  extends VisScrollPane {
    //**************************************************************************
    // GuiTextBox
    //**************************************************************************
    private VisLabel m_text;

    //--------------------------------------------------------------------------
    public GuiTextBox(String text) {
        super(new VisTable());
        m_text = new VisLabel(text);
        m_text.setWrap(true);
        m_text.setFillParent(true);

        VisTable text_table = (VisTable)getActor();
        text_table.add(m_text).expand().fill();
        setOverscroll(false, false);
        setFadeScrollBars(false);
        setForceScroll(false, false);

        LabelStyle style = new LabelStyle(m_text.getStyle());
        style.background = new TextureRegionDrawable(
          Main.inst.gui.GetSkin().getAtlas().findRegion("slider-knob-disabled"));
        m_text.setStyle(style);
    }

    //--------------------------------------------------------------------------
    public GuiTextBox SetText(String text) {
        m_text.setText(text);
        return this;
    }
}
