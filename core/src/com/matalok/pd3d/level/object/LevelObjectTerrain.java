//------------------------------------------------------------------------------
package com.matalok.pd3d.level.object;

//------------------------------------------------------------------------------
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.Desc;
import com.matalok.pd3d.level.LevelDirection;
import com.matalok.pd3d.level.LevelTrashBin;
import com.matalok.pd3d.map.MapCell;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.renderable.RenderableObject;
import com.matalok.pd3d.renderable.RenderableObject.ITemplate;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.renderable.model.template.MTmplSpriteVertical;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgNode;

//------------------------------------------------------------------------------
public class LevelObjectTerrain 
  extends LevelObject {
    //**************************************************************************
    // UpdateCtx
    //**************************************************************************
    public static class UpdateCtx
      extends MapCell
      implements LevelObject.IUpdateCtx {
        //**********************************************************************
        // Desc
        //**********************************************************************
        public int idx;
        public Vector3 pos;
        public LevelObjectCell origin_cell;
        public float map_size2;

        //----------------------------------------------------------------------
        public UpdateCtx(int idx, float x, float y, float z) {
            super();
            this.pos = new Vector3(x, y, z);
            this.idx = idx;
        }

        //**********************************************************************
        // LevelObject.IUpdateCtx
        //**********************************************************************
        @Override public int GetCellId() {
            return idx;
        }

        //----------------------------------------------------------------------
        @Override public int GetPdId() {
            return idx;
        }

        //----------------------------------------------------------------------
        @Override public int GetModelId() {
            return -1;
        }

        //----------------------------------------------------------------------
        @Override public Desc GetDescriptor() {
            return null;
        }

        //----------------------------------------------------------------------
        @Override public boolean IsDirty() {
            return Utils.FlagTest(flags, MapEnum.TerrainFlags.PD3D_DIRTY.flag);
        }
    }

    //**************************************************************************
    // Alignment
    //**************************************************************************
    public enum Alignment {
        //----------------------------------------------------------------------
        X_ALIGNED(+00.0f),                      // Parallel to X axis
        Z_ALIGNED(+90.0f),                      // Parallel to Z axis
        CORNER_ZX(+45.0f),                      // In ZX corner
        CORNER_XZ(-45.0f),                      // In XZ corner (opposite to ZX along the X axis)
        NONE(MathUtils.random(20.0f, 70.0f));   // No alignment

        //----------------------------------------------------------------------
        public float rotation;

        //----------------------------------------------------------------------
        private Alignment(float rotation) {
            this.rotation = rotation;
        }
    }

    //**************************************************************************
    // Terrain enums
    //**************************************************************************
    public enum Base {
        WALL, FLOOR,
    }
    public enum Material {
        STONE, WOOD, WATER, GRASS, DECO, EMBERS,
    }
    public enum Architecture {
        WELL, ALCHEMY, PEDESTAL, ENTRANCE, EXIT, SIGN, DOOR, BARRICADE, STATUE, 
        BOOK_SHELF, BOSS_EXIT,
    }
    public enum Trap {
        TOXIC, FIRE, PARALYTIC, POISON, ALARM, LIGHTNING, GRIPPING, SUMMONING,
    }
    public enum Flag {
        // Base flags
        PASSABLE, VISIBLE, ACTIVE,

        // Misc flags 
        CLOSED, LOCKED, HIGH, EMPTY
    }
    public enum Fx {
        FIRE, FIRE_SACRIFICIAL,
        FOLIAGE, FREEZING, REGROWTH, WEB,
        GAS_CONFUSION, GAS_PARALYTIC, GAS_TOXIC, 
        WATER_OF_AWARENESS, WATER_OF_HEALTH, WATER_OF_TRANSMUTATION, WATER_WELL,
        ALCHEMY;
    }

    //**************************************************************************
    // LevelObjectCell
    //**************************************************************************
    private EnumSet<RenderableObjectType> m_types_cur;
    private EnumSet<RenderableObjectType> m_types_old;

    private Base m_layer_base;
    private Material m_layer_material0;
    private Material m_layer_material1;
    private Architecture m_layer_arch;
    private Trap m_layer_trap;
    private EnumSet<Fx> m_fx;

    private EnumSet<Flag> m_flags_base;
//    private EnumSet<Flag> m_flags_material0;
    private EnumSet<Flag> m_flags_material1;
    private EnumSet<Flag> m_flags_arch;
    private EnumSet<Flag> m_flags_trap;

    private Boolean m_rotate_door;
    private HashSet<RenderableObject> m_pack_pending;

    private LevelObjectCell m_cache_origin_cell;
    private float m_cache_distance_limit;
    private float m_cache_distance_to_origin;

    private int m_cell_elevation_step, m_cell_elevation_step_old;

    //--------------------------------------------------------------------------
    public LevelObjectTerrain(String name, int level_obj_id) {
        super(name, level_obj_id);

        m_types_cur = EnumSet.noneOf(RenderableObjectType.class);
        m_pack_pending = new HashSet<RenderableObject>();
    }

    //--------------------------------------------------------------------------
    private void UpdateLayers(MapEnum.TerrainType type, int flags) {
        //......................................................................
        // Base flags

        // Passable
        if(Utils.FlagTest(flags, MapEnum.TerrainFlags.PASSABLE.flag)) {
            m_flags_base.add(Flag.PASSABLE);
        }

        // Visible
        if(Utils.FlagTest(flags, MapEnum.TerrainFlags.VISIBLE.flag)) {
            m_flags_base.add(Flag.VISIBLE);
        }

        // Active
        if(Utils.FlagTest(flags, MapEnum.TerrainFlags.VISITED.flag)) {
            m_flags_base.add(Flag.ACTIVE);
        }

        //......................................................................
        // Base

        // No base for exit tile
        if(type == MapEnum.TerrainType.EXIT) {
            m_layer_base = null;

        // Exit from boss level is like a poster on the wall
        } else if(type == MapEnum.TerrainType.LOCKED_EXIT || 
                  type == MapEnum.TerrainType.UNLOCKED_EXIT) {
            m_layer_base = Base.WALL;

        // Trap
        } else if(type.is_trap) {
            // Stone floor is below the trap
            m_layer_base = Base.FLOOR;
            m_layer_material0 = Material.STONE;

            // Trap is active
            if(type.is_trap_active) {
                m_flags_trap.add(Flag.ACTIVE);
            }

            // Trap is visible
            if(type.is_trap_visible) {
                m_flags_trap.add(Flag.VISIBLE);
            }

            // Trap types
            if(type == MapEnum.TerrainType.TOXIC_TRAP) { 
                m_layer_trap = Trap.TOXIC;
            } else if(type == MapEnum.TerrainType.FIRE_TRAP) {
                m_layer_trap = Trap.FIRE;
            } else if(type == MapEnum.TerrainType.PARALYTIC_TRAP) {
                m_layer_trap = Trap.PARALYTIC;
            } else if(type == MapEnum.TerrainType.POISON_TRAP) {
                m_layer_trap = Trap.POISON;
            } else if(type == MapEnum.TerrainType.ALARM_TRAP) {
                m_layer_trap = Trap.ALARM;
            } else if(type == MapEnum.TerrainType.LIGHTNING_TRAP) {
                m_layer_trap = Trap.LIGHTNING;
            } else if(type == MapEnum.TerrainType.GRIPPING_TRAP) {
                m_layer_trap = Trap.GRIPPING;
            } else if(type == MapEnum.TerrainType.SUMMONING_TRAP) {
                m_layer_trap = Trap.SUMMONING;
            }

        // Floor
        } else if(Utils.FlagTest(flags, MapEnum.TerrainFlags.PASSABLE.flag) || 
           type == MapEnum.TerrainType.LOCKED_DOOR || 
           type == MapEnum.TerrainType.BARRICADE ||
           type == MapEnum.TerrainType.ALCHEMY ||
           type == MapEnum.TerrainType.WELL ||
           type == MapEnum.TerrainType.EMPTY_WELL ||
           type == MapEnum.TerrainType.STATUE ||
           type == MapEnum.TerrainType.STATUE_SP ||
           type == MapEnum.TerrainType.BOOKSHELF) {
            m_layer_base = Base.FLOOR;

            // Material at layer #0
            if(type == MapEnum.TerrainType.EMPTY_SP || 
               type == MapEnum.TerrainType.ALCHEMY) {
                m_layer_material0 = Material.WOOD;
            } else {
                m_layer_material0 = Material.STONE;
            }

        // Chasm 
        } else if(type == MapEnum.TerrainType.CHASM || 
                  type == MapEnum.TerrainType.CHASM_FLOOR ||
                  type == MapEnum.TerrainType.CHASM_FLOOR_SP ||
                  type == MapEnum.TerrainType.CHASM_WALL ||
                  type == MapEnum.TerrainType.CHASM_WATER) {
            m_layer_base = null;

        // Wall
        } else {
            m_layer_base = Base.WALL;
        }

        //......................................................................
        // Material at layer #1
        //

        // Water
        if(Utils.FlagTest(flags, MapEnum.TerrainFlags.WATER.flag)) {
            m_layer_material1 = Material.WATER;
        }

        // Grass
        if(type == MapEnum.TerrainType.GRASS || 
           type == MapEnum.TerrainType.HIGH_GRASS) {
            m_layer_material1 = Material.GRASS;

            if(type == MapEnum.TerrainType.HIGH_GRASS) {
                m_flags_material1.add(Flag.HIGH);
            }
        }

        // Embers
        if(type == MapEnum.TerrainType.EMBERS) {
            m_layer_material1 = Material.EMBERS;
        }

        // Floor/wall deco
        if(type == MapEnum.TerrainType.EMPTY_DECO || 
           type == MapEnum.TerrainType.WALL_DECO) {
            m_layer_material1 = Material.DECO;
        }

        //......................................................................
        // Architecture
        //

        // Door
        if(type == MapEnum.TerrainType.DOOR || 
           type == MapEnum.TerrainType.LOCKED_DOOR ||
           type == MapEnum.TerrainType.OPEN_DOOR) {
            m_layer_arch = Architecture.DOOR;

            // Closed door
            if(type != MapEnum.TerrainType.OPEN_DOOR) {
                m_flags_arch.add(Flag.CLOSED);
            }

            // Locked door
            if(type == MapEnum.TerrainType.LOCKED_DOOR) {
                m_flags_arch.add(Flag.LOCKED);
            }
        }

        // Exit from boss level
        if(type == MapEnum.TerrainType.LOCKED_EXIT || 
           type == MapEnum.TerrainType.UNLOCKED_EXIT) {
            m_layer_arch = Architecture.BOSS_EXIT;
            if(type == MapEnum.TerrainType.LOCKED_EXIT) {
                m_flags_arch.add(Flag.LOCKED);
            }
        }

        // Entrance
        if(type == MapEnum.TerrainType.ENTRANCE) {
            m_layer_arch = Architecture.ENTRANCE;
        }

        // Exit
        if(type == MapEnum.TerrainType.EXIT) {
            m_layer_arch = Architecture.EXIT;
        }

        // Barricade
        if(type == MapEnum.TerrainType.BARRICADE) {
            m_layer_arch = Architecture.BARRICADE;
        }

        // Sign
        if(type == MapEnum.TerrainType.SIGN) {
            m_layer_arch = Architecture.SIGN;
        }

        // Pedestal
        if(type == MapEnum.TerrainType.PEDESTAL) {
            m_layer_arch = Architecture.PEDESTAL;
        }

        // Alchemy
        if(type == MapEnum.TerrainType.ALCHEMY) {
            m_layer_arch = Architecture.ALCHEMY;
        }

        // Well
        if(type == MapEnum.TerrainType.WELL || 
          type == MapEnum.TerrainType.EMPTY_WELL) {
            m_layer_arch = Architecture.WELL;

            if(type == MapEnum.TerrainType.EMPTY_WELL) {
                m_flags_arch.add(Flag.EMPTY);
            }
        }

        // Statue
        if(type == MapEnum.TerrainType.STATUE || 
           type == MapEnum.TerrainType.STATUE_SP) {
            m_layer_arch = Architecture.STATUE;
        }

        // Book shelf
        if(type == MapEnum.TerrainType.BOOKSHELF) {
            m_layer_arch = Architecture.BOOK_SHELF;
        }

        //......................................................................
        // Fx - only when visible
        //
        if(Utils.FlagTest(flags, MapEnum.TerrainFlags.VISIBLE.flag)) {
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_FIRE.flag)) {
                m_fx.add(Fx.FIRE);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_SACRIFICIAL_FIRE.flag)) {
                m_fx.add(Fx.FIRE_SACRIFICIAL);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_PARALYTIC_GAS.flag)) {
                m_fx.add(Fx.GAS_PARALYTIC);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_TOXIC_GAS.flag)) {
                m_fx.add(Fx.GAS_TOXIC);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_CONFUSION_GAS.flag)) {
                m_fx.add(Fx.GAS_CONFUSION);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_WEB.flag)) {
                m_fx.add(Fx.WEB);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_WATER_OF_HEALTH.flag)) {
                m_fx.add(Fx.WATER_OF_HEALTH);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_WATER_OF_AWARENESS.flag)) {
                m_fx.add(Fx.WATER_OF_AWARENESS);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_WATER_OF_TRANSMUTATION.flag)) {
                m_fx.add(Fx.WATER_OF_TRANSMUTATION);
            }
            if(Utils.FlagTest(flags, MapEnum.TerrainFlags.BLOB_ALCHEMY.flag)) {
                m_fx.add(Fx.ALCHEMY);
            }
        }
    }

    //--------------------------------------------------------------------------
    private void UpdateObjectTypes() {
        // Reset elevation
        m_cell_elevation_step = 0;

        // Ignore inactive cells
        if(!m_flags_base.contains(Flag.ACTIVE)) {
            // Cover inactive cells with fog
            m_types_cur.add(RenderableObjectType.FOG);
            return;
        }

        // Fog
        if(!m_flags_base.contains(Flag.VISIBLE)) {
            // Cover non-visible cells with fog
            m_types_cur.add(RenderableObjectType.FOG);
        }

        // Wall
        if(m_layer_base == Base.WALL) {
            m_types_cur.add(RenderableObjectType.WALL);

            // Deco on wall
            if(m_layer_material1 == Material.DECO) {
                m_types_cur.add(RenderableObjectType.DECO_WALL);

                // Water leakage is present only on sewer levels
                if(Main.inst.renderable_man.GetTerrainTexure().equals("tiles0.png") && 
                    m_flags_base.contains(Flag.VISIBLE)) {
                    m_types_cur.add(RenderableObjectType.PFX_LEAKAGE);
                    m_types_cur.add(RenderableObjectType.FX_RIPPLE_LEAKAGE);
                }
            }

        // Floor
        } else if(m_layer_base == Base.FLOOR) {
            // Stone floor
            if(m_layer_material0 == Material.STONE) {
                m_types_cur.add(RenderableObjectType.FLOOR);

            // Wooden floor
            } else if(m_layer_material0 == Material.WOOD) {
                m_types_cur.add(RenderableObjectType.EMPTY_SPACE);
            }

            // Water on floor
            if(m_layer_material1 == Material.WATER) {
                m_types_cur.add(RenderableObjectType.WATER);
            }

            // Grass on floor
            if(m_layer_material1 == Material.GRASS) {
                m_types_cur.add(RenderableObjectType.GRASS_LOW);
                if(m_flags_material1.contains(Flag.HIGH)) {
                    m_types_cur.add(RenderableObjectType.GRASS_HIGH);
                }
            }

            // Deco on floor
            if(m_layer_material1 == Material.DECO) {
                m_types_cur.add(RenderableObjectType.DECO_FLOOR);
            }

            // Embers on floor
            if(m_layer_material1 == Material.EMBERS) {
                m_types_cur.add(RenderableObjectType.EMBERS);
            }

            // Visible traps
            if(m_flags_trap.contains(Flag.VISIBLE)) {
                if(!m_flags_trap.contains(Flag.ACTIVE)) {
                    m_types_cur.add(RenderableObjectType.TRAP_INACTIVE);
                } else if(m_layer_trap == Trap.TOXIC) {
                    m_types_cur.add(RenderableObjectType.TRAP_TOXIC);
                } else if(m_layer_trap == Trap.FIRE) {
                    m_types_cur.add(RenderableObjectType.TRAP_FIRE);
                } else if(m_layer_trap == Trap.PARALYTIC) {
                    m_types_cur.add(RenderableObjectType.TRAP_PARALYTIC);
                } else if(m_layer_trap == Trap.POISON) {
                    m_types_cur.add(RenderableObjectType.TRAP_POISON);
                } else if(m_layer_trap == Trap.ALARM) {
                    m_types_cur.add(RenderableObjectType.TRAP_ALARM);
                } else if(m_layer_trap == Trap.LIGHTNING) {
                    m_types_cur.add(RenderableObjectType.TRAP_LIGHTNING);
                } else if(m_layer_trap == Trap.GRIPPING) {
                    m_types_cur.add(RenderableObjectType.TRAP_GRIPPING);
                } else if(m_layer_trap == Trap.SUMMONING) {
                    m_types_cur.add(RenderableObjectType.TRAP_SUMMONING);
                }
            }
        }

        // Exit from boss level
        if(m_layer_arch == Architecture.BOSS_EXIT) {
            if(m_flags_arch.contains(Flag.LOCKED)) {
                m_types_cur.add(RenderableObjectType.EXIT_LOCKED);
            } else {
                m_types_cur.add(RenderableObjectType.EXIT_UNLOCKED);
            }
        }

        // Door
        if(m_layer_arch == Architecture.DOOR) {
            // Select door
            if(m_flags_arch.contains(Flag.CLOSED)) {
                if(m_flags_arch.contains(Flag.LOCKED)) {
                    m_types_cur.add(RenderableObjectType.DOOR_LOCKED);
                } else {
                    m_types_cur.add(RenderableObjectType.DOOR);
                }
            } else {
                m_types_cur.add(RenderableObjectType.DOOR_OPEN);
            }

            // Frame of the door
            m_types_cur.add(RenderableObjectType.DOOR_FRAME);

        // Entrance
        } else if(m_layer_arch == Architecture.ENTRANCE) {
            m_types_cur.add(RenderableObjectType.ENTRANCE);
            m_cell_elevation_step = +1;

        // Exit
        } else if(m_layer_arch == Architecture.EXIT) {
            m_types_cur.add(RenderableObjectType.EXIT);
            m_cell_elevation_step = -1;

        // Sign
        } else if(m_layer_arch == Architecture.SIGN) {
            m_types_cur.add(RenderableObjectType.SIGN);

        // Pedestal
        } else if(m_layer_arch == Architecture.PEDESTAL) {
            m_types_cur.add(RenderableObjectType.PEDESTAL);
            m_cell_elevation_step = +1;

        // Alchemy
        } else if(m_layer_arch == Architecture.ALCHEMY) {
            m_types_cur.add(RenderableObjectType.ALCHEMY);
            m_cell_elevation_step = +1;

        // Well
        } else if(m_layer_arch == Architecture.WELL) {
            m_cell_elevation_step = +1;

            if(m_flags_arch.contains(Flag.EMPTY)) {
                m_types_cur.add(RenderableObjectType.WELL_EMPTY);
            } else {
                m_types_cur.add(RenderableObjectType.WELL);
            }

        // Statue
        } else if(m_layer_arch == Architecture.STATUE) {
            m_types_cur.add(RenderableObjectType.STATUE);

        // Book shelf
        } else if(m_layer_arch == Architecture.BOOK_SHELF) {
            m_types_cur.add(RenderableObjectType.BOOK_SHELF);

        // Barricade
        } else if(m_layer_arch == Architecture.BARRICADE) {
            m_types_cur.add(RenderableObjectType.BARRICADE);
        }

        //......................................................................
        // Fx
        if(m_fx.contains(Fx.FIRE)) {
            m_types_cur.add(RenderableObjectType.PFX_FIRE);
        }
        if(m_fx.contains(Fx.FIRE_SACRIFICIAL)) {
            m_types_cur.add(RenderableObjectType.PFX_FIRE_SACRIFICIAL);
        }
        if(m_fx.contains(Fx.GAS_PARALYTIC)) {
            m_types_cur.add(RenderableObjectType.PFX_GAS_PARALYTIC);
        }
        if(m_fx.contains(Fx.GAS_TOXIC)) {
            m_types_cur.add(RenderableObjectType.PFX_GAS_TOXIC);
        }
        if(m_fx.contains(Fx.GAS_CONFUSION)) {
            m_types_cur.add(RenderableObjectType.PFX_GAS_CONFUSING);
        }
        if(m_fx.contains(Fx.WEB)) {
            m_types_cur.add(RenderableObjectType.PFX_WEB);
        }
        if(m_fx.contains(Fx.WATER_OF_HEALTH)) {
            m_types_cur.add(RenderableObjectType.PFX_WATER_OF_HEALING);
        }
        if(m_fx.contains(Fx.WATER_OF_AWARENESS)) {
            m_types_cur.add(RenderableObjectType.PFX_WATER_OF_AWARENESS);
        }
        if(m_fx.contains(Fx.WATER_OF_TRANSMUTATION)) {
            m_types_cur.add(RenderableObjectType.PFX_WATER_OF_TRANSMUTATION);
        }
        if(m_fx.contains(Fx.ALCHEMY)) {
            m_types_cur.add(RenderableObjectType.PFX_ALCHEMY);
        }
    }

    //--------------------------------------------------------------------------
    public void UpdatePackedTiles() {
        // Tile packing can be done only if animation is over
        if(!IsStill() || IsFading()) {
            return;
        }

        // Pack all pending objects if their animation is over
        Iterator<RenderableObject> it = m_pack_pending.iterator();
        while(it.hasNext()) {
            RenderableObject robj = it.next();
            if(!robj.IsStill() || robj.IsFading()) {
                continue;
            }

            // Object can finally be packed
            Main.inst.level.OnToggleObject(robj, true);
            it.remove();
        }
    }

    //--------------------------------------------------------------------------
    public boolean IsDoorFrameXAligned() {
        // Get owner cell
        LevelObjectCell cell = (LevelObjectCell)SgGetParent();
        Utils.Assert(cell != null, 
          "Failed to get alignment of door-frame, no parent cell :: terrain=%s", 
          SgGetNameId());

        // Get cell object to the left
        LevelObjectTerrain left_terrain = 
          cell.GetNeighborTerrain(LevelDirection.WEST);

        // Object is considered to be aligned to wall if it is surrounded by 
        // walls from left and right sides
        return (left_terrain != null) ? 
          (left_terrain.m_layer_base == Base.WALL) : false;
    }

    //--------------------------------------------------------------------------
    public Alignment GetObjectAlignment(RenderableObjectType obj_type) {
        // Get owner cell
        LevelObjectCell cell = (LevelObjectCell)SgGetParent();
        Utils.Assert(cell != null, 
          "Failed to get object alignment, no parent cell :: terrain=%s", 
          SgGetNameId());

        // Get terrain of neighbor cells
        LevelObjectTerrain west_terr = cell.GetNeighborTerrain(LevelDirection.WEST);
        LevelObjectTerrain east_terr = cell.GetNeighborTerrain(LevelDirection.EAST);
        LevelObjectTerrain north_terr = cell.GetNeighborTerrain(LevelDirection.NORTH);
        LevelObjectTerrain south_terr = cell.GetNeighborTerrain(LevelDirection.SOUTH);

        // Should not happen
        if(west_terr == null || east_terr == null || 
          north_terr == null || south_terr == null) {
            return Alignment.NONE;
        }

        // Check presence of same object around current cell
        boolean west = west_terr.HasObject(obj_type);
        boolean east = east_terr.HasObject(obj_type);
        boolean north = north_terr.HasObject(obj_type);
        boolean south = south_terr.HasObject(obj_type);

        // Multiple objects are X-aligned
        if(west || east) {
            return Alignment.X_ALIGNED;
        }

        // Multiple models are Z-aligned
        if(north || south) {
            return Alignment.Z_ALIGNED;
        }

        // Check presence of wall around current cell
        west = !west_terr.IsPassable() || west_terr.HasObject(RenderableObjectType.DOOR);
        east = !east_terr.IsPassable() || east_terr.HasObject(RenderableObjectType.DOOR);
        north = !north_terr.IsPassable() || north_terr.HasObject(RenderableObjectType.DOOR);
        south = !south_terr.IsPassable() || south_terr.HasObject(RenderableObjectType.DOOR);

        // X aligned
        if((north || south) && (!west && !east)) {
            return Alignment.X_ALIGNED;
        }

        // Z aligned
        if((west || east) && (!north && !south)) {
            return Alignment.Z_ALIGNED;
        }

        // ZX corner
        if((west && north) || (east && south)) {
            return Alignment.CORNER_ZX;
        }

        // XZ corner
        if((west && south) || (east && north)) {
            return Alignment.CORNER_XZ;
        }
        return Alignment.NONE;
    }

    //--------------------------------------------------------------------------
    public boolean IsPassable() {
        return m_flags_base.contains(Flag.PASSABLE);
    }

    //--------------------------------------------------------------------------
    public boolean IsTrap() {
        return (m_layer_trap != null);
    }

    //--------------------------------------------------------------------------
    public boolean IsChasm() {
        return (m_layer_base == null);
    }

    //--------------------------------------------------------------------------
    public boolean HasDoor(int type) {
        // Check if door
        if(m_layer_arch != Architecture.DOOR && 
           m_layer_arch != Architecture.BOSS_EXIT) {
            return false;
        }

        switch(type) {
        // Check for closed door
        case 0: {
            return m_flags_arch.contains(Flag.CLOSED);
        }

        // Check for open door
        case 1: {
            return !m_flags_arch.contains(Flag.CLOSED);
        }

        // Check for locked door
        case 2: {
            return m_flags_arch.contains(Flag.LOCKED);
        }

        // Check for any door
        default: {
            return true;
        }}
    }

    //--------------------------------------------------------------------------
    public boolean HasWell() {
        return m_layer_arch == Architecture.WELL;
    }

    //--------------------------------------------------------------------------
    public boolean HasWater() {
        return m_layer_material1 == Material.WATER;
    }

    //--------------------------------------------------------------------------
    public boolean HasObject(RenderableObjectType obj_type) {
        return m_types_cur.contains(obj_type);
    }

    //--------------------------------------------------------------------------
    private void ResetDistanceToOrigin(LevelObjectCell origin_cell, 
      float distance_limit) {
        m_cache_origin_cell = origin_cell;
        m_cache_distance_limit = distance_limit;
        m_cache_distance_to_origin = -1.0f;
    }

    //--------------------------------------------------------------------------
    private float GetElevationOffset(int step) {
        return Main.inst.cfg.lvl_cell_elevation * step;
    }

    //--------------------------------------------------------------------------
    private float GetDistanceToOrigin() {
        // Returned cached value
        if(m_cache_distance_to_origin >= 0.0f) {
            return m_cache_distance_to_origin;
        }

        //
        // Recalculate distance to origin
        //

        // Check origin cell
        if(m_cache_origin_cell == null) {
            return 0.0f;
        }

        // Get owner cell
        LevelObjectCell cell = (LevelObjectCell)SgGetParent();
        if(cell == null) {
            return 0.0f;
        }

        // Calculate absolute distance to origin cell
        float dist2 = m_cache_origin_cell.GetDistanceToTarget(cell, false);

        // Apply distance limit
        if(dist2 > m_cache_distance_limit) {
            dist2 = m_cache_distance_limit;
        }

        // Calculate relative distance to origin in [0.0f; 1.0f] range
        m_cache_distance_to_origin = dist2 / m_cache_distance_limit;
        return m_cache_distance_to_origin;
    }

    //--------------------------------------------------------------------------
    private RenderableObject CreateObject(RenderableObjectType type) {
        // Create new terrain object
        RenderableObject robj = (RenderableObject)SgAddChild(type.Create());
        ITemplate t = robj.GetTemplate();

        // By default don't do fade-in
        boolean do_fade_in = false;
        float fade_in_duration_factor = 1.0f;

        //..................................................................
        // Pfx
        if(robj.GetRendererLayer() == Renderer.Layer.PARTICLE) {
            // XXX

        //..................................................................
        // Door
        } else if(type == RenderableObjectType.DOOR || 
          type == RenderableObjectType.DOOR_OPEN || 
          type == RenderableObjectType.DOOR_FRAME || 
          type == RenderableObjectType.DOOR_LOCKED) {
            // Door needs to be rotated if it is not aligned to wall
            if(m_rotate_door == null) {
                m_rotate_door = !IsDoorFrameXAligned();
            }

            // Rotate door by 90 degrees around Y-axis
            if(m_rotate_door) {
                robj.GetLocalRot(true)
                  .mul(new Quaternion(Vector3.Y, 90.0f));
            }

            // Do fade-in for doors
            do_fade_in = true;

        //..................................................................
        // Vertical objects
        } else if(t instanceof MTmplSpriteVertical) {
            float rotation = GetObjectAlignment(type).rotation + 15.0f;
            robj.GetLocalRot(true)
              .mul(new Quaternion(Vector3.Y, rotation));

        //..................................................................
        // Elevation is not meant for floor tiles, that's why we need to move 
        // floor in opposite direction so that it is always on ground level
        } else if(m_cell_elevation_step != 0 && 
          (type == RenderableObjectType.FLOOR || type == RenderableObjectType.EMPTY_SPACE)) {
            robj.GetLocalPos(true).y = -GetElevationOffset(m_cell_elevation_step);
        }

        // Fade in
        if(do_fade_in) {
            robj.FadeIn(fade_in_duration_factor);
        }

        // Link object to packed-tile and make sure that fading objects
        // are auto-excluded from packed-tile
        robj.SetPackedTile(
          Main.inst.level.GetPackedTile(type), GetPdId(), do_fade_in);
        return robj;
    }

    //--------------------------------------------------------------------------
    private void OnToggleObject(RenderableObject robj, boolean is_created) {
        // Ignore object that can not be packed
        if(!robj.HasPackedTile()) {
            return;
        }

        // When new object is being created then we should notify level only when 
        // animation is over
        if(is_created) {
            if(Main.inst.cfg.terrain_smooth_transition_enable) {
                m_pack_pending.add(robj);
            } else {
                Main.inst.level.OnToggleObject(robj, true);
            }

        // When old object is being deleted then we should notify level instantly
        } else {
            Main.inst.level.OnToggleObject(robj, false);
            m_pack_pending.remove(robj);
        }
    }

    //**************************************************************************
    // LevelObject
    //**************************************************************************
    @Override public void OnDelete(LevelTrashBin trash_bin) {
        Iterator<SgNode> it = SgGetChildren();
        while(it.hasNext()) {
            OnToggleObject((RenderableObject)it.next(), false);
        }
        m_pack_pending.clear();

        super.OnDelete(trash_bin);
    }

    //--------------------------------------------------------------------------
    @Override public void OnPreUpdate(IUpdateCtx ctx, LevelTrashBin trash) {
        // Do not update unless dirty
        if(ctx.IsDirty()) {
            // Reset layers
            m_layer_base      = null;
            m_layer_material0 = null;
            m_layer_material1 = null;
            m_layer_arch      = null;
            m_layer_trap      = null;

            // Reset layer flags
            m_flags_base      = EnumSet.noneOf(Flag.class);
//            m_flags_material0 = EnumSet.noneOf(Flag.class);
            m_flags_material1 = EnumSet.noneOf(Flag.class);
            m_flags_arch      = EnumSet.noneOf(Flag.class);
            m_flags_trap      = EnumSet.noneOf(Flag.class);

            // Reset fx
            m_fx = EnumSet.noneOf(Fx.class);

            // Reset types
            m_types_old = EnumSet.copyOf(m_types_cur);
            m_types_cur = EnumSet.noneOf(RenderableObjectType.class);

            // Update layers of the cell
            UpdateLayers(((UpdateCtx)ctx).type, ((UpdateCtx)ctx).flags);

            // Update types of the cell
            UpdateObjectTypes();
        }

        // Pack pending tiles
        UpdatePackedTiles();

        // Update terrain in minimap
        for(RenderableObjectType type : m_types_cur) {
            Main.inst.level.GetMinimap()
              .UpdateTerrain(ctx.GetCellId(), type, 0);
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnUpdate(IUpdateCtx ctx, LevelTrashBin trash_bin) {
        super.OnUpdate(ctx, trash_bin);

        // Test if anything has changed
        if(!ctx.IsDirty() || m_types_cur.equals(m_types_old)) {
            return;
        }

        //......................................................................
        //
        // Update elevation of the cell
        //
        LevelObjectCell parent = 
          ((LevelObjectCell)SgGetParent(LevelObjectCell.class));

        // Disable elevation
        if(m_cell_elevation_step == 0 && parent.HasElevation()) {
            parent.UnsetElevation();

        // Update elevation
        } else if(m_cell_elevation_step != m_cell_elevation_step_old) {
            parent.SetElevation(
              GetElevationOffset(m_cell_elevation_step));
        }
        m_cell_elevation_step_old = m_cell_elevation_step;

        // Reset distance to origin
        ResetDistanceToOrigin(((UpdateCtx)ctx).origin_cell, 
          ((UpdateCtx)ctx).map_size2 * 0.1f);

        //......................................................................
        //
        // Update fog-of-war
        //
        boolean fog_was_present = m_types_old.contains(RenderableObjectType.FOG);
        boolean fog_is_present = m_types_cur.contains(RenderableObjectType.FOG);
        boolean was_empty_cell = (m_types_old.size() == 0 || 
          (m_types_old.size() == 1 && m_types_old.contains(RenderableObjectType.FOG)));

        // Fog appeared on cell
        if((was_empty_cell || !fog_was_present) && fog_is_present) {
            Main.inst.level.UpdateFow(GetPdId(), GetDistanceToOrigin(), true);

        // Fog disappeared from cell
        } else if((was_empty_cell || fog_was_present) && !fog_is_present) {
            Main.inst.level.UpdateFow(GetPdId(), GetDistanceToOrigin(), false);
        }

        //......................................................................
        //
        // Run terrain's appear animation
        //

        // If there were no models on this terrain then start appear animation and
        // slowly move newly created terrain object from below
        if(was_empty_cell && Main.inst.cfg.terrain_smooth_transition_enable) {
            GetLocalPos().set(Main.inst.cfg.terrain_move_from);
            TweenPos(Main.inst.cfg.terrain_move_to, 
              Main.inst.cfg.terrain_move_duration * GetDistanceToOrigin(), null);
        }

        //......................................................................
        //
        // Manage old objects

        // Get list of old object
        LinkedList<RenderableObject> old_robjs = new LinkedList<RenderableObject>();
        Iterator<SgNode> it = SgGetChildren();
        while(it.hasNext()) {
            RenderableObject robj = (RenderableObject)it.next();
            RenderableObjectType type = robj.GetObjectType();
            if(!m_types_cur.contains(type)) {
                old_robjs.add(robj);
            }
        }

        // Get rid of old objects
        for(RenderableObject robj : old_robjs) {
            trash_bin.Put(robj, false);
            OnToggleObject(robj, false);
        }

        //......................................................................
        //
        // Manage new objects
        //

        // Reset door rotation 
        m_rotate_door = null;

        // Create new models
        for(RenderableObjectType type : m_types_cur) {
            // Ignore fog
            if(type == RenderableObjectType.FOG) {
                continue;
            }

            // Ignore model if it was already present
            if(m_types_old.contains(type)) {
                continue;
            }

            // Create new object
            OnToggleObject(CreateObject(type), true);
        }
    }
}
