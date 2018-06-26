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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

//------------------------------------------------------------------------------
public class GuiSpinnerFloat 
  extends Spinner {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //----------------------------------------------------------------------
        public void OnValueChanged(float value) {};
    }

    //**************************************************************************
    // GuiSpinnerFloat
    //**************************************************************************
    public GuiSpinnerFloat(String label, float init, float step, int precission,
      final Listener listener) {
        this(label, init, -42424242, +42424242, step, precission, listener);
    }

    //--------------------------------------------------------------------------
    public GuiSpinnerFloat(String label, float init, float min, float max, 
      float step, int precission, final Listener listener) {
        super(label, new SimpleFloatSpinnerModel(init, min, max, step, precission));
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if(listener != null) {
                    listener.OnValueChanged(GetValue());
                }
            }
        });
    }

    //--------------------------------------------------------------------------
    public float GetValue() {
        return ((SimpleFloatSpinnerModel)getModel()).getValue();
    }

    //--------------------------------------------------------------------------
    public void SetValue(float value, boolean fire_event) {
        ((SimpleFloatSpinnerModel)getModel()).setValue(value, fire_event);
    }
}
