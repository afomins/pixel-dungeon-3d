// -----------------------------------------------------------------------------
package com.matalok.pd3d.level.packed_tile;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformUtils;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.renderer.RendererTexture;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.level.packed_tile.PackedTileCursor.Direction;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class PackedTile 
 extends GameNode {
    //***************************************************************************
    // NodeState
    //***************************************************************************
    public enum NodeState {
        //----------------------------------------------------------------------
        IDLE,           // Not part of packed tile 
        PACK_PENDING,   // Waiting for packing
        PACKED          // Packed
    }

    //***************************************************************************
    // Node
    //***************************************************************************
    public class Node {
        //----------------------------------------------------------------------
        public PackedTileCursor cursor;
        public NodeState state;

        //----------------------------------------------------------------------
        public Node() {
            state = NodeState.IDLE;
            cursor = null;
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static final int idx_none = -1;

    //**************************************************************************
    // PackedTile
    //**************************************************************************
    protected Node m_nodes[];
    protected LinkedList<PackedTileCursor> m_cursors;
    protected int m_add_num;
    protected int m_map_width;
    protected int m_map_height;
    protected Vector3 m_node_size;
    protected Vector3 m_node_offset;

    protected DescSprite m_sprite;
    protected int m_tile_idx;

    protected Material m_material;

    protected boolean m_needs_repack;

    protected HashMap<Integer, Model> m_model_cache;
    protected int m_model_cache_hit;
    protected int m_model_cache_miss;

    protected long m_dbg_color;
    protected Renderer.Layer m_renderer_layer;
    protected boolean m_do_alpha_blending;

    //--------------------------------------------------------------------------
    public PackedTile(String name, Renderer.Layer renderer_layer, DescSprite sprite, 
      int map_width, int map_height, Vector3 node_size, Vector3 node_offset, 
      Float alpha, long dbg_color) {
        super(name, (alpha == null) ? 1.0f : alpha);

        m_sprite = sprite;

        m_nodes = new Node[map_width * map_height];
        for(int i = 0; i < m_nodes.length; i++) {
            m_nodes[i] = new Node();
        }
        m_map_width = map_width;
        m_map_height = map_height;

        m_cursors = new LinkedList<PackedTileCursor>();

        m_node_size = node_size;
        m_node_offset = (node_offset == null) ? new Vector3() : node_offset;

        m_model_cache = new HashMap<Integer, Model>();
        m_dbg_color = dbg_color;
        m_renderer_layer = renderer_layer;
        m_do_alpha_blending = (alpha != null);
    }

    //--------------------------------------------------------------------------
    public void SetSprite(DescSprite sprite) {
        m_sprite = sprite;
        m_needs_repack = true;

        for(Model m : m_model_cache.values()) {
            m.dispose();
        }
        m_model_cache.clear();
    }

    //--------------------------------------------------------------------------
    public void AddNode(int idx) {
        Node node = m_nodes[idx];
        if(node.state == NodeState.PACK_PENDING) {
            return; // Already added
        }

        node.state = NodeState.PACK_PENDING;
        node.cursor = null;

        m_needs_repack = true;
        m_add_num++;
    }

    //--------------------------------------------------------------------------
    public void RemoveNode(int idx) {
        Node node = m_nodes[idx];
        if(node.state == NodeState.IDLE) {
            return;  // Already removed
        }

        node.state = NodeState.IDLE;
        node.cursor = null;

        m_add_num--;
        m_needs_repack = true;
    }

    //--------------------------------------------------------------------------
    public void ToggleNode(int idx, boolean is_added, boolean do_repack) {
        if(is_added) {
            AddNode(idx);
        } else {
            RemoveNode(idx);
        }
        m_needs_repack = do_repack;
    }

    //--------------------------------------------------------------------------
    public void ResetNodes(NodeState status) {
        for(int i = 0; i < m_nodes.length; i++) {
            Node node = m_nodes[i];
            node.state = status;
            node.cursor = null;
        }

        m_needs_repack = true;
        m_add_num = (status == NodeState.IDLE) ? 0 : m_nodes.length;
    }

    //--------------------------------------------------------------------------
    public boolean IsPacked(int idx) {
        return (m_nodes[idx].state == NodeState.PACKED);
    }

    //--------------------------------------------------------------------------
    private int GetMapSize(Direction dir) {
        return (dir == Direction.X) ? m_map_width : m_map_height;
    }

    //--------------------------------------------------------------------------
    private Node GetNode(int x, int y) {
        int idx = m_map_width * y + x;
        return m_nodes[idx];
    }

    //--------------------------------------------------------------------------
    private int GetNextPendingNode(int start_from) {
        for(int i = start_from; i < m_nodes.length; i++) {
            Node node = m_nodes[i]; 
            if(node.state == NodeState.PACK_PENDING) {
                return i;
            }
        }
        return idx_none;
    }

    //--------------------------------------------------------------------------
    private boolean TestExtend(PackedTileCursor cursor, Direction dir) {
        Direction dir_opp = dir.GetOpposite();
        int origin_opp = cursor.GetOrigin(dir_opp);
        int size_opp = cursor.GetSize(dir_opp);

        // Limit maxim size of packed tile. This is needed to prevent 
        // prevent situation when packed tile is too scretched and 
        // per-vertex lighting does not work correctly
        int max_size = Main.inst.cfg.lvl_pack_max_size;
        if(max_size > 0 && cursor.GetSize(dir) >= max_size) {
            return false;
        }

        // Test if cursor will exceed map boundaries after extension
        int extension_candidate = cursor.GetEnding(dir);
        if(extension_candidate >= GetMapSize(dir)) {
            return false;
        }

        // Test if all new nodes can be selected after extension
        for(int i = origin_opp; i < origin_opp + size_opp; i++) {
            int x, y;
            if(dir == Direction.X) {
                x = extension_candidate; y = i;
            } else {
                y = extension_candidate; x = i;
            }

            Node node = GetNode(x, y);
            if(node.state != NodeState.PACK_PENDING) {
                return false;
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    private PackedTileCursor CreateCursor(int origin_idx) {
        // Create cursor
        int x = origin_idx % m_map_width;
        int y = origin_idx / m_map_width;
        PackedTileCursor cursor = new PackedTileCursor(m_cursors.size(), x, y);

        // Extend cursor until it is locked
        Direction dir = Direction.X;
        for(;;) {
            Direction dir_opp = dir.GetOpposite();
            boolean is_locked = cursor.IsLocked(dir);
            boolean is_locked_opp = cursor.IsLocked(dir_opp);

            // Stop extending when both directions are locked
            if(is_locked && is_locked_opp) {
                break;
            }

            // Swap direction if possible
            if(!is_locked_opp) {
                dir = dir_opp;
            }

            // Lock cursor if can not extend further
            if(!TestExtend(cursor, dir)) {
                cursor.Lock(dir);
                continue;
            }

            // Extend cursor
            cursor.Extend(dir);
        }
        return cursor;
    }

    //--------------------------------------------------------------------------
    private void SetCursor(PackedTileCursor c) {
        for(int z = c.GetOrigin(Direction.Z); z < c.GetEnding(Direction.Z); z++) {
            for(int x = c.GetOrigin(Direction.X); x < c.GetEnding(Direction.X); x++) {
                Node node = GetNode(x, z);
                Utils.Assert(node.state == NodeState.PACK_PENDING, 
                  "Failed to select map, wrong node status :: pos=%s:%s status=%s", 
                  x, z, node.state);
                node.state = NodeState.PACKED;
                node.cursor = c;
            }
        }
    }

    //--------------------------------------------------------------------------
    private Model GetModel(PackedTileCursor cursor) {
        // Return model from cache if exists
        int hash = cursor.GetHash();
        if(m_model_cache.containsKey(hash)) {
            m_model_cache_hit++;
            return m_model_cache.get(hash);
        }

        m_model_cache_miss++;
        Logger.d("Creating cursor model :: tile=%s hash=%d size=%d:%d flags=%s", 
          SgGetNameId(), hash, cursor.GetSize(Direction.X), cursor.GetSize(Direction.Z),
          Integer.toBinaryString(cursor.sides));

        // Create debug material
        Material material = m_material;
        if(Main.inst.cfg.lvl_pack_color && m_dbg_color != 0) {
            material = new Material(m_material);
            material.set(ColorAttribute.createDiffuse(
              new Color((int)MathUtils.random(m_dbg_color))));
        }

        // Create and cache new model
        Model model = cursor.CreateModel(material, m_node_size, m_node_offset);
        m_model_cache.put(hash, model);
        return model;
    }

    //--------------------------------------------------------------------------
    private void Pack() {
        // Clear old cursors before packing
        m_cursors.clear();
        for(Node node : m_nodes) {
            if(node.state == NodeState.PACKED) {
                node.state = NodeState.PACK_PENDING; 
            }
            node.cursor = null;
        }

        // Walk all prepared nodes and create cursors
        int origin_idx = 0;
        for(;;) {
            // Find next pending node or exit
            origin_idx = GetNextPendingNode(origin_idx);
            if(origin_idx == idx_none) {
                break;
            }

            // Create cursor
            PackedTileCursor cursor = CreateCursor(origin_idx);

            // Save cursor
            m_cursors.add(cursor);

            // Apply cursor
            SetCursor(cursor);
        }

        // Update model of the cursor
        UpdateCursorModel();
    }

    //--------------------------------------------------------------------------
    public void UpdateSprite() {
        // Ignore if texture did not change since previous update
        String tx_cache_key = SgGetName();
        Renderer.TxCache tx_cache = Main.inst.renderer.GetTxCache();
        RendererTexture texture = tx_cache.Get(tx_cache_key);
        if(texture != null && texture.name.equals(m_sprite.texture)) {
            return;
        }

        // Read tilemap
        Pixmap pm_orig = new Pixmap(
          PlatformUtils.OpenInternalFile(m_sprite.texture, true));

        // Get rect of idle animation
        DescRect rect = m_sprite.GetFirstAnimRect("idle");
        if(rect == null) {
            Logger.e("Failed to update sprite of packed tile :: packed-sprite=%s",
              SgGetNameId());
            return;
        }

        // Extract sprite from texture tilemap  
        Pixmap pm = RendererTexture.CutPixmap(
          pm_orig, rect.x, rect.y, rect.width, rect.height);

        // Remove old texture from cache
        if(texture != null) {
            tx_cache.Delete(tx_cache_key, true);
        }

        // Put new texture to cache
        texture = tx_cache.Put(tx_cache_key, 
          new RendererTexture(m_sprite.texture, new Texture(pm), true));

        // Create material
        m_material = RendererModel.CreateMaterial(
          null, texture.tx, true, m_do_alpha_blending);

        // Cleanup
        pm.dispose();
        pm_orig.dispose();
    }

    //--------------------------------------------------------------------------
    public void UpdateCursorModel() {
        // Update model of the cursor
        m_model_cache_hit = m_model_cache_miss = 0;
        for(PackedTileCursor c : m_cursors) {
            // Sides of the model
            c.sides = GeomBuilder.CubeSide.TOP.bit;
            c.triangle_num = 2;
            if(m_node_size.y > 0.0f) {
                c.sides |= GeomBuilder.CubeSide.FRONT.bit;
                c.sides |= GeomBuilder.CubeSide.RIGHT.bit;
                c.sides |= GeomBuilder.CubeSide.BACK.bit;
                c.sides |= GeomBuilder.CubeSide.LEFT.bit;
                c.triangle_num = 10;
            }
            c.SetModel(GetModel(c), m_node_size, m_do_alpha_blending, GetLocalAlpha());
        }

        int cache_get_total = (m_model_cache_hit + m_model_cache_miss);
        float cache_hit_rate = ((float)m_model_cache_hit / cache_get_total) * 100;
        Logger.d("Packing tiles :: name=%s cursor-num=%d cache-size=%d cache-get-num=%d cache-hit-num=%d(%.0f%%)", 
          SgGetNameId(), m_cursors.size(), m_model_cache.size(), cache_get_total, m_model_cache_hit, cache_hit_rate);
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Repack tiles
        if(m_needs_repack) {
            m_needs_repack = false;
            UpdateSprite();
            Pack();
        }

        // Render all cursors
        for(PackedTileCursor c : m_cursors) {
            if(c.renderer_model == null) {
                break;
            }

            boolean is_visible = ctx.GetCamera(m_renderer_layer)
              .TestFrustumCulling(c.bounds.center_local, c.bounds.radius);
            ctx.AddLayerObject(c.renderer_model, m_renderer_layer, 
              is_visible, c.triangle_num);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodJson(JsonCtx ctx) {
        super.OnMethodJson(ctx);

        if(ctx.pre_children && 
           ctx.targets.contains(JsonTarget.COMMON)) {
            RendererTexture tx = Main.inst.renderer.GetTxCache().Get(SgGetName());
            String texture_name = (tx == null) ? "none" : tx.name;

            ctx.Write("\"map-width\" : %d, \"map-height\" : %d, \"texture\" : \"%s\",", 
              m_map_width, m_map_height, texture_name);
            ctx.Write("\"node-num\" : %d, \"cursor-num\" : %d, \"optimization\" = %.1f,", 
              m_add_num, m_cursors.size(), 
              (((float)m_add_num) / m_cursors.size()));
            for(int y = 0 ; y < m_map_height; y++) {
                String line = "";
                for(int x = 0 ; x < m_map_width; x++) {
                    Node node = GetNode(x, y);
                    char c = (node.cursor != null) ? node.cursor.id : '.';
                    line += c;
                }

                if(y == 0) {
                    ctx.Write("\"map\" : [ \"%s\",", line);
                } else if (y == m_map_height - 1) {
                    ctx.Write("          \"%s\",]", line);
                } else {
                    ctx.Write("          \"%s\",", line);
                }
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        for(int i = 0; i < m_nodes.length; i++) {
            m_nodes[i].cursor = null;
            m_nodes[i] = null;
        }
        m_nodes = null;

        for(Model m : m_model_cache.values()) {
            m.dispose();
        }
        m_model_cache.clear();
        m_model_cache = null;

        for(PackedTileCursor c : m_cursors) {
            c.OnCleanup();
        }
        m_cursors.clear();
        m_cursors = null;
        return true;
    }
}
