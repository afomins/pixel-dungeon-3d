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
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import java.util.HashMap;
import com.matalok.pd3d.renderable.RenderableTemplate;
import com.matalok.pd3d.renderable.model.ModelAnim;

//------------------------------------------------------------------------------
public abstract class MTmpl 
  extends RenderableTemplate {
    //**************************************************************************
    // Template
    //**************************************************************************
    public String default_anim;
    public HashMap<String, ModelAnim.Template> anim_templates;

    //--------------------------------------------------------------------------
    public MTmpl(String name, Float alpha, boolean do_runtime_transform) {
        super(name, alpha, do_runtime_transform);
        anim_templates = new HashMap<String, ModelAnim.Template>();
    }

    //----------------------------------------------------------------------
    public ModelAnim.Template SetAnim(String name, int fps, boolean is_looped) {
        if(default_anim == null || name.equals("idle")) {
            default_anim = name;
        }

        ModelAnim.Template anim = 
          new ModelAnim.Template(fps, is_looped);
        anim_templates.put(name, anim);
        return anim;
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

        for(ModelAnim.Template t : anim_templates.values()) {
            t.OnCleanup();
        }
        anim_templates.clear();
        anim_templates = null;
        default_anim = null;
    }
}
