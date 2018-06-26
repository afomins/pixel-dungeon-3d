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
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribTexture 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribTexture
    //**************************************************************************
    public GuiAttribTexture(RendererAttrib.ATexture.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.TEXTURE, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        Set<String> names = Main.inst.renderer.GetTxCache().GetKeys();
        final RendererAttrib.ATexture.Cfg cfg = (RendererAttrib.ATexture.Cfg)m_cfg;

        if(!names.contains(cfg.name)) {
            cfg.name = "default";
        }

        final GuiSelectBox sbox = add(new GuiSelectBox(names.toArray(new String[]{}))
          .SetName(cfg.name, false)).expandX().fillX().getActor();
        sbox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                cfg.name = sbox.GetName();
                OnUpdate();
            }
        });
    }
}
