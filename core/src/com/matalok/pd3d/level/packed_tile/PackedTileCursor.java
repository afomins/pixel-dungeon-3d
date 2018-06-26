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

// -----------------------------------------------------------------------------
package com.matalok.pd3d.level.packed_tile;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class PackedTileCursor 
 implements IManaged {
    //***************************************************************************
    // Direction
    //***************************************************************************
    public enum Direction {
        //----------------------------------------------------------------------
        X, Z;

        //----------------------------------------------------------------------
        public Direction GetOpposite() {
            return (this == X) ? Z : X;
        }
    }

    //***************************************************************************
    // TilePackerCursor
    //***************************************************************************
    public char id;
    public Integer origin[];
    public Integer size[];
    public Boolean is_locked[];
    public RendererModel renderer_model;
    public RenderableObject.Bounds bounds;
    public int triangle_num;
    public int sides;
    public int hash;

    //--------------------------------------------------------------------------
    public PackedTileCursor(int id, int x, int y) {
        this.id = (char) ('A' + (char)id);
        origin = new Integer[] {x, y};
        size = new Integer[] {1, 1};
        is_locked = new Boolean[] {false, false};
        bounds = new RenderableObject.Bounds();
    }

    //--------------------------------------------------------------------------
    public int GetOrigin(Direction dir) {
        return origin[dir.ordinal()];
    }

    //--------------------------------------------------------------------------
    public int GetSize(Direction dir) {
        return size[dir.ordinal()];
    }

    //--------------------------------------------------------------------------
    public int GetEnding(Direction dir) {
        return GetOrigin(dir) + GetSize(dir);
    }

    //--------------------------------------------------------------------------
    public boolean IsLocked(Direction dir) {
        return is_locked[dir.ordinal()];
    }

    //--------------------------------------------------------------------------
    public void Lock(Direction dir) {
        is_locked[dir.ordinal()] = true;
    }

    //--------------------------------------------------------------------------
    public void Extend(Direction dir) {
        size[dir.ordinal()]++;
    }

    //--------------------------------------------------------------------------
    public boolean IsHorizontal() {
        // If duplicate packing is disabled then consider all cursors horizontal
        if(!Main.inst.cfg.lvl_pack_duplicates) {
            return true;
        }

        // If duplicate packing is enabled then return true  if cursor is horizontal
        return (GetSize(Direction.X) >= GetSize(Direction.Z));
    }

    //--------------------------------------------------------------------------
    // Hash format: |  width  |  height  |  T  |  F  |  R  |  B  |  L  |
    //              +---------+----------+-----+-----+-----+-----+-----+
    //              |  0      |  8       |  16 |  17 |  18 |  19 |  20 |
    public int GetHash() {
        // Size hash
        int width = GetSize(Direction.X);
        int height = GetSize(Direction.Z);
        Utils.Assert(width < 256 && height < 256, 
          "Failed to get cursor hash, wrong size :: size=%d:%d", width, height);

        // Swap XZ size so that hash describes only horizontally-aligned cursor
        if(!IsHorizontal()) {
            int tmp = height;
            height = width; width = tmp;
        }

        // Build hash
        int top = ((sides & GeomBuilder.CubeSide.TOP.bit) != 0) ? 1 : 0;
        int front = ((sides & GeomBuilder.CubeSide.FRONT.bit) != 0) ? 1 : 0;
        int right = ((sides & GeomBuilder.CubeSide.RIGHT.bit) != 0) ? 1 : 0;
        int back = ((sides & GeomBuilder.CubeSide.BACK.bit) != 0) ? 1 : 0;
        int left = ((sides & GeomBuilder.CubeSide.LEFT.bit) != 0) ? 1 : 0;
        return (
          (width << 0) | (height << 8) |
          (top << 16) | (front << 17) | (right << 18) | (back << 19) | (left << 20));
    }

    //--------------------------------------------------------------------------
    public Model CreateModel(Material material, Vector3 node_size, 
      Vector3 node_offset) {
        // Swap width/height so that model is horizontally-aligned
        float dir_x = GetSize(Direction.X), dir_z = GetSize(Direction.Z);
        if(!IsHorizontal()) {
            float tmp = dir_z;
            dir_z = dir_x; dir_x = tmp;
        }

        // Size of the cube
        float size_x = dir_x * node_size.x, 
              size_z = dir_z * node_size.z,
              size_y = node_size.y;

        // Texture coordinates
        float u = dir_x, v = dir_z, w = size_y;

        // Init cube vertex
        GeomBuilder builder = Main.inst.geom_builder;
        builder.InitVertex8(
          size_x, size_y, size_z,                               // Center (0, 0, 0)
          node_offset.x, node_offset.y, node_offset.z, null);   // Offset from (0, 0, 0)

        // Create cube
        return builder.CreateCube(sides,  
          Usage.Position | Usage.Normal | Usage.TextureCoordinates, 
          material, u, v, w, true, null);
    }

    //--------------------------------------------------------------------------
    public void SetModel(Model model, Vector3 node_size, boolean do_alpha_blending, 
      float alpha) {
        // Create model instance
        renderer_model = new RendererModel(model);

        // Move to origin
        float size_x = GetSize(Direction.X), size_z = GetSize(Direction.Z);
        float x = GetOrigin(Direction.X) + size_x / 2 - node_size.x / 2;
        float z = GetOrigin(Direction.Z) + size_z / 2 - node_size.z / 2;
        renderer_model.inst.transform.translate(x, 0.0f, z);

        // Rotate because model is horizontally-aligned
        if(!IsHorizontal()) {
            renderer_model.inst.transform.rotate(Vector3.Y, 90.0f);
        }

        // Update bounding box of the model 
        bounds.Update(renderer_model.inst.calculateBoundingBox(new BoundingBox()));
        bounds.center_local.x += x;
        bounds.center_local.z += z;

        // Apply transparency
        renderer_model.SetAlphaBlending(do_alpha_blending, alpha);
    }

    //***************************************************************************
    // IManaged
    //***************************************************************************
    @Override public void OnCleanup() {
        origin = null;
        size = null;
        is_locked = null;
        renderer_model = null;
        bounds.OnCleanup();
    }
}
