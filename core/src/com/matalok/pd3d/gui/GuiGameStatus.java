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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.gui.EngineWnd;

//------------------------------------------------------------------------------
public class GuiGameStatus 
  extends VisTable {
    //--------------------------------------------------------------------------
    private Cell<GuiBusyIndicator> m_busy;
    private Cell<VisLabel> m_text;
    private int m_game_step;

    //**************************************************************************
    // GuiGameStatus
    //**************************************************************************
    public GuiGameStatus(Enum<?> busy_sprite, float busy_size) {
        TableUtils.setSpacingDefaults(this);

        m_busy = add(new GuiBusyIndicator(
          Main.inst.renderable_man.GetTextureRegion(busy_sprite, null)));
        EngineWnd.SetCellSize(m_busy, busy_size, busy_size);

        m_text = add(new VisLabel("READY PLAYER ONE"));
        LabelStyle status_style = m_text.getActor().getStyle();
        status_style.font = VisUI.getSkin().getFont("default-shadow-font");
        m_text.getActor().setStyle(status_style);
    }

    //--------------------------------------------------------------------------
    public void SetBusy(boolean is_busy) {
        m_busy.getActor().SetRotation(is_busy);
    }

    //--------------------------------------------------------------------------
    public void SetText(String text, int game_step) {
        text = text.toUpperCase() + " >> " + game_step;
        if(game_step != m_game_step) {
            text += " [+" + Integer.toString(game_step - m_game_step) + "]";
            m_game_step = game_step;
        }
        m_text.getActor().setText(text);
    }
}
