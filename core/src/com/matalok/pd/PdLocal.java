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
package com.matalok.pd;

//------------------------------------------------------------------------------
import com.matalok.pd3d.Main;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;

//------------------------------------------------------------------------------
public class PdLocal
  extends GameNode {
    //**************************************************************************
    // PdLocal
    //**************************************************************************
    private PdWrapper m_pd;

    //--------------------------------------------------------------------------
    public PdLocal() {
        super("pd-local", 1.0f);

        if(!Main.inst.cfg.server_is_remote) {
            Logger.SetPrefix("S");
            m_pd = new PdWrapper();
            m_pd.RunOnCreate();
            Logger.SetPrefix("C");
        }
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodResume() {
        Logger.NewStep(false);
        Logger.SetPrefix("S");
        super.OnMethodResume();

        if(m_pd != null) {
            m_pd.RunOnResume();
        }
        Logger.SetPrefix("C");
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodPause() {
        Logger.NewStep(false);
        Logger.SetPrefix("S");
        super.OnMethodPause();

        if(m_pd != null) {
            m_pd.RunOnPause();
        }
        Logger.SetPrefix("C");
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        Logger.NewStep(false);
        Logger.SetPrefix("S");
        super.OnMethodCleanup();

        if(m_pd != null) {
            m_pd.RunOnDestroy();
        }
        Logger.SetPrefix("C");
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        Logger.NewStep(false);
        Logger.SetPrefix("S");
        super.OnMethodRender(ctx);

        if(m_pd != null) {
            m_pd.RunOnDrawFrame();
        }
        Logger.SetPrefix("C");
        return true;
    }
}
