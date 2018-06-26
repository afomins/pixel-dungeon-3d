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
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.gui.GuiSpinnerEx;
import com.matalok.pd3d.gui.GuiToggleButton;
import com.matalok.pd3d.msg.MsgCommand;

//------------------------------------------------------------------------------
public class EngineWndSettings 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndSettings
    //**************************************************************************
    public EngineWndSettings() {
        super("settings", true, true, 0.6f, 0.0f, 
          InputAction.CONSUME,      // OnTouchInArea
          InputAction.POP_STATE,    // OnTouchOutArea
          InputAction.POP_STATE,    // OnKeyPress
          InputAction.POP_STATE,    // OnBack
          0.7f, null, 0.0f);
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        float btn_height = GetButtonSize();
        int col_num = 2;

        // Screen orientation
        boolean value = Main.inst.renderer.IsLandscape();
        AddCellToggleButton(null, "Landscape", value, null, btn_height, 
          new GuiToggleButton.Listener() {
              @Override public void OnValueChanged(boolean value) {
                  Main.inst.cfg.app_landscape = value;
                  Main.inst.SaveConfig();
                  Main.inst.renderer.RotateScreen();
          }
        }).colspan(1).expand().fill().uniform();

        // Large font
        value = (Main.inst.cfg.gui_font_scale == 2);
        AddCellToggleButton(null, "Large font", value, null, btn_height, 
          new GuiToggleButton.Listener() {
              @Override public void OnValueChanged(boolean value) {
                  Main.inst.gui.Scale(value ? 2 : 1, null, true);
              }
        }).colspan(1).expand().fill().uniform();

        // Icon scale
        row();
        AddCellSpinnerExFloat(null, "Icon scale", Main.inst.cfg.gui_icon_scale, 
          0.4f, 3.0f, 0.2f, 1, null, btn_height, 
          new GuiSpinnerEx.Listener<Float>() {
            @Override public void OnValueChanged(Float value) {
                Main.inst.gui.Scale(null, value, true);
            }
        }).colspan(col_num).expand().fill();

        // Sound
        row();
        value = (Main.inst.cfg.app_sound_volume > 0.0f);
        AddCellToggleButton(null, "Sound", value, null, btn_height, 
          new GuiToggleButton.Listener() {
              @Override public void OnValueChanged(boolean value) {
                  Main.inst.cfg.app_sound_volume = value ? 1.0f : 0.0f;
                  Main.inst.SaveConfig();

                  MsgCommand msg = MsgCommand.CreateRequest();
                  msg.sound = value;
                  Main.inst.proxy_client.Send(msg);
          }
        }).colspan(1).expand().fill().uniform();

        // Music
        value = (Main.inst.cfg.app_music_volume > 0.0f);
        AddCellToggleButton(null, "Music", value, null, btn_height, 
          new GuiToggleButton.Listener() {
              @Override public void OnValueChanged(boolean value) {
                  Main.inst.cfg.app_music_volume = value ? 1.0f : 0.0f;
                  Main.inst.SaveConfig();

                  MsgCommand msg = MsgCommand.CreateRequest();
                  msg.music = value;
                  Main.inst.proxy_client.Send(msg);
          }
        }).colspan(1).expand().fill().uniform();

        // Auto movement
        row();
        value = Main.inst.cfg.lvl_hero_auto_move;
        AddCellToggleButton(null, "Hero auto-movement", value, null, btn_height, 
          new GuiToggleButton.Listener() {
              @Override public void OnValueChanged(boolean value) {
                  Main.inst.cfg.lvl_hero_auto_move = value;
                  Main.inst.SaveConfig();
          }
        }).colspan(col_num).expand().fill();

        SetFixedSize(Main.inst.gui.IsLandscape() ? 0.6f : 0.8f, 0.0f);
        return this;
    }
}
