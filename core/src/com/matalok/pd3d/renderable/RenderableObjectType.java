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
package com.matalok.pd3d.renderable;

//-----------------------------------------------------------------------------
import java.util.Collection;
import java.util.HashMap;
import com.matalok.pd3d.renderable.RenderableObject.ITemplate;
import com.matalok.pd3d.renderable.RenderableObject.ITemplateBuilder;
import com.matalok.pd3d.renderable.billboard.Billboard;
import com.matalok.pd3d.renderable.model.Model;
import com.matalok.pd3d.renderable.particle.Particle;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.shared.Logger;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public enum RenderableObjectType 
  implements IManaged {
    //**************************************************************************
    // ENUM
    //**************************************************************************

    // Opaque terrain
    WALL                        (Renderer.Layer.OPAQUE_TERRAIN),
    FLOOR                       (Renderer.Layer.OPAQUE_TERRAIN),
    EMPTY_SPACE                 (Renderer.Layer.OPAQUE_TERRAIN),
    BOOK_SHELF                  (Renderer.Layer.OPAQUE_TERRAIN),

    // Opaque water
    WATER                       (Renderer.Layer.OPAQUE_WATER),

    // Transparent terrain
    EMBERS                      (Renderer.Layer.TRANSPARENT_TERRAIN),
    GRASS_LOW                   (Renderer.Layer.TRANSPARENT_TERRAIN),
    DECO_FLOOR                  (Renderer.Layer.TRANSPARENT_TERRAIN),
    DECO_WALL                   (Renderer.Layer.TRANSPARENT_TERRAIN),
    PEDESTAL                    (Renderer.Layer.TRANSPARENT_TERRAIN),
    ALCHEMY                     (Renderer.Layer.TRANSPARENT_TERRAIN),
    WELL                        (Renderer.Layer.TRANSPARENT_TERRAIN),
    WELL_EMPTY                  (Renderer.Layer.TRANSPARENT_TERRAIN),
    ENTRANCE                    (Renderer.Layer.TRANSPARENT_TERRAIN),
    EXIT                        (Renderer.Layer.TRANSPARENT_TERRAIN),
    EXIT_LOCKED                 (Renderer.Layer.TRANSPARENT_TERRAIN),
    EXIT_UNLOCKED               (Renderer.Layer.TRANSPARENT_TERRAIN),
    PLANT                       (Renderer.Layer.TRANSPARENT_TERRAIN),

    // Traps
    TRAP_TOXIC                  (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_FIRE                   (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_PARALYTIC              (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_INACTIVE               (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_POISON                 (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_ALARM                  (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_LIGHTNING              (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_GRIPPING               (Renderer.Layer.TRANSPARENT_TERRAIN),
    TRAP_SUMMONING              (Renderer.Layer.TRANSPARENT_TERRAIN),

    // Transparent objects
    ITEM                        (Renderer.Layer.TRANSPARENT_ITEM),
    BARRICADE                   (Renderer.Layer.TRANSPARENT_ITEM), 
    CHAR                        (Renderer.Layer.TRANSPARENT_ITEM), 
    GRASS_HIGH                  (Renderer.Layer.TRANSPARENT_ITEM),
    SIGN                        (Renderer.Layer.TRANSPARENT_ITEM),
    STATUE                      (Renderer.Layer.TRANSPARENT_ITEM), 
    DOOR                        (Renderer.Layer.TRANSPARENT_ITEM), 
    DOOR_OPEN                   (Renderer.Layer.TRANSPARENT_ITEM), 
    DOOR_FRAME                  (Renderer.Layer.TRANSPARENT_ITEM), 
    DOOR_LOCKED                 (Renderer.Layer.TRANSPARENT_ITEM), 
    TARGET                      (Renderer.Layer.TRANSPARENT_ITEM),
    SHADOW                      (Renderer.Layer.TRANSPARENT_ITEM),

    // Fx
    FX_RIPPLE                   (Renderer.Layer.TRANSPARENT_ITEM),
    FX_RIPPLE_LEAKAGE          (Renderer.Layer.TRANSPARENT_ITEM),

    // Transparent fog
    FOG                         (Renderer.Layer.TRANSPARENT_FOG),
    MARKER_SELECT               (Renderer.Layer.TRANSPARENT_FOG),
    MARKER_SEARCH               (Renderer.Layer.TRANSPARENT_FOG),
    MARKER_BLOOD                (Renderer.Layer.TRANSPARENT_FOG),

    // Overlay debug
    AXIS                        (Renderer.Layer.OVERLAY_DBG), 
    WIRE_BOX                    (Renderer.Layer.OVERLAY_DBG),

    // Particle fx
    PFX_FIRE                    (Renderer.Layer.PARTICLE),
    PFX_FIRE_SACRIFICIAL        (Renderer.Layer.PARTICLE),
    PFX_GAS_PARALYTIC           (Renderer.Layer.PARTICLE),
    PFX_GAS_TOXIC               (Renderer.Layer.PARTICLE),
    PFX_GAS_CONFUSING           (Renderer.Layer.PARTICLE),
    PFX_WEB                     (Renderer.Layer.PARTICLE),
    PFX_WATER_OF_HEALING        (Renderer.Layer.PARTICLE),
    PFX_WATER_OF_AWARENESS      (Renderer.Layer.PARTICLE),
    PFX_WATER_OF_TRANSMUTATION  (Renderer.Layer.PARTICLE),
    PFX_ALCHEMY                 (Renderer.Layer.PARTICLE),
    PFX_VAPOR                   (Renderer.Layer.PARTICLE),
    PFX_SCALE_N_FADE            (Renderer.Layer.PARTICLE),
    PFX_SPLASH                  (Renderer.Layer.PARTICLE),
    PFX_FOUNTAIN                (Renderer.Layer.PARTICLE),
    PFX_LEVITATION              (Renderer.Layer.PARTICLE),
    PFX_LEAKAGE                 (Renderer.Layer.PARTICLE),

    // Billboards
    BILLBOARD_TEXT              (Renderer.Layer.BILLBOARD),
    BILLBOARD_SPRITE            (Renderer.Layer.BILLBOARD),
    BILLBOARD_EMOTION           (Renderer.Layer.BILLBOARD),
    BILLBOARD_HP                (Renderer.Layer.BILLBOARD);

    //**************************************************************************
    // ModelType
    //**************************************************************************
    private Renderer.Layer m_renderer_type;
    private HashMap<Integer, ITemplate> m_templates;
    private HashMap<Integer, ITemplateBuilder> m_template_builders;

    //--------------------------------------------------------------------------
    private RenderableObjectType(Renderer.Layer renderer_type) {
        m_renderer_type = renderer_type;
        m_templates = new HashMap<Integer, ITemplate>();
        m_template_builders = new HashMap<Integer, ITemplateBuilder>();
    }

    //--------------------------------------------------------------------------
    public boolean IsParticle() {
        return (m_renderer_type == Renderer.Layer.PARTICLE);
    }

    //--------------------------------------------------------------------------
    public void AddTemplateBuilder(ITemplateBuilder builder) {
        AddTemplateBuilder(0, builder);
    }

    //--------------------------------------------------------------------------
    public void AddTemplateBuilder(int id, ITemplateBuilder builder) {
        // Save new template builder
        m_template_builders.put(id, builder);

        // Update existing template that was built from old builder
        ITemplate old_template = m_templates.get(id); 
        if(old_template != null) {
            // New template inherits params from old one
            ITemplate new_template = builder
              .Build().InheritOldTemplate(old_template).Finalize();

            // Update templates
            m_templates.put(id, new_template);
        }
    }

    //--------------------------------------------------------------------------
    public Collection<ITemplate> GetTemplates() {
        return m_templates.values();
    }

    //--------------------------------------------------------------------------
    public RenderableObject Create() {
        return Create(0);
    }

    //--------------------------------------------------------------------------
    public RenderableObject Create(int template_id) {
        String name = toString().toLowerCase();

        // Create template if it is not present yet
        ITemplate template = m_templates.get(template_id);
        if(template == null) {
            if(!m_template_builders.containsKey(template_id)) {
                Logger.e(
                  "Failed to create model, template is missing :: type=%s id=%d", 
                  name, template_id);
                return null;
            }

            // Build, finalize and save template
            Logger.d("Building template :: type=%s id=%d", name, template_id);
            template = m_template_builders.get(template_id)
              .Build().Finalize();
            m_templates.put(template_id, template);
        }

        RenderableObject obj = null;
        switch(m_renderer_type) {
        // Create particle object
        case PARTICLE: {
            obj = new Particle(name, this, template_id, 
              (com.matalok.pd3d.renderable.particle.ParticleTmpl)template, 
              m_renderer_type);
        } break;

        // Create decal object
        case BILLBOARD: {
            obj = new Billboard(name, this, template_id, 
              (com.matalok.pd3d.renderable.billboard.BillboardTmpl)template, 
              m_renderer_type);
        } break;

        // Create model object
        default: {
            obj = new Model(name, this, template_id, 
              (com.matalok.pd3d.renderable.model.template.MTmpl)template, 
              m_renderer_type);
        }
        }
        return obj;
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        if(m_templates != null) {
            for(ITemplate t : m_templates.values()) {
                t.OnCleanup();
            }
            m_templates.clear();
            m_templates = null;
        }

        if(m_template_builders != null) {
            m_template_builders.clear();
            m_template_builders = null;
        }
    }
}
