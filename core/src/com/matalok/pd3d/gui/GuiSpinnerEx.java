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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class GuiSpinnerEx<T>
  extends VisTable {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener<T> {
        //----------------------------------------------------------------------
        public abstract void OnValueChanged(T value);
    }

    //**************************************************************************
    // GuiSpinnerInt
    //**************************************************************************
    private Cell<VisTextButton> m_btn_inc, m_btn_dec;
    private Cell<VisLabel> m_label;
    private Listener<T> m_listener;
    private UtilsClass.TemplateMath<T> m_template_math;
    private T m_value;
    private int m_precission;
    private String m_label_prefix;

    //--------------------------------------------------------------------------
    public GuiSpinnerEx(String label, T init, final T min, final T max, final T step, 
      int precission, UtilsClass.TemplateMath<T> template_math, 
      final Listener<T> listener) {
        m_precission = precission;
        m_listener = listener;
        m_template_math = template_math;
        m_label_prefix = label;

        // Decrement button
        m_btn_dec = add(new VisTextButton("-")).expandY().fillY();
        m_btn_dec.getActor().addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SetValue(m_template_math.Dec(m_value, step, min, max));
            }
        });
        m_btn_dec.getActor().setColor(Main.inst.cfg.gui_btn_color);

        // Label
        m_label = add(new VisLabel()).expand().fill();
        m_label.getActor().setAlignment(Align.center);

        // Increment button
        m_btn_inc = add(new VisTextButton("+")).expandY().fillY();
        m_btn_inc.getActor().addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                SetValue(m_template_math.Inc(m_value, step, min, max));
            }
        });
        m_btn_inc.getActor().setColor(Main.inst.cfg.gui_btn_color);

        SetValue(init);
    }

    //--------------------------------------------------------------------------
    public void SetValue(T value) {
        if(m_value != null && m_value.equals(value)) {
            return;
        }

        m_label.getActor().setText(
          m_label_prefix + " [" + m_template_math.ToString(value, m_precission) + "]");

        if(m_value != null) {
            m_listener.OnValueChanged(value);
        }
        m_value = value;
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public void layout() {
        float h = getHeight();
        if(h > 0.0f) {
            m_btn_dec.width(h);
            m_btn_inc.width(h);
        }
        super.layout();
    }
}
