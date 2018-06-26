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

//-----------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.renderer.Renderer;

//------------------------------------------------------------------------------
public class GuiCamera
  extends Camera {
    //**************************************************************************
    // LevelCamera
    //**************************************************************************
    public GuiCamera() {
        super();
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Register self as level camera
        ctx.SetCamera(Renderer.CameraType.GUI, this);

        // Default static camera without target
        m_gdx_camera.up.set(new Vector3(0.0f, 0.0f, -1.0f));
        m_gdx_camera.position.set(0.0f, 2.0f, 0.0f);
        m_gdx_camera.lookAt(0.0f, 0.0f, 0.0f);

        // Update camera matrix
        m_gdx_camera.update();
        return true;
    }
}
