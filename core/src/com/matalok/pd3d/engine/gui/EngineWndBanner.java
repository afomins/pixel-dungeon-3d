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
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisImage;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.renderable.RenderableMan;

//------------------------------------------------------------------------------
public class EngineWndBanner 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndInfo
    //**************************************************************************
    private Cell<VisImage> m_image;
    private Enum<?> m_sprite;

    //--------------------------------------------------------------------------
    public EngineWndBanner() {
        super(null, true, true, 1.0f, 1.0f, 
          InputAction.IGNORE,    // OnTouchInArea
          InputAction.IGNORE,    // OnTouchOutArea
          InputAction.IGNORE,    // OnKeyPress
          InputAction.IGNORE,    // OnBack
          1.0f, MapEnum.ItemType.HIDDEN, 0.0f);

        // Non interactive
        clearListeners();
        setTouchable(Touchable.disabled);
    }

    //--------------------------------------------------------------------------
    public void UpdateImage(Enum<?> sprite, Float width, Float height,
      Float delay) {
        RenderableMan rm = Main.inst.renderable_man;
        VisImage img = m_image.getActor();

        // Clear old fading actions
        img.clearActions();

        // Show image
        if(sprite != null) {
            // Update image
            img.setDrawable(
              new TextureRegionDrawable(rm.GetTextureRegion(sprite, null)));
            EngineWnd.SetCellSize(m_image, width, height);
            img.setScaling(Scaling.fillX);

            // Fade-in instantly
            addAction(Actions.alpha(1.0f, Main.inst.cfg.gui_fade_duration));
    
            // Fade-out after delay
            if(delay != null) {
                addAction(Actions.delay(delay, 
                  Actions.alpha(0.0f, Main.inst.cfg.gui_fade_duration)));
            }

            // Force new layout
            img.invalidateHierarchy();

        // Hide image
        } else {
            addAction(Actions.alpha(0.0f, Main.inst.cfg.gui_fade_duration));
        }
        m_sprite = sprite;
    }

    //--------------------------------------------------------------------------
    public Enum<?> GetSprite() {
        return m_sprite;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        RenderableMan m = Main.inst.renderable_man;
        m_image = AddCellImage(null, m.GetTextureRegion(
          (m_sprite != null) ? m_sprite : MapEnum.BannerType.PIXEL_DUNGEON, null), 
          null, null);
        return this;
    }
}
