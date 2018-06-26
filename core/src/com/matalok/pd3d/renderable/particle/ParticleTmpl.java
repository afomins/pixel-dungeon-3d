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
package com.matalok.pd3d.renderable.particle;

//------------------------------------------------------------------------------
import com.matalok.pd3d.desc.DescPfxMutator;
import com.matalok.pd3d.renderable.RenderableTemplate;
import com.matalok.pd3d.renderer.RendererParticle;

//------------------------------------------------------------------------------
public class ParticleTmpl 
  extends RenderableTemplate {
    //**************************************************************************
    // PTmpl
    //**************************************************************************
    public RendererParticle pfx;

    //--------------------------------------------------------------------------
    public ParticleTmpl(String name, String path, DescPfxMutator mutator) {
        super(name, 1.0f, false);

        // Create particle
        pfx = new RendererParticle(
          RendererParticle.manager.Load(path));

        // Mutate particle
        if(mutator != null) {
            pfx.ApplyMutator(mutator);
        }
    }

    //**************************************************************************
    // ModelBase.ITemplate
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.idt();
    }

    //--------------------------------------------------------------------------
    @Override public void UpdateRuntimeTransform() {
    }

    //**********************************************************************
    // IManaged
    //**********************************************************************
    @Override public void OnCleanup() {
        super.OnCleanup();

        pfx.OnCleanup();
        pfx = null;
    }
}
