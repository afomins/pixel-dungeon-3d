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
package com.matalok.pd3d;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.matalok.pd3d.node.GameNode;

// -----------------------------------------------------------------------------
public class GeomBuilder 
  extends GameNode {
    // *************************************************************************
    // CubeSide
    // *************************************************************************
    //    ^ y (w)
    //     \
    //      \                         x (u)
    //       +---------------------------->
    //       | 
    //       |  0---------------3
    //       |  |\              |\ 
    //       |  | \             | \
    //       |  |  4............|..7
    //       |  |  .            |  |
    //       |  |  .   (top)    |  |
    //       |  |  .            |  |   (right)
    //       |  1---------------2  |
    //       |   \ .             \ |
    //       |    \.              \|
    //       |     5---------------6
    //       |          (front)
    // z (v) v
    //
    public enum CubeSide {
        //----------------------------------------------------------------------
        TOP(    0, new int[] {0, 1, 2, 3}, new Vector3(Vector3.Y)),
        FRONT(  1, new int[] {1, 5, 6, 2}, new Vector3(Vector3.Z)),
        RIGHT(  2, new int[] {2, 6, 7, 3}, new Vector3(Vector3.X)),
        BOTTOM( 3, new int[] {5, 4, 7, 6}, new Vector3(Vector3.Y).scl(-1.0f)),
        BACK(   4, new int[] {3, 7, 4, 0}, new Vector3(Vector3.Z).scl(-1.0f)),
        LEFT(   5, new int[] {0, 4, 5, 1}, new Vector3(Vector3.X).scl(-1.0f));

        //----------------------------------------------------------------------
        public int bit;
        public int[] idx;
        public Vector3 normal;

        //----------------------------------------------------------------------
        private CubeSide(int bit, int[] idx, Vector3 normal) {
            this.bit = (1 << bit);
            this.idx = idx;
            this.normal = normal;
        }
    }
    public static final CubeSide[] sides_array = new CubeSide[] {
      CubeSide.TOP, CubeSide.FRONT, CubeSide.RIGHT, 
      CubeSide.BOTTOM, CubeSide.BACK, CubeSide.LEFT};
    public static ModelBuilder builder = new ModelBuilder();

    //**************************************************************************
    // GeomBuilder
    //**************************************************************************
    private ModelBuilder m_model_builder;
    private MeshPartBuilder m_part_builder;
    private Vector3 m_normal;
    private Vector3 m_vertex8[];

    private VertexInfo m_vertex_info[];
    private Vector2 m_tmp_uv[];
    private Vector3 m_tmp_side_vertex[];
    private int m_triangle_num;
    private boolean m_invert_vertex_order;

    //--------------------------------------------------------------------------
    public GeomBuilder() {
        super("geomerty-builder", 1.0f);

        m_model_builder = null;
        m_normal = new Vector3();
        m_vertex8 = new Vector3[] { new Vector3(), new Vector3(), new Vector3(), new Vector3(),
                                    new Vector3(), new Vector3(), new Vector3(), new Vector3() };
        m_vertex_info = new VertexInfo[] { new VertexInfo(), new VertexInfo(), 
                                            new VertexInfo(), new VertexInfo() };
        m_tmp_uv = new Vector2[] { new Vector2(), new Vector2(), new Vector2(), new Vector2() };
        m_tmp_side_vertex = new Vector3[4];
    }

    //--------------------------------------------------------------------------
    public void InvertVertexOrder(boolean value) {
        m_invert_vertex_order = value;
    }

    //--------------------------------------------------------------------------
    private MeshPartBuilder CreatePartBuilder(String type, long attributes, 
      Material material) {
        return m_model_builder.part(
          type, GL20.GL_TRIANGLES, attributes, material);
    }

    //--------------------------------------------------------------------------
    public void BeginModel() {
        m_model_builder = GeomBuilder.builder;
        m_model_builder.begin();
        m_triangle_num = 0;
    }

    //--------------------------------------------------------------------------
    public Model FinalizeModel() {
        Model model = m_model_builder.end();
        m_model_builder = null;
        return model;
    }

    //--------------------------------------------------------------------------
    public int GetTriangleNum() {
        return m_triangle_num;
    }

    //--------------------------------------------------------------------------
    public void InitVertex8(float size_x, float size_y, float size_z, 
      float off_x, float off_y, float off_z, Matrix4 transform) {
        size_x /= 2; size_y /= 2; size_z /= 2; 

        // Top plane
        m_vertex8[0].set(-size_x + off_x, +size_y + off_y, -size_z + off_z);
        m_vertex8[1].set(-size_x + off_x, +size_y + off_y, +size_z + off_z);
        m_vertex8[2].set(+size_x + off_x, +size_y + off_y, +size_z + off_z);
        m_vertex8[3].set(+size_x + off_x, +size_y + off_y, -size_z + off_z);

        // Bottom plane
        m_vertex8[4].set(-size_x + off_x, -size_y + off_y, -size_z + off_z);
        m_vertex8[5].set(-size_x + off_x, -size_y + off_y, +size_z + off_z);
        m_vertex8[6].set(+size_x + off_x, -size_y + off_y, +size_z + off_z);
        m_vertex8[7].set(+size_x + off_x, -size_y + off_y, -size_z + off_z);

        // Apply transformation
        if(transform != null) {
            for(int i = 0; i < 8; i++) {
                m_vertex8[i].mul(transform);
            }
        }
    }

    //--------------------------------------------------------------------------
    public void InitVertex4(float size_x, float size_z, 
      float off_x, float off_y, float off_z, Matrix4 transform) {
        size_x /= 2; size_z /= 2; 

        // Top plane
        m_vertex8[0].set(-size_x + off_x, off_y, -size_z + off_z);
        m_vertex8[1].set(-size_x + off_x, off_y, +size_z + off_z);
        m_vertex8[2].set(+size_x + off_x, off_y, +size_z + off_z);
        m_vertex8[3].set(+size_x + off_x, off_y, -size_z + off_z);

        // Apply transformation
        if(transform != null) {
            for(int i = 0; i < 4; i++) {
                m_vertex8[i].mul(transform);
            }
        }
    }

    //--------------------------------------------------------------------------
    private Vector2[] InitUV(float x, float y, float width, float height) {
        m_tmp_uv[0].set(x,         y);
        m_tmp_uv[1].set(x,         y + height);
        m_tmp_uv[2].set(x + width, y + height);
        m_tmp_uv[3].set(x + width, y);
        return m_tmp_uv;
    }

    //--------------------------------------------------------------------------
    private int CreateRect(MeshPartBuilder builder, Vector3[] vertex_array, 
      Vector2[] uv_array, Vector3 normal) {
        // Fill vertex info
        for(int i = 0; i < m_vertex_info.length; i++) {
            VertexInfo vinfo = m_vertex_info[i];
            vinfo.hasColor = vinfo.hasNormal = vinfo.hasPosition = 
              vinfo.hasUV = false;

            if(vertex_array != null) {
                vinfo.setPos(vertex_array[i]);
            }
            if(uv_array != null) {
                vinfo.setUV(uv_array[i]);
            }
            if(normal != null) {
                vinfo.setNor(normal);
            }
        }

        // Build plane
        builder.rect(m_vertex_info[0], m_vertex_info[1], 
          m_vertex_info[2], m_vertex_info[3]);
        return 2;
    }

    //--------------------------------------------------------------------------
    private void CreateCubeSide(CubeSide side, long attributes, 
      float uu, float vv, float ww, Matrix4 transform) {
        CreateCubeSide(side, attributes, 0.0f, 0.0f, 0.0f, uu, vv, ww, transform);
    }

    //--------------------------------------------------------------------------
    private void CreateCubeSide(CubeSide side, long attributes,  
      float u, float v, float w, float uu, float vv, float ww, Matrix4 transform) {
        // Prepare vertex array
        if(!m_invert_vertex_order) {
            m_tmp_side_vertex[0] = m_vertex8[side.idx[0]];
            m_tmp_side_vertex[1] = m_vertex8[side.idx[1]];
            m_tmp_side_vertex[2] = m_vertex8[side.idx[2]];
            m_tmp_side_vertex[3] = m_vertex8[side.idx[3]];
        } else {
            m_tmp_side_vertex[0] = m_vertex8[side.idx[0]];
            m_tmp_side_vertex[1] = m_vertex8[side.idx[3]];
            m_tmp_side_vertex[2] = m_vertex8[side.idx[2]];
            m_tmp_side_vertex[3] = m_vertex8[side.idx[1]];
        }

        // Prepare UV array
        Vector2[] uv_array = null;
        if((attributes & Usage.TextureCoordinates) != 0) { 
            if(side == CubeSide.TOP || side == CubeSide.BOTTOM) {
                uv_array = InitUV(u, v, uu, vv); 

            } else if(side == CubeSide.FRONT || side == CubeSide.BACK) {
                uv_array = InitUV(u, w, uu, ww);

            } else if(side == CubeSide.RIGHT || side == CubeSide.LEFT) {
                uv_array = InitUV(v, w, vv, ww);
            }
        }

        // Prepare normal
        Vector3 normal = null;
        if((attributes & Usage.Normal) != 0) {
            normal = m_normal.set(side.normal);
            if(transform != null) {
                normal.rot(transform);
            }
        }

        // Build plane
        m_triangle_num += CreateRect(
          m_part_builder, m_tmp_side_vertex, uv_array, normal);
    }

    //--------------------------------------------------------------------------
    public Model CreateCube(int sides, long attributes, 
      Material material, boolean do_finalize, Matrix4 transform) {
        return CreateCube(sides, attributes, material, 
          0.0f, 0.0f, 0.0f, do_finalize, transform);
    }

    //--------------------------------------------------------------------------
    public Model CreateCube(int sides, long attributes, Material material, float u, 
      float v, float w, boolean do_finalize, Matrix4 transform) {
        // Begin model once
        if(m_model_builder == null) {
            BeginModel();
        }

        // Create new part
        m_part_builder = CreatePartBuilder("rect", attributes, material);

        // Create cube sides
        for(int i = 0; i < sides_array.length; i++) {
            CubeSide side = sides_array[i];
            if((side.bit & sides) == 0) {
                continue;
            }
            CreateCubeSide(side, attributes, u, v, w, transform);
        }

        // Return model if finalization was requested
        return (do_finalize) ? FinalizeModel() : null;
    }

    //--------------------------------------------------------------------------
    public Model CreatePlane(long attributes, Material material, float u, float v, 
      float uu, float vv, boolean do_finalize, Matrix4 transform) {
        // Begin model once
        if(m_model_builder == null) {
            BeginModel();
        }

        // Create new part
        m_part_builder = CreatePartBuilder("rect", attributes, material);

        // Create TOP side of the cube
        CreateCubeSide(CubeSide.TOP, attributes, 
          u, v, 0.0f, uu, vv, 0.0f, transform);

        // Return model if finalize was request
        return (do_finalize) ? FinalizeModel() : null;
    }
}
