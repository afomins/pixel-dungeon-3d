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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class GuiWndAttribVector3 
  extends GuiWndAttrib {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //----------------------------------------------------------------------
        public void OnUpdate(Vector3 v) {};
    }

    //**************************************************************************
    // GuiWndAttribVector3
    //**************************************************************************
    private Listener m_listener;
    private Vector3 m_value, m_value_orig;
    private float m_polar_h, m_polar_v, m_polar_l;
    private GuiSpinnerFloat m_spinner_x, m_spinner_y, m_spinner_z;
    private GuiSpinnerFloat m_spinner_h, m_spinner_v, m_spinner_l;

    //--------------------------------------------------------------------------
    public GuiWndAttribVector3() {
        super("Vector3");
    }

    //--------------------------------------------------------------------------
    public void Init(Vector3 init, Listener listener) {
        m_value_orig.set(init);
        SetXYZ(init.x, init.y, init.z);
        m_listener = listener;
    }

    //--------------------------------------------------------------------------
    private void SetXYZ(float x, float y, float z) {
        m_value.set(x, y, z);

        // XYZ
        m_spinner_x.SetValue(x, false);
        m_spinner_y.SetValue(y, false);
        m_spinner_z.SetValue(z, false);

        // Polar length
        m_polar_l = Vector3.len2(x, y, z);
        m_spinner_l.SetValue(m_polar_l, false);

        // Polar horizontal
        m_polar_h = 
          (x == 0.0f && z == 0.0f) ?  0.0f :
          (x == 0.0f && z >  0.0f) ?  0.0f :
          (x == 0.0f && z <  0.0f) ?  MathUtils.PI :
          (x >  0.0f && z == 0.0f) ?  MathUtils.PI / 2.0f :
          (x <  0.0f && z == 0.0f) ?  MathUtils.PI / 2.0f * 3.0f :
          MathUtils.atan2(x, z);
        m_polar_h = Utils.NormalizeAngle(m_polar_h * MathUtils.radiansToDegrees);
        m_spinner_h.SetValue(m_polar_h, false);

        // Polar vertical
        m_polar_v = 
          (y == 0.0f || m_polar_l == 0.0f) ? 0.0f:
          (float)Math.asin(y / m_polar_l);
        m_polar_v *= MathUtils.radiansToDegrees;
        m_spinner_v.SetValue(m_polar_v, false);

        // Notify listener
        if(m_listener != null) {
            m_listener.OnUpdate(m_value);
        }
    }

    //--------------------------------------------------------------------------
    private void SetPolar(float h, float v, float l) {
        m_spinner_h.SetValue(m_polar_h = h, false);
        m_spinner_v.SetValue(m_polar_v = v, false);
        m_spinner_l.SetValue(m_polar_l = l, false);

        float y = l * MathUtils.sinDeg(v);
        float z = l * MathUtils.cosDeg(h);
        float x = l * MathUtils.sinDeg(h);

        m_value.set(x, y, z);
        m_spinner_x.SetValue(x, false);
        m_spinner_y.SetValue(y, false);
        m_spinner_z.SetValue(z, false);
        if(m_listener != null) {
            m_listener.OnUpdate(m_value);
        }
    }

    //**************************************************************************
    // GuiWndAttrib
    //**************************************************************************
    @Override public int OnCreateBody() {
        m_value = new Vector3();
        m_value_orig = new Vector3();
        float width = 150.0f;

        // X
        row(); 
        add(m_spinner_x = new GuiSpinnerFloat("x:", 0.0f, 0.1f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  SetXYZ(value, m_value.y, m_value.z);
              };
        })).expandX().fillX().width(width);

        // Horizontal angle
        add(m_spinner_h = new GuiSpinnerFloat("h:", 0.0f, 10.0f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  SetPolar(value, m_polar_v, m_polar_l);
              };
        })).expandX().fillX().width(width);

        // Y
        row(); 
        add(m_spinner_y = new GuiSpinnerFloat("y:", 0.0f, 0.1f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  SetXYZ(m_value.x, value, m_value.z);
              };
        })).expandX().fillX();

        // Vertical angle
        add(m_spinner_v = new GuiSpinnerFloat("v:", 0.0f, 10.0f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  SetPolar(m_polar_h, value, m_polar_l);
              };
        })).expandX().fillX();

        // Z
        row(); 
        add(m_spinner_z = new GuiSpinnerFloat("z:", 0.0f, 0.1f, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  SetXYZ(m_value.x, m_value.y, value);
              };
        })).expandX().fillX();

        // Length
        add(m_spinner_l = new GuiSpinnerFloat("l:", 0.0f, 0.01f, 2,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  SetPolar(m_polar_h, m_polar_v, value);
              };
        })).expandX().fillX();
        return 2;
    }

    //--------------------------------------------------------------------------
    @Override public void OnRestoreSettings() {
        SetXYZ(m_value_orig.x, m_value_orig.y, m_value_orig.z);
    }
}
