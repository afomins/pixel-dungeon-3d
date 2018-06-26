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
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

//------------------------------------------------------------------------------
public class GuiSpinnerInt 
  extends Spinner {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //......................................................................
        public void OnValueChanged(int value) {};
    }

    //**************************************************************************
    // GuiSpinnerInt
    //**************************************************************************
    public GuiSpinnerInt(String label, int init, int step, 
      final Listener listener) {
        this(label, init, -42424242, +42424242, step, listener);
    }

    //--------------------------------------------------------------------------
    public GuiSpinnerInt(String label, int init, int min, int max, 
      int step, final Listener listener) {
        super(label, new IntSpinnerModel(init, min, max, step));
        addListener(new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                if(listener != null) {
                    listener.OnValueChanged(GetValue());
                }
            }
        });
    }

    //--------------------------------------------------------------------------
    public int GetValue() {
        return ((IntSpinnerModel)getModel()).getValue();
    }

    //--------------------------------------------------------------------------
    public void SetValue(int val) {
        ((IntSpinnerModel)getModel()).setValue(val);
    }
}
