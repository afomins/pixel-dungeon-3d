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
package com.matalok.pd3d.renderable.model;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.badlogic.gdx.utils.Disposable;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.model.template.MTmpl;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class ModelStack 
  implements IManaged {
    //**************************************************************************
    // ModelStack
    //**************************************************************************
    private HashMap<String, ModelAnim> m_anims;
    private ModelAnim m_cur_anim;
    private String m_cur_anim_name;
    private String m_name;

    //--------------------------------------------------------------------------
    public ModelStack(RenderableObject.ITemplate t) {
        this(t.GetName());

        // Create model animations from template
        MTmpl tt = (MTmpl)t;
        for(Entry<String, ModelAnim.Template> e : tt.anim_templates.entrySet()) {
            SetAnim(e.getKey(), new ModelAnim(e.getValue()));
        }
    }

    //--------------------------------------------------------------------------
    public ModelStack(String name) {
        m_anims = new HashMap<String, ModelAnim>();
        m_name = name;
    }

    //--------------------------------------------------------------------------
    public void SwitchTemplate(RenderableObject.ITemplate new_template, 
      LinkedList<Disposable> kill_list) {
        MTmpl t = (MTmpl)new_template;
        for(Entry<String, ModelAnim.Template> e : t.anim_templates.entrySet()) {
            String anim_name = e.getKey();
            ModelAnim.Template anim_template = e.getValue();
            ModelAnim anim = m_anims.get(anim_name);
            Utils.Assert(anim != null, 
              "Failed to switch animation template :: template=%s animation=%s",
              m_name, anim_name);
            anim.SwitchTemplate(anim_template, kill_list);
        }
    }

    //--------------------------------------------------------------------------
    private void SetAnim(String name, ModelAnim anim) {
        m_anims.put(name, anim);
    }

    //--------------------------------------------------------------------------
    public int GetAnimNum() {
        return m_anims.size();
    }

    //--------------------------------------------------------------------------
    public RendererModel GetFirstRendererModel() {
        RendererModel inst = null;
        for(ModelAnim anim : m_anims.values()) {
            inst = anim.GetRendererModel();
            break;
        }
        return inst;
    }

    //--------------------------------------------------------------------------
    public boolean HasAnim(String name) {
        return m_anims.containsKey(name);
    }

    //--------------------------------------------------------------------------
    public ModelAnim GetAnim(String name) {
        return m_anims.get(name);
    }

    //--------------------------------------------------------------------------
    public void StartAnim(String name) {
        if(m_cur_anim_name != null && m_cur_anim_name.equals(name)) {
            return;
        }

//        Logger.d("Starting model animation :: batch-name=%s anim-name=%s", m_name, name);
        Utils.Assert(m_anims.containsKey(name), 
          "Failed to start model animation :: batch-name=%s name=%s", m_name, name);

        m_cur_anim = m_anims.get(name);
        m_cur_anim_name = name;
        m_cur_anim.Start();
    }

    //--------------------------------------------------------------------------
    public RendererModel Update() {
        if(m_cur_anim == null) {
            return null;
        }
        return m_cur_anim.Update();
    }

    //--------------------------------------------------------------------------
    public int GetTriangleNum() {
        if(m_cur_anim == null) {
            return 0;
        }
        return m_cur_anim.GetTriangleNum();
    }

    //--------------------------------------------------------------------------
    public String GetAnimName() {
        return m_cur_anim_name;
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        for(ModelAnim m : m_anims.values()) {
            m.OnCleanup();
        }
        m_anims.clear();
        m_anims = null;

        m_cur_anim = null;
        m_cur_anim_name = null;
        m_name = null;
    }
}
