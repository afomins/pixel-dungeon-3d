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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

//------------------------------------------------------------------------------
public class GuiColorPicker 
  extends VisTable {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static final Drawable white = VisUI.getSkin().getDrawable("white");

    //--------------------------------------------------------------------------
    private Cell<GuiButton> m_button;
    private ColorPicker m_color_picker;
    private Color m_target;

    //**************************************************************************
    // GuiColorPicker
    //**************************************************************************
    public GuiColorPicker(String text, float width, float height, Color target) {
        m_target = target;

        // Color picker window
        m_color_picker = new ColorPicker(text, new ColorPickerAdapter() {
            @Override public void canceled (Color oldColor) {
                UpdateColor(oldColor);
            }
            @Override public void changed (Color newColor) {
                UpdateColor(newColor);
            }
            @Override public void finished (Color newColor) {
                UpdateColor(newColor);
            }
        });
        m_color_picker.setColor(target);

        // Label
        add(new VisLabel(text + ": "));

        // Button
        m_button = add(new GuiButton(null, null, width, height, null, null, false, 
          new ChangeListener() {
              @Override public void changed(ChangeEvent event, Actor actor) {
                  getStage().addActor(m_color_picker.fadeIn());
              }})
        );
        GuiImage img = m_button.getActor().GetImage();
        img.setDrawable(white);
        UpdateColor(target);
    }

    //--------------------------------------------------------------------------
    private void UpdateColor(Color color) {
        if(m_button == null) {
            return;
        }
        m_button.getActor().GetImage().setColor(color);
        m_target.set(color);
    }
}
