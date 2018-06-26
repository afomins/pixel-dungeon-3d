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

//------------------------------------------------------------------------------
import java.util.LinkedList;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.IManaged;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.level.packed_tile.PackedTile;

//------------------------------------------------------------------------------
public class RenderableObject 
  extends GameNode {
    // *************************************************************************
    // ITemplate
    // *************************************************************************
    public interface ITemplate 
      extends IManaged {
        //----------------------------------------------------------------------
        public String GetName();
        public Float GetAlpha();
        public Matrix4 GetInitialTransform();
        public Matrix4 GetRuntimeTransform();
        public void UpdateInitialTransform();
        public void UpdateRuntimeTransform();
        public int GetSize();
        public boolean IsRegistered(RenderableObject obj);
        public void Register(RenderableObject obj);
        public void Unregister(RenderableObject obj);
        public ITemplate InheritOldTemplate(ITemplate old_template);
        public ITemplate Finalize();
    }

    //**************************************************************************
    // ITemplateBuilder
    //**************************************************************************
    public interface ITemplateBuilder {
        //----------------------------------------------------------------------
        public ITemplate Build();
    }

    // *************************************************************************
    // Bounds
    // *************************************************************************
    public static class Bounds 
      implements IManaged {
        //----------------------------------------------------------------------
        public BoundingBox box;
        public Vector3 dimensions;
        public Vector3 center_local;
        public Vector3 center_global;
        public float radius;

        //----------------------------------------------------------------------
        public Bounds() {
            dimensions = new Vector3();
            center_local = new Vector3();
            center_global = new Vector3();
        }

        //----------------------------------------------------------------------
        public Bounds(BoundingBox box) {
            this();
            Update(box);
        }

        //----------------------------------------------------------------------
        public void Update(BoundingBox box) {
            if(box != null) {
                this.box = box;
            }

            this.box.getCenter(center_local);
            this.box.getDimensions(dimensions);
            radius = dimensions.len() / 2;
        }

        //----------------------------------------------------------------------
        public void UpdateCenterGlobal(Vector3 global_pos) {
            center_global.set(global_pos).add(center_local);
        }

        //**************************************************************************
        // IManaged
        //**************************************************************************
        @Override public void OnCleanup() {
            dimensions = null;
            center_local = null;
            center_global = null;
        }
    }

    //**************************************************************************
    // RenderableObject
    //**************************************************************************
    protected RenderableObjectType m_obj_type;
    protected int m_obj_id;
    protected ITemplate m_template;
    protected Renderer.Layer m_renderer_layer;
    protected Bounds m_bounds;
    protected boolean m_is_render_allowed;

    protected PackedTile m_packed_tile;
    protected int m_packed_tile_idx;
    protected boolean m_packed_tile_exclude_fading;

    //--------------------------------------------------------------------------
    public RenderableObject(String name, RenderableObjectType obj_type, int obj_id, 
      ITemplate template, Renderer.Layer renderer_layer) {
        super(name, (template.GetAlpha() == null) ? 1.0f : template.GetAlpha());

        // Type and stuff
        m_obj_type = obj_type;
        m_obj_id = obj_id;
        m_template = template;
        m_renderer_layer = renderer_layer;

        // Set initial transform of the object
        SetInitTransform(template.GetInitialTransform());

        // Rendering is allowed by default 
        SetRenderAllowed(true);

        // Register self in template
        m_template.Register(this);

        // By default object is not linked to packed tile
        m_packed_tile = null;
        m_packed_tile_idx = -1;
    }

    //--------------------------------------------------------------------------
    public RenderableObjectType GetObjectType() {
        return m_obj_type;
    }

    //--------------------------------------------------------------------------
    public int GetObjectId() {
        return m_obj_id;
    }

    //--------------------------------------------------------------------------
    public ITemplate GetTemplate() {
        return m_template;
    }

    //--------------------------------------------------------------------------
    public Renderer.Layer GetRendererLayer() {
        return m_renderer_layer;
    }

    //--------------------------------------------------------------------------
    public Bounds GetBounds() {
        return m_bounds;
    }

    //--------------------------------------------------------------------------
    public void SwitchTemplate(ITemplate new_template, LinkedList<Disposable> kill_list) {
        Utils.Assert(new_template.IsRegistered(this),
          "Object is not registered in new template :: template=%s obj=%s", 
          new_template.GetName(), SgGetNameId());
        m_template = new_template;
    }

    //--------------------------------------------------------------------------
    public boolean TestFrustumCulling(Camera camera) {
        // Frustum culling passes if no bounds
        return (m_bounds == null || 
          camera.TestFrustumCulling(m_bounds.center_global, m_bounds.radius));
    }

    //--------------------------------------------------------------------------
    public boolean IsRenderAllowed() {
        return !m_is_render_allowed ? false : 
               (m_packed_tile == null) ? true : 
               !m_packed_tile.IsPacked(m_packed_tile_idx);
    }

    //--------------------------------------------------------------------------
    public void SetRenderAllowed(boolean is_allowed) {
        m_is_render_allowed = is_allowed;
    }

    //--------------------------------------------------------------------------
    public void SetPackedTile(PackedTile packed_tile, int idx, 
      boolean exclude_fading_from_pack) {
        m_packed_tile = packed_tile;
        m_packed_tile_idx = idx;
        m_packed_tile_exclude_fading = exclude_fading_from_pack;
    }

    //--------------------------------------------------------------------------
    public PackedTile GetPackedTile() {
        return m_packed_tile;
    }

    //--------------------------------------------------------------------------
    public int GetPackedTileIdx() {
        return m_packed_tile_idx;
    }

    //--------------------------------------------------------------------------
    public boolean HasPackedTile() {
        return (m_packed_tile != null);
    }

    //**************************************************************************
    // GameNode
    //**************************************************************************
    @Override public boolean IsVisibleByCamera(Camera camera) {
        return (GetGlobalAlpha() > 0.0f && TestFrustumCulling(camera));
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        // Unregister self from template
        m_template.Unregister(this);
        m_template = null;

        // Clear bounds
        if(m_bounds != null) {
            m_bounds.OnCleanup();
            m_bounds = null;
        }
        return true;
    }
}
