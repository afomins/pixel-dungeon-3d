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
import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

//------------------------------------------------------------------------------
public class GuiSpinnerVector3 
  extends VisTable {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //......................................................................
        public void OnValueChanged() {};
    }

    //**************************************************************************
    // GuiSpinnerVector3
    //**************************************************************************
    public GuiSpinnerVector3(String label, final Vector3 target, float step, 
      final Listener listener) {
        add(new VisLabel(label));

        // X
        add(new GuiSpinnerFloat("x: ", target.x, step, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  target.x = value;
                  if(listener != null) {
                      listener.OnValueChanged();
                  }
              };
        }));

        // Y
        add(new GuiSpinnerFloat("y: ", target.y, step, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  target.y = value;
                  if(listener != null) {
                      listener.OnValueChanged();
                  }
              };
        }));

        // Z
        add(new GuiSpinnerFloat("z: ", target.z, step, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  target.z = value;
                  if(listener != null) {
                      listener.OnValueChanged();
                  }
              };
        }));
    }
}
