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
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.ButtonBar;
import com.kotcrab.vis.ui.widget.ButtonBar.ButtonType;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.color.ColorPickerStyle;
import static com.kotcrab.vis.ui.widget.color.internal.ColorPickerText.*;

//------------------------------------------------------------------------------
public class GuiWndAttrib 
  extends VisWindow {
    //**************************************************************************
    // GuiWndAttrib
    //**************************************************************************
    public GuiWndAttrib(String title) {
        super(title, 
          VisUI.getSkin().get("default", ColorPickerStyle.class));

        // Window description
        setModal(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();

        // Create body
        int row_num = OnCreateBody();

        // Create Restore/OK/Cancel footer buttons
        ButtonBar btn_bar = new ButtonBar();
        btn_bar.setIgnoreSpacing(true);
        btn_bar.setButton(ButtonType.LEFT, 
          new VisTextButton(RESTORE.get()), new ChangeListener() {
              @Override public void changed(ChangeEvent event, Actor actor) {
                  OnRestoreSettings();
              }
        });
        btn_bar.setButton(ButtonType.OK, 
          new VisTextButton(OK.get(), new ChangeListener() {
              @Override public void changed(ChangeEvent event, Actor actor) {
                  close();
              }
        }));
        btn_bar.setButton(ButtonType.CANCEL, 
          new VisTextButton(CANCEL.get(), new ChangeListener() {
              @Override public void changed(ChangeEvent event, Actor actor) {
                  OnRestoreSettings();
                  close();
              }
        }));
        row();
        add(btn_bar.createTable()).pad(3).right()
          .expandX().colspan(row_num);

        // Finalize window
        pack();
        centerWindow();
    }

    //--------------------------------------------------------------------------
    public int OnCreateBody() {
        return 1; /* row-num */
    }

    //--------------------------------------------------------------------------
    public void OnRestoreSettings() {
    }
}
