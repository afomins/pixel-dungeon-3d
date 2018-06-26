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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.matalok.pd3d.Main;

//------------------------------------------------------------------------------
public class GuiToggleButton
  extends VisTextButton {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //----------------------------------------------------------------------
        public abstract void OnValueChanged(boolean value);
    }

    //**************************************************************************
    // GuiSpinnerInt
    //**************************************************************************
    private Cell<VisTable> m_cb_table;
    private VisCheckBox m_cb;
    private Listener m_listener;

    //--------------------------------------------------------------------------
    public GuiToggleButton(String text, boolean init, Listener listener) {
        super(text);

        m_listener = listener;

        getLabel().setAlignment(Align.center);
        getLabelCell().expand().fill();

        // Checkbox table
        m_cb_table = add(new VisTable())
          .expandY().fillY().align(Align.right);

        // Checkbox
        m_cb = m_cb_table.getActor()
          .add(new VisCheckBox(null, init))
          .expand().fill().getActor();
        m_cb.getImageStack().setFillParent(true);
        m_cb.align(Align.bottom);
        m_cb.setTouchable(Touchable.disabled);

        addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                boolean new_value = !IsChecked();
                SetChecked(new_value, true);
                m_listener.OnValueChanged(new_value);
            }
        });

        // No-border and custom color
        setFocusBorderEnabled(false);
        setColor(Main.inst.cfg.gui_btn_color);
    }

    //--------------------------------------------------------------------------
    public boolean IsChecked() {
        return m_cb.isChecked();
    }

    //--------------------------------------------------------------------------
    public void SetChecked(boolean is_checed, boolean fire_event) {
        m_cb.setProgrammaticChangeEvents(fire_event);
        m_cb.setChecked(is_checed);
        m_cb.setProgrammaticChangeEvents(true);
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public void layout() {
        float h = getHeight();
        m_cb_table.padTop(h * 0.1f).padRight(h * 0.23f);
        m_cb_table.size(h * 0.7f);
        super.layout();
    }
}
