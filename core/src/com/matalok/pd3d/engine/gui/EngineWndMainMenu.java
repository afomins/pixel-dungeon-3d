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
import com.matalok.pd3d.Scheduler.Event;
import com.matalok.pd3d.engine.Scene;
import com.matalok.pd3d.msg.MsgSwitchScene;

//------------------------------------------------------------------------------
public class EngineWndMainMenu 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndMainMenu
    //**************************************************************************
    public EngineWndMainMenu() {
        super("main menu", true, true, 0.6f, 0.0f, 
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

        // Settings
        AddCellButton(null, "Settings", null, btn_height,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.engine.PopState(1);
                  Main.inst.engine.PushState(
                    new Scene.StateShowMainsMenuSettings());
          }}).expand().fill().colspan(col_num)
             .getActor().setColor(Main.inst.cfg.gui_btn_color);

        row();
        AddCellButton(null, "Main menu", null, btn_height,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.proxy_client.Send(
                    MsgSwitchScene.CreateRequest("scene-title"));
          }}).expand().fill()
             .getActor().setColor(Main.inst.cfg.gui_btn_color);

        AddCellButton(null, "Exit game", null, btn_height, 
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.scheduler.ScheduleEvent(Event.QUIT);
          }}).expand().fill()
             .getActor().setColor(Main.inst.cfg.gui_btn_color);

        row();
        AddCellButton(null, "Return to game", null, btn_height,
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.engine.PopState(1);
          }}).expand().fill().colspan(col_num)
             .getActor().setColor(Main.inst.cfg.gui_btn_color);

        SetFixedSize(Main.inst.gui.IsLandscape() ? 0.6f : 0.8f, 0.0f);
        return this;
    }
}
