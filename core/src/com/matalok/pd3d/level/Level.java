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
package com.matalok.pd3d.level;

//------------------------------------------------------------------------------
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Camera;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.Tweener;
import com.matalok.pd3d.desc.Desc;
import com.matalok.pd3d.desc.DescChar;
import com.matalok.pd3d.desc.DescEvent;
import com.matalok.pd3d.desc.DescStringInst;
import com.matalok.pd3d.desc.DescHeap;
import com.matalok.pd3d.desc.DescPfxMutator;
import com.matalok.pd3d.desc.DescSceneGame;
import com.matalok.pd3d.desc.DescSnapshot;
import com.matalok.pd3d.desc.DescSpriteInst;
import com.matalok.pd3d.level.object.*;
import com.matalok.pd3d.level.packed_tile.*;
import com.matalok.pd3d.map.Map;
import com.matalok.pd3d.map.MapCell;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderable.RenderableMan;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderable.particle.Particle;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererParticle;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgNode;

// -----------------------------------------------------------------------------
public class Level 
  extends GameNode {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static MapEnum.TerrainType dbg_detect_terrain[] = 
      new MapEnum.TerrainType[] {MapEnum.TerrainType.ALCHEMY};
    public static boolean dbg_detect_terrain_done = false;

    // *************************************************************************
    // Level
    // *************************************************************************

    // Root of cell subtree
    private GameNode m_cells;

    // Root of trash subtree
    private LevelTrashBin m_trash_bin;

    // Terrain map
    private Map m_terrain_map;
    private float m_terrain_map_size2;
    private LevelObjectTerrain.UpdateCtx m_terrain_ctx[];
    private int m_map_width, m_map_height;

    // Previous map
    private MapCell m_map_prev[];
    private int m_map_csum;

    // Cache
    private boolean m_update_cache;
    private UtilsClass.Callback m_update_cb;
    private LevelObjectCache m_cache[];
    private LevelObjectCache m_cache_cells;
    private LevelObjectCache m_cache_terrains;
    private LevelObjectCache m_cache_chars;
    private LevelObjectCache m_cache_items;
    private LevelObjectCache m_cache_plants;

    // Packed tiles
    private GameNode m_packed_tiles;
    private PackedTile m_packed_tile_wall;
    private PackedTile m_packed_tile_floor;
    private PackedTile m_packed_tile_empty_space;
    private PackedTile m_packed_tile_water;
    private PackedTile m_packed_tile_grass;

    // Misc
    private LevelEventHandler m_evt_handler;
    private LevelSkybox m_skybox;
    private LevelFog m_fog;
    private LevelMinimap m_minimap;
    private Camera m_camera;
    private LevelObjectChar m_hero;
    private LevelTarget m_target;
    private int m_dungeon_depth;

    // Update packed tiles when texture changes 
    private boolean m_update_packed_tile_sprite;

    // -------------------------------------------------------------------------
    public Level() {
        super("level", 1.0f);

        // Object cache for fast lookup
        m_cache = new LevelObjectCache[] {
            m_cache_cells    = new LevelObjectCache("cell"),
            m_cache_terrains = new LevelObjectCache("terrain"),
            m_cache_chars    = new LevelObjectCache("char"),
            m_cache_items    = new LevelObjectCache("item"),
            m_cache_plants   = new LevelObjectCache("plant"),
        };

        // Target manager
        m_target = new LevelTarget();

        // Event handler
        m_evt_handler = new LevelEventHandler();

        SetDbgRender(
          Main.inst.cfg.dbg_render_axis);
    }

    // -------------------------------------------------------------------------
    public void HandleEvent(DescEvent event) {
        try {
            m_evt_handler.Run(this, event);
        } catch(Exception ex) {
            Logger.e("Failed to run event :: id=%d", event.event_id);
        }
    }

    // -------------------------------------------------------------------------
    public LevelMinimap GetMinimap() {
        return m_minimap;
    }

    // -------------------------------------------------------------------------
    public LevelTarget GetTarget() {
        return m_target;
    }

    // -------------------------------------------------------------------------
    public LevelTarget UpdateTarget() {
        m_target.UpdateChars(m_cache_chars);
        return m_target;
    }

    // -------------------------------------------------------------------------
    public void UpdatePackedTileSprite() {
        m_update_packed_tile_sprite = true;
    }

    // -------------------------------------------------------------------------
    public LevelTarget SelectNextTarget() {
        LevelObjectChar char_obj = m_target.SelectNextTarget();
        if(char_obj != null) {
            m_hero.Rotate(char_obj);
        }
        return m_target;
    }

    // -------------------------------------------------------------------------
    public LevelObjectChar GetTargetChar() {
        return m_target.GetSelectedChar();
    }

    // -------------------------------------------------------------------------
    public Map InitializeTerrain(int width, int height) {
        if(width == m_map_width && height == m_map_height) {
            return m_terrain_map;
        }

        // Create static terrain context
        m_terrain_ctx = new LevelObjectTerrain.UpdateCtx[width * height];
        m_map_width = width; 
        m_map_height = height;

        // Snapshot of previous map which is used for 
        // incremental checksum calculation
        m_map_prev = new MapCell[m_terrain_ctx.length];
        m_map_csum = 0;

        // Create node for storing dying objects
        m_trash_bin = (LevelTrashBin)SgAddChild(new LevelTrashBin("lvl-trash-bin"));

        // Create cells
        m_cache_cells.Clear();
        m_cells = (GameNode)SgAddChild(new GameNode("cells", 1.0f));
        for(int i = 0; i < m_terrain_ctx.length; i++) {
            // Calculate initial position of the cell
            int x = i % width, z = i / width;
            float size = Main.inst.cfg.lvl_cell_size;

            // Create update-context for each terrain
            LevelObjectTerrain.UpdateCtx ctx = m_terrain_ctx[i] = 
              new LevelObjectTerrain.UpdateCtx(i, x * size, 0.0f, z * size);

            // Fill previous map
            if(m_map_prev != null) {
                m_map_prev[i] = new MapCell();
            }

            // Create cell
            LevelObject cell = (LevelObject)m_cells.SgAddChild(
              new LevelObjectCell(
                String.format("cell-%d-%d-%d", i, x, z), i, x, z, m_cache_cells));
            m_cache_cells.Put(cell);

            // Update cell only once after creation
            cell.OnUpdate(ctx, m_trash_bin);
        }

        // Initialize cell neighbors
        for(int i = 0; i < m_terrain_ctx.length; i++) {
            ((LevelObjectCell)m_cache_cells.Get(i)).InitNeighbors(width);
        }

        // Create terrain map
        m_terrain_map = new Map(m_terrain_ctx, width, height);
        m_terrain_map_size2 = width * width + height * height;

        // Packed wall
        RenderableMan rm = Main.inst.renderable_man;
        m_packed_tiles = (GameNode)SgAddChild(new GameNode("packed-tiles", 1.0f));
        m_packed_tile_wall = (PackedTile)m_packed_tiles.SgAddChild(
          new PackedTile("packed-wall", Renderer.Layer.OPAQUE_TERRAIN, 
            rm.GetSprite(MapEnum.TerrainType.WALL), 
            width, height, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.5f, 0.0f), null,
            Main.inst.cfg.lvl_pack_color_wall));

        // Packed floor
        m_packed_tile_floor = (PackedTile)m_packed_tiles.SgAddChild(
          new PackedTile("packed-floor", Renderer.Layer.OPAQUE_TERRAIN,
            rm.GetSprite(MapEnum.TerrainType.EMPTY), 
            width, height, new Vector3(1.0f, 0.0f, 1.0f), null, null, 
            Main.inst.cfg.lvl_pack_color_floor));

        // Packed empty space
        m_packed_tile_empty_space = (PackedTile)m_packed_tiles.SgAddChild(
          new PackedTile("packed-empty-space", Renderer.Layer.OPAQUE_TERRAIN,
            rm.GetSprite(MapEnum.TerrainType.EMPTY_SP), 
            width, height, new Vector3(1.0f, 0.0f, 1.0f), null, null, 
            Main.inst.cfg.lvl_pack_color_empty_space));

        // Packed water
        m_packed_tile_water = (PackedTile)m_packed_tiles.SgAddChild(
          new PackedTile("packed-water", Renderer.Layer.OPAQUE_WATER,
            rm.GetSprite("water", 0), width, height, 
            new Vector3(1.0f, 0.0f, 1.0f), new Vector3(0.0f, 0.05f, 0.0f), 
            Main.inst.cfg.water_alpha, Main.inst.cfg.lvl_pack_color_water));

        // Packed grass
        m_packed_tile_grass = (PackedTile)m_packed_tiles.SgAddChild(
          new PackedTile("packed-grass", Renderer.Layer.TRANSPARENT_TERRAIN,
            rm.GetSprite(MapEnum.TerrainType.GRASS),
            width, height, new Vector3(1.0f, 0.0f, 1.0f), new Vector3(0.0f, 0.05f, 0.0f), 
            1.0f, Main.inst.cfg.lvl_pack_color_grass));

        // Fog of war
        m_fog = (LevelFog)SgAddChild(new LevelFog(
          Renderer.Layer.TRANSPARENT_FOG, width, height, 
          new Vector3(1.0f, 0.0f, 1.0f), new Vector3(0.0f, 1.1f, 0.0f), 
          Main.inst.cfg.fog_alpha, Main.inst.cfg.fog_color));

        // Skybox
        float sky_size = Renderer.skybox_distance_hack - 100.0f;
//        float fog_size = Renderer.skybox_distance_hack - 200.0f;
        Color fog_color = new Color(Main.inst.cfg.fog_color); fog_color.a = Main.inst.cfg.fog_alpha; 
        m_skybox = (LevelSkybox)SgAddChild(new LevelSkybox());
        m_skybox.CreateSkybox("pd3d_skybox2.png", Renderer.Layer.OPAQUE_SKY, sky_size, 
          new Color(0.2f, 0.2f, 0.2f, 1.0f), false);
//        m_skybox.CreateSkybox("fog", Renderer.Type.TRANSPARENT_FOG, fog_size, fog_color, true);

        // Minimap
        m_minimap = (LevelMinimap)SgAddChild(new LevelMinimap());
        return m_terrain_map;
    }

    // -------------------------------------------------------------------------
    public boolean DeserializeTerrain(int width, int height, String terrain_srlz) {
        // Create map
        if(m_terrain_map == null || m_terrain_map.GetWidth() != width || 
          m_terrain_map.GetHeight() != height) {
            InitializeTerrain(width, height);
        }

        // Deserialize map
        return m_terrain_map.DeserializeMap(terrain_srlz);
    }

    // -------------------------------------------------------------------------
    public boolean TestChecksum(int correct_csum) {
        if(m_map_prev == null) {
            return true;
        }

        // Calculate incremental checksum by comparing previous value of the cell 
        // with current value
        int dirty_num = 0;
        for(int i = 0; i < m_terrain_ctx.length; i++) {
            LevelObjectTerrain.UpdateCtx cur_cell = m_terrain_ctx[i];
            MapCell prev_cell = m_map_prev[i];
            if(cur_cell.IsDirty()) {
                dirty_num++;
                m_map_csum = Utils.UpdateCsum(m_map_csum, 
                  prev_cell.GetCsum(), cur_cell.GetCsum());
            }
            prev_cell.flags = cur_cell.flags;
            prev_cell.type = cur_cell.type;
        }

        // Checksum is good
        if(correct_csum == m_map_csum) {
            Logger.d("Level checksum is good :: client=%d dirty-num=%d", 
              correct_csum, dirty_num);
            return true;
        }

        // Force full update if checksum is bad
        Logger.e("Level checksum is bad :: server=%d client=%d dirty_num=%d", 
          correct_csum, m_map_csum, dirty_num);
        m_map_csum = correct_csum;
        for(LevelObjectTerrain.UpdateCtx ctx : m_terrain_ctx) {
            ctx.flags = Utils.FlagSet(ctx.flags, MapEnum.TerrainFlags.PD3D_DIRTY.flag);
        }
        return false;
    }

    // -------------------------------------------------------------------------
    public boolean UpdateTerrain(int origin_id, int dungeon_depth, 
      UtilsClass.Callback cb) {
        m_update_cache = true;
        m_update_cb = cb;

        // Dungeon depth
        m_dungeon_depth = dungeon_depth;

        // Pre-update terrain
        for(LevelObjectTerrain.UpdateCtx ctx : m_terrain_ctx) {
            int pd_id = ctx.GetPdId();
            int cell_id = ctx.GetCellId();

            // Create terrain object if missing
            LevelObjectTerrain terrain_obj = 
              (LevelObjectTerrain)m_cache_terrains.Get(pd_id);
            if(terrain_obj == null) {
                // Get cell object that will own terrain
                LevelObjectCell cell = GetCell(cell_id);

                // Create terrain object
                terrain_obj = (LevelObjectTerrain)cell.SgAddChild(
                  new LevelObjectTerrain("terrain-" + pd_id, pd_id));
                m_cache_terrains.Put(terrain_obj);
            }

            // Pre-update terrain
            terrain_obj.OnPreUpdate(ctx, m_trash_bin);
        }

        // Get origin cell
        LevelObjectCell origin_cell = 
          (LevelObjectCell)m_cache_cells.Get(origin_id);

        // Update terrain
        for(LevelObjectTerrain.UpdateCtx ctx : m_terrain_ctx) {
            // Update context before doing update
            ctx.origin_cell = origin_cell;
            ctx.map_size2 = m_terrain_map_size2;

            // Update terrain
            LevelObjectTerrain terrain = 
              (LevelObjectTerrain)m_cache_terrains.Get(ctx.GetPdId());
            terrain.OnUpdate(ctx, m_trash_bin);

            // Detect terrain
            if(!dbg_detect_terrain_done && dbg_detect_terrain != null) {
                for(MapEnum.TerrainType t : dbg_detect_terrain) {
                    if(t == ctx.type) {
                        Logger.d("DEBUG :: terrain detected :: terrain=%s", 
                          dbg_detect_terrain.toString());
                        dbg_detect_terrain_done = true;
                        break;
                    }
                }
            }
        }

        // 42nd cell becomes default camera target
        Main.inst.level_camera.SetTarget(m_cache_cells.Get(42));

        // Update packed tiles
        if(m_update_packed_tile_sprite) {
            m_update_packed_tile_sprite = false;

            Logger.d("Updating packed tiles");
            RenderableMan mm = Main.inst.renderable_man;
            m_packed_tile_wall.SetSprite(mm.GetSprite(MapEnum.TerrainType.WALL));
            m_packed_tile_floor.SetSprite(mm.GetSprite(MapEnum.TerrainType.EMPTY));
            m_packed_tile_empty_space.SetSprite(mm.GetSprite(MapEnum.TerrainType.EMPTY_SP));
            m_packed_tile_water.SetSprite(mm.GetSprite("water", 0));
            m_packed_tile_grass.SetSprite(mm.GetSprite(MapEnum.TerrainType.GRASS));
        }
        return true;
    }

    // -------------------------------------------------------------------------
    public LevelObjectDynamic UpdateChar(DescChar desc) {
        // Update char object
        LevelObjectChar.UpdateCtx ctx = new LevelObjectChar.UpdateCtx(desc);
        LevelObjectChar obj = (LevelObjectChar)UpdateDynamicObject(
          LevelObjectChar.class, ctx, m_cache_chars);

        // Object is not visible
        if(obj == null) {
            return null;
        }

        // Hero
        if(obj.IsHero()) {
            m_hero = obj;
            Main.inst.level_camera.SetTarget(m_hero);

            // Update minimap
            m_minimap.SetCenter(ctx.GetCellId());
            m_minimap.UpdateChar(ctx.GetCellId(), RenderableObjectType.CHAR, 
              MapEnum.CharType.WARRIOR0.ordinal());

        // Enemy
        } else {
            // All enemies should be looking at hero
            if(m_hero != null) {
                obj.ScheduleRotate(m_hero);
            }

            // Update minimap
            m_minimap.UpdateChar(ctx.GetCellId(), RenderableObjectType.CHAR, 
              MapEnum.CharType.RAT.ordinal());
        }

        // Show char status string
        if(desc.status_string != null) {
            for(DescStringInst status : desc.status_string) {
                obj.AddFadingStatus(status);
            }
        }

        // Show char status sprite
        if(desc.status_sprite != null) {
            for(DescSpriteInst status : desc.status_sprite) {
                obj.AddFadingStatus(status);
            }
        }

        obj.SetDbgRender(
          Main.inst.cfg.dbg_render_axis);
        return obj;
    }

    // -------------------------------------------------------------------------
    public LevelObjectDynamic UpdateItem(DescHeap desc) {
        LevelObjectItem obj = (LevelObjectItem)UpdateDynamicObject(
          LevelObjectItem.class, new LevelObjectItem.UpdateCtx(desc), 
          m_cache_items);

        // Object is not visible
        if(obj == null) {
            return null;
        }

        obj.SetDbgRender(
          Main.inst.cfg.dbg_render_axis);
        return obj;
    }

    // -------------------------------------------------------------------------
    public LevelObjectDynamic UpdatePlant(DescHeap heap_desc) {
        LevelObjectPlant obj = (LevelObjectPlant)UpdateDynamicObject(
          LevelObjectPlant.class, new LevelObjectPlant.UpdateCtx(heap_desc), 
          m_cache_plants);

        // Object is not visible
        if(obj == null) {
            return null;
        }

        obj.SetDbgRender(
          Main.inst.cfg.dbg_render_axis);
        return obj;
    }

    // -------------------------------------------------------------------------
    private LevelObjectDynamic UpdateDynamicObject(
      Class<? extends LevelObjectDynamic> obj_class, LevelObject.IUpdateCtx ctx, 
      LevelObjectCache cache) {

        // Ignore objects (chars/items) if they are covered by fog
        LevelObjectCell cell = GetCell(ctx.GetCellId());
        if(!Main.inst.cfg.dbg_render_non_visible_obj) {
            LevelObjectTerrain terrain = cell.GetTerrain();
            if(terrain != null && terrain.HasObject(RenderableObjectType.FOG)) {
                return null;
            }
        }

        // Create object if missing
        int pd_id = ctx.GetPdId();
        LevelObjectDynamic obj = (LevelObjectDynamic)cache.Get(pd_id);
        if(obj == null) {
            // Create new char
            if(obj_class == LevelObjectChar.class) {
                obj = new LevelObjectChar("char-" + pd_id, pd_id);

            // Create new item
            } else if(obj_class == LevelObjectItem.class) {
                obj = new LevelObjectItem("item-" + pd_id, pd_id);

            // Create new plant
            } else if(obj_class == LevelObjectPlant.class) {
                obj = new LevelObjectPlant("plant-" + pd_id, pd_id);
            }

            // Cell becomes parent of new object
            cell.SgAddChild(obj);

            // Put new object to cache
            cache.Put(obj);
        }

        // Update object
        obj.OnUpdate(ctx, m_trash_bin);
        return obj;
    }

    // -------------------------------------------------------------------------
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private LinkedList<? extends Desc> GetDynamicObjectDesc(
      Class<? extends LevelObjectDynamic> obj_class) {
        LinkedList list = null;
        Iterator<LevelObject> it = null;
        if(obj_class == LevelObjectChar.class && m_cache_chars.GetSize() > 0) {
            it = m_cache_chars.GetIt();
            list = new LinkedList<DescChar>();
        } else if(obj_class == LevelObjectItem.class && m_cache_items.GetSize() > 0) {
            it = m_cache_items.GetIt();
            list = new LinkedList<DescHeap>();
        } else if(obj_class == LevelObjectPlant.class && m_cache_plants.GetSize() > 0) {
            it = m_cache_items.GetIt();
            list = new LinkedList<DescHeap>();
        } else {
            return null;
        }

        while(it.hasNext()) {
            LevelObjectDynamic obj = (LevelObjectDynamic)it.next();
            list.add(obj.GetUpdateCtx().GetDescriptor());
        }
        return list;
    }

    //--------------------------------------------------------------------------
    public void UpdateFow(int idx, float distance_to_origin, boolean is_visible) {
        if(m_fog == null) {
            return;
        }
        m_fog.UpdateVisibility(idx, distance_to_origin, is_visible);
    }

    //--------------------------------------------------------------------------
    public void ThrowObject(final RenderableObject robj, final int src_cell, 
      final int dst_cell, boolean do_return) {
        LevelObjectCell src = GetCell(src_cell);
        LevelObjectCell dst = GetCell(dst_cell);

        // Attach object to source cell
        src.SgAddChild(robj);

        // Model becomes child of destination cell and tweens position 
        // towards center of new parent
        Vector3 dest_pos = new Vector3();
        if(!do_return) {
            dst.SgRelocateChild(robj);

        // Model remains child of source cell and tweens position
        // towards center of destination cell and back 
        } else {
            dest_pos.set(src.GetPosDiff(dst));
        }

        // Calculate tween duration
        final float duration = src.GetDistanceToTarget(dst, true) * 
          Main.inst.cfg.model_throw_unit_duration;

        // DESC -> SRC
        final Tweener.Callback dest_to_src_cb = (!do_return) ? null : 
          new Tweener.Callback(robj) {
            @Override public void OnComplete() {
                Main.inst.sg_man.ScheduleForDeletion(robj);
            }
        };

        // SRC -> DEST
        Tweener.Callback src_to_dest_cb = 
          new Tweener.Callback(robj) {
            @Override public void OnComplete() {
                if(dest_to_src_cb == null) {
                    Main.inst.sg_man.ScheduleForDeletion(robj);
                } else {
                    robj.TweenPos(new Vector3(), duration, dest_to_src_cb);
                }
            }
        };

        // Tween
        robj.TweenPos(dest_pos, duration, src_to_dest_cb);
    }

    //--------------------------------------------------------------------------
    public LevelSkybox GetSkybox() {
        return m_skybox;
    }

    //--------------------------------------------------------------------------
    public int GetDungeonDepth() {
        return m_dungeon_depth;
    }

    //--------------------------------------------------------------------------
    public PackedTile GetPackedTile(RenderableObjectType type) {
        return 
          (!Main.inst.cfg.lvl_pack) ? null :
          (type == RenderableObjectType.FLOOR) ? m_packed_tile_floor :
          (type == RenderableObjectType.EMPTY_SPACE) ? m_packed_tile_empty_space :
          (type == RenderableObjectType.WATER) ? m_packed_tile_water :
          (type == RenderableObjectType.WALL) ? m_packed_tile_wall : 
          (type == RenderableObjectType.GRASS_LOW) ? m_packed_tile_grass : null;
    }

    //--------------------------------------------------------------------------
    public void OnToggleObject(RenderableObject robj, boolean is_added) {
        // Should have packed tile
        Utils.Assert(robj.HasPackedTile(), "Failed to toggle packed model");

        // FLOOR
        RenderableObjectType type = robj.GetObjectType();
        int tile_idx = robj.GetPackedTileIdx();
        if(type == RenderableObjectType.FLOOR || type == RenderableObjectType.WALL) {
            m_packed_tile_floor.ToggleNode(tile_idx, is_added, true);
        }

        // EMPTY SPACE
        if(type == RenderableObjectType.EMPTY_SPACE) {
            m_packed_tile_empty_space.ToggleNode(tile_idx, is_added, true);
        }

        // WALL
        if(type == RenderableObjectType.WALL) {
            m_packed_tile_wall.ToggleNode(tile_idx, is_added, true);
        }

        // WATER
        if(type == RenderableObjectType.WATER) {
            m_packed_tile_water.ToggleNode(tile_idx, is_added, true);
        }

        // GRASS
        if(type == RenderableObjectType.GRASS_LOW) {
            m_packed_tile_grass.ToggleNode(tile_idx, is_added, true);
        }
    }

    // -------------------------------------------------------------------------
    public LevelObjectCell GetCell(int id) {
        LevelObjectCell cell = (LevelObjectCell)m_cache_cells.Get(id);
        Utils.Assert(cell != null, 
          "Failed to get cell object, cell not found :: id=%d", id);
        return cell;
    }

    // -------------------------------------------------------------------------
    public LevelObjectCell GetCell(int x, int z) {
        if(x < 0 || x >= m_map_width || z < 0 || z >= m_map_height) {
            return null;
        }
        return GetCell(z * m_map_width + x);
    }

    //--------------------------------------------------------------------------
    public LevelObjectChar GetHero() {
        return m_hero;
    }

    //--------------------------------------------------------------------------
    public GameNode GetSelectedNode(int screen_x, int screen_y) {
        if(m_camera == null) {
            return null;
        }

        // Don't select hero if first person camera
        HashSet<GameNode> blacklist = new HashSet<GameNode>();
        if(((LevelCamera)m_camera).IsFirstPerson() && m_hero != null) {
            blacklist.add(m_hero.GetMainObject());
        }

        // Select
        GameNode.SelectCtx select_ctx = new GameNode.SelectCtx()
          .Init(screen_x, screen_y, m_camera, blacklist);
        SgNode.Walk(this, true, GameNode.SELECT.ResetStats(), 0, 
          select_ctx);
        return select_ctx.selected_node;
    }

    //--------------------------------------------------------------------------
    public Vector3 GetSelectedCoord(int screen_x, int screen_y) {
        // Get selection coordinate on zero-plane
        Vector3 intersection = new Vector3();
        if(Intersector.intersectRayPlane(
          m_camera.GetGdxCamera().getPickRay((int)screen_x, (int)screen_y), 
          new Plane(Vector3.Y, Vector3.Zero), 
          intersection)) {
            return intersection;
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public LevelObjectCell GetSelectedCell(int screen_x, int screen_y, 
      boolean create_marker, boolean allow_hero_selection) {
        // Get selected node
        GameNode selected_node = GetSelectedNode(screen_x, screen_y);

        // Find parent cell 
        LevelObjectCell selected_cell = (selected_node == null) ? null : 
          (LevelObjectCell)selected_node.SgGetParent(LevelObjectCell.class);

        // Try selecting invisible cell
        if(selected_cell == null) {
            Vector3 coord = GetSelectedCoord(screen_x, screen_y);
            if(coord != null) {
                int cell_x, cell_z;
                float cell_size = Main.inst.cfg.lvl_cell_size;
                cell_x = (int)((coord.x + cell_size / 2) / cell_size);
                cell_z = (int)((coord.z + cell_size / 2) / cell_size);
                Logger.d(
                  "Selecting cell :: screen-pos=%d:%d zero-plane-coord=%.1f:%.1f:%.1f cell-pos=%d:%d", 
                  screen_x, screen_y, coord.x, coord.y, coord.z, cell_x, cell_z);

                selected_cell = GetCell(cell_x, cell_z);
            }
        }

        // Get hero cell
        LevelObjectCell hero_cell = 
          (LevelObjectCell)m_hero.SgGetParent(LevelObjectCell.class);

        // Make sure we do not select cell where hero is standing
        boolean discard_hero_cell = false;
        if(!allow_hero_selection && m_hero != null) {
            if(hero_cell != null && hero_cell == selected_cell) {
                discard_hero_cell = true;
            }
        }

        Logger.d("Selecting cell :: screen-pos=%d:%d selected-node=%s selected-cell=%s discard-hero-cell=%s", 
          screen_x, screen_y,
          (selected_node == null) ? "none" : selected_node.SgGetNameId(),
          (selected_cell == null) ? "none" : selected_cell.SgGetNameId(),
          Boolean.toString(discard_hero_cell));

        // Create marker
        if(create_marker && selected_cell != null && selected_cell != hero_cell) {
            CreateMarker(selected_cell, RenderableObjectType.MARKER_SELECT);
        }
        return (discard_hero_cell) ? null : selected_cell;
    }

    //--------------------------------------------------------------------------
    public void CreateMarker(LevelObjectCell cell, RenderableObjectType marker) {
        // Attach new marker to cell
        final RenderableObject obj = (RenderableObject)cell.SgAddChild(
          marker.Create());

        // Delete when tween is over
        Tweener.Callback cb = new Tweener.Callback(obj) {
            @Override public void OnComplete() {
                Main.inst.sg_man.ScheduleForDeletion(obj);
            }
        };

        // Selection marker
        switch(marker) {
        //......................................................................
        case MARKER_SELECT: {
            // Tween marker alpha [0.0 -> alpha -> 0.0]    
            float old_alpha = obj.SetLocalAlpha(0.0f);
            obj.TweenAlpha(new Float[] { old_alpha, 0.0f }, 
              Main.inst.cfg.model_marker_fade_duration, cb);
        } break;

        //......................................................................
        case MARKER_SEARCH: {
            // Scale down to 0.0f
            obj.TweenScale(new Vector3(0.1f, 0.1f, 0.1f), 
            Main.inst.cfg.model_marker_fade_duration, cb);
        } break;

        //......................................................................
        case MARKER_BLOOD: {
            obj.TweenAlpha(0.0f, Main.inst.cfg.model_blood_fade_duration, cb);
        } break;

        //......................................................................
        default:
            break;
        }
    }

    //--------------------------------------------------------------------------
    public void CreateRipple(LevelObjectCell cell, float delay) {
        // Create ripple
        final RenderableObject robj = (RenderableObject)
          cell.SgAddChild(RenderableObjectType.FX_RIPPLE.Create());

        // Add random offset
        Vector3 pos = robj.GetLocalPos(true);
        pos.x = MathUtils.random(0.3f);
        pos.z= MathUtils.random(0.3f);

        // Fade ripple
        robj.TweenAlpha(0.0f, 1.0f + delay, null);

        // Scale ripple
        float scale = 15.0f + delay * 5;
        robj.TweenScale(new Vector3(scale, 0.7f, scale), 1.3f + delay, 
          new Tweener.Callback(robj) {
            @Override public void OnComplete() {
                Main.inst.sg_man.ScheduleForDeletion(robj);
            }
        });
    }

    //--------------------------------------------------------------------------
    public void CreateParticle(LevelObjectCell cell, RenderableObjectType pfx_type, 
      DescPfxMutator mutator) {
        Utils.Assert(pfx_type.IsParticle(), 
          "Failed to create particle :: cell=%d type=%s",
          cell.GetPdId(), pfx_type.toString());

        // Create particle
        RendererParticle pfx = ((Particle)cell.SgAddChild(
          pfx_type.Create())).GetPfx();

        // Apply mutator
        if(mutator != null) {
            pfx.ApplyMutator(mutator);
        }

        // Restart particle
        pfx.Reset().StopEmitter();
    }

    //--------------------------------------------------------------------------
    public boolean IsCharMovementOver() {
        // Movement is considered to be over if it ends on next frame 
        long frame_duration = Main.inst.renderer.GetFrameDuration();

        // Check if movement of all chars is over
        Iterator<LevelObject> it = m_cache_chars.GetIt();
        while(it.hasNext()) {
            LevelObject obj = it.next();
            if(!obj.IsDefaultAnimRunning() ||
               obj.GetMoveTween().GetTtl() > frame_duration) {
                return false;
            }
        }
        return true;
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Get rendering camera
        m_camera = ctx.GetCamera(Renderer.CameraType.LEVEL);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        super.OnMethodPostRender(ctx, pre_children);

        // Ignore post-children
        if(!pre_children) {
            return false;
        }

        // Delete all non-updated objects from cache
        if(m_update_cache) {
            for(LevelObjectCache cache : m_cache) {
                if(cache == m_cache_cells) {
                    continue; // Do not update cells
                }

                Iterator<LevelObject> it = cache.GetIt();
                while(it.hasNext()) {
                    // Ignore updated objects
                    LevelObject obj = it.next();
                    if(obj.TestUpdateFlag()) {
                        continue;
                    }

                    // Delete non-updated object ...
                    obj.OnDelete(m_trash_bin);

                    // ... and remove it from cache
                    it.remove();
                }
            }

            if(m_update_cb != null) {
                m_update_cb.Run();
                m_update_cb = null;
            }
            m_update_cache = false;
        }

        // Do not call post-render for child nodes
        return false;
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public boolean OnMethodSnapshot(DescSnapshot snapshot) {
        DescSceneGame desc = snapshot.game = new DescSceneGame();
        desc.map_width = m_map_width;
        desc.map_height = m_map_height;
        desc.map_srlz = m_terrain_map.SerializeMap();
        desc.dungeon_depth = m_dungeon_depth;
        desc.chars = 
          (LinkedList<DescChar>)GetDynamicObjectDesc(LevelObjectChar.class);
        desc.heaps = 
          (LinkedList<DescHeap>)GetDynamicObjectDesc(LevelObjectItem.class);
        desc.plants = 
          (LinkedList<DescHeap>)GetDynamicObjectDesc(LevelObjectPlant.class);
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        m_cells = null;
        m_trash_bin = null;
        m_packed_tiles = null;

        // Terrain map
        m_terrain_map = null;

        // Terrain context
        if(m_terrain_ctx != null) {
            for(int i = 0; i < m_terrain_ctx.length; i++) {
                m_terrain_ctx[i] = null;
            }
            m_terrain_ctx = null;
        }

        // Cache
        for(int i = 0; i < m_cache.length; i++) {
            m_cache[i].OnCleanup();
            m_cache[i] = null;
        }
        m_cache = null;

        // Camera target
        m_camera = null;
        return true;
    }
}
