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
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiButtonChangeBlending
  extends VisTextButton {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static GuiWndAttribBlending blending_wnd = null;

    //**************************************************************************
    // GuiButtonChangeBlending
    //**************************************************************************
    public GuiButtonChangeBlending(final GuiAttribBlending gui_attrib) {
        super("update");

        // Show window
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if(blending_wnd == null) {
                    blending_wnd = new GuiWndAttribBlending();
                };

                final RendererAttrib.ABlending.Cfg cfg = 
                  (RendererAttrib.ABlending.Cfg)gui_attrib.GetCfg();
                blending_wnd.Init(cfg.is_blended, cfg.alpha, cfg.src_func, cfg.dst_func, 
                  new GuiWndAttribBlending.Listener() {
                      @Override public void OnUpdate(boolean is_blended, 
                        float alpha, int src_func, int dst_func) {
                          cfg.is_blended = is_blended;
                          cfg.alpha = alpha;
                          cfg.src_func = src_func;
                          cfg.dst_func = dst_func;
                          setText("blending");
                          gui_attrib.OnUpdate();
                      };
                });
                getStage().addActor(blending_wnd.fadeIn());
            }
        });
    }
}
