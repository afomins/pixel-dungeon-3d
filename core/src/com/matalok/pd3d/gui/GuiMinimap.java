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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.level.LevelMinimap;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass.Vector2i;

//------------------------------------------------------------------------------
public class GuiMinimap 
  extends VisTable {
    //--------------------------------------------------------------------------
    private Cell<VisImage> m_image_cell;
    private float m_rotation;
    private Texture m_tx;

    //**************************************************************************
    // GuiMinimap
    //**************************************************************************
    public GuiMinimap() {
        m_image_cell = add(new VisImage());
        GetImage().setFillParent(true);
    }

    //--------------------------------------------------------------------------
    public void Update(LevelMinimap minimap, float angle) {
        Texture tx = minimap.GetTexture();
        if(tx == null) {
            return;
        }

        VisImage image = GetImage();
        float img_size = image.getHeight();
        float tx_size = tx.getHeight();
        float tx_scale = img_size / tx_size;
        Vector2i center = minimap.GetCenter();

        // Origin is located in center of minimap
        image.setOrigin(center.x * tx_scale, (tx_size - center.y) * tx_scale);

        // Apply offset
        image.setPosition(img_size / 2.0f - image.getOriginX(), 
         img_size / 2.0f - image.getOriginY());

        // Apply rotation
        if(m_rotation != angle) {
            m_rotation = angle;

            image.clearActions();

            float cur_angle = Utils.NormalizeAngle(image.getRotation());
            float diff = Utils.MinimizeRotAngle(angle - cur_angle);
            image.addAction(Actions.rotateBy(diff, Main.inst.cfg.minimap_rotate_speed));
        }

        // Update texture
        if(m_tx != tx) {
            image.setDrawable(tx);
        }
    }

    //--------------------------------------------------------------------------
    public Cell<VisImage> GetImageCell() {
        return m_image_cell;
    }

    //--------------------------------------------------------------------------
    public VisImage GetImage() {
        return m_image_cell.getActor();
    }
}
