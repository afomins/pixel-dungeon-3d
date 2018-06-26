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
import java.util.Collection;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.matalok.pd3d.TextRenderer;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformUtils;
import com.matalok.pd3d.desc.*;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.map.MapEnum.TerrainType;
import com.matalok.pd3d.renderable.RenderableObject.ITemplate;
import com.matalok.pd3d.renderable.RenderableObject.ITemplateBuilder;
import com.matalok.pd3d.renderable.billboard.BillboardEmotionTmpl;
import com.matalok.pd3d.renderable.billboard.BillboardHpTmpl;
import com.matalok.pd3d.renderable.billboard.BillboardTmpl;
import com.matalok.pd3d.renderable.model.template.*;
import com.matalok.pd3d.renderable.particle.ParticleTmpl;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.Renderer.TxCache;
import com.matalok.pd3d.renderer.RendererTexture;
import com.matalok.pd3d.shared.GsonUtils;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgNode;

//------------------------------------------------------------------------------
public class RenderableMan 
  extends GameNode {
    //**************************************************************************
    // STATIC
    //**************************************************************************

    // Traps
    private static final HashMap<MapEnum.TerrainType, RenderableObjectType> traps;
    static {
        traps = new HashMap<MapEnum.TerrainType, RenderableObjectType>();
        traps.put(MapEnum.TerrainType.TOXIC_TRAP,       RenderableObjectType.TRAP_TOXIC);
        traps.put(MapEnum.TerrainType.FIRE_TRAP,        RenderableObjectType.TRAP_FIRE);
        traps.put(MapEnum.TerrainType.PARALYTIC_TRAP,   RenderableObjectType.TRAP_PARALYTIC);
        traps.put(MapEnum.TerrainType.INACTIVE_TRAP,    RenderableObjectType.TRAP_INACTIVE);
        traps.put(MapEnum.TerrainType.POISON_TRAP,      RenderableObjectType.TRAP_POISON);
        traps.put(MapEnum.TerrainType.ALARM_TRAP,       RenderableObjectType.TRAP_ALARM);
        traps.put(MapEnum.TerrainType.LIGHTNING_TRAP,   RenderableObjectType.TRAP_LIGHTNING);
        traps.put(MapEnum.TerrainType.GRIPPING_TRAP,    RenderableObjectType.TRAP_GRIPPING);
        traps.put(MapEnum.TerrainType.SUMMONING_TRAP,   RenderableObjectType.TRAP_SUMMONING);
    }

    //**************************************************************************
    // ModelMan
    //**************************************************************************
    private HashMap<String, DescSprite[]> m_sprite_map;
    private HashMap<String, Integer> m_billboard_text_id_map;
    private HashMap<DescSprite, Integer> m_billboard_sprite_id_map;
    private HashMap<DescSprite, Integer> m_billboard_emotion_id_map;
    private String m_terrain_texture;

    //--------------------------------------------------------------------------
    public RenderableMan() {
        super("renderable-manager", 1.0f);

        // Init sprite descriptor map 
        m_sprite_map = new HashMap<String, DescSprite[]>();
        m_sprite_map.put(MapEnum.CharType.name,          new DescSprite[MapEnum.CharType.GetSize()]);
        m_sprite_map.put(MapEnum.ItemType.name,          new DescSprite[MapEnum.ItemType.GetSize()]);
        m_sprite_map.put(MapEnum.PlantType.name,         new DescSprite[MapEnum.PlantType.GetSize()]);
        m_sprite_map.put(MapEnum.TerrainType.name,       new DescSprite[MapEnum.TerrainType.GetSize()]);
        m_sprite_map.put(MapEnum.IconType.name,          new DescSprite[MapEnum.IconType.GetSize()]);
        m_sprite_map.put(MapEnum.BannerType.name,        new DescSprite[MapEnum.BannerType.GetSize()]);
        m_sprite_map.put(MapEnum.DashboardItemType.name, new DescSprite[MapEnum.DashboardItemType.GetSize()]);
        m_sprite_map.put(MapEnum.AvatarType.name,        new DescSprite[MapEnum.AvatarType.GetSize()]);
        m_sprite_map.put(MapEnum.ToolbarType.name,       new DescSprite[MapEnum.ToolbarType.GetSize()]);
        m_sprite_map.put(MapEnum.StatusPaneType.name,    new DescSprite[MapEnum.StatusPaneType.GetSize()]);
        m_sprite_map.put(MapEnum.BuffType.name,          new DescSprite[MapEnum.BuffType.GetSize()]);
        m_sprite_map.put(MapEnum.PfxImage.name,          new DescSprite[MapEnum.PfxImage.GetSize()]);
        m_sprite_map.put("water",                        new DescSprite[1]);

        // Billboard
        m_billboard_text_id_map = new HashMap<String, Integer>();
        m_billboard_sprite_id_map = new HashMap<DescSprite, Integer>();
        m_billboard_emotion_id_map = new HashMap<DescSprite, Integer>();

        // Create static templates
        UpdateModelTemplates();

        // Create pfx templates
        UpdatePfxTemplates();

        // Load billboard font
        Main.inst.text_renderer.Add(
          "billboard", Main.inst.cfg.billboard_font);
    }

    //--------------------------------------------------------------------------
    public String GetTerrainTexure() {
        return m_terrain_texture;
    }

    //--------------------------------------------------------------------------
    private boolean SaveSprite(DescSprite desc) {
        // Check sprite's object type
        if(!m_sprite_map.containsKey(desc.obj_type)) {
            return false;
        }

        // Check sprite's object id
        DescSprite desc_array[] = m_sprite_map.get(desc.obj_type);
        if(desc.obj_id < 0 || desc.obj_id >= desc_array.length) {
            return false;
        }
        desc_array[desc.obj_id] = desc;
        return true;
    }

    //--------------------------------------------------------------------------
    public DescSprite GetSprite(Enum<?> e) {
        return GetSprite(MapEnum.GetNameByType(e), e.ordinal());
    }

    //--------------------------------------------------------------------------
    public DescSprite GetSprite(String type, Enum<?> e) {
        return GetSprite(type, e.ordinal());
    }

    //--------------------------------------------------------------------------
    public DescSprite GetSprite(String type, int id) {
        // Check sprite's object type
        Utils.Assert(m_sprite_map.containsKey(type), 
          "Failed to get sprite, wrong type :: type=%s id=%d", type, id);

        // Check sprite's object id
        DescSprite desc_array[] = m_sprite_map.get(type);
        Utils.Assert(id >= 0 && id < desc_array.length, 
          "Failed to get sprite, wrong id :: type=%s id=%d", type, id);
        return desc_array[id];
    }

    //--------------------------------------------------------------------------
    public TextureRegion GetTextureRegion(Enum<?> e, String tx_key) {
        return GetTextureRegion(MapEnum.GetNameByType(e), e.ordinal(), tx_key);
    }

    //--------------------------------------------------------------------------
    public TextureRegion GetTextureRegion(String sprite_type, int sprite_id, 
      String tx_key) {
        DescSprite sprite = GetSprite(sprite_type, sprite_id);
        String tx_cache_key = GetTxCacheKey(sprite);
        RendererTexture tx = UpdateSpriteTexture(sprite, tx_cache_key, false, false);
        return tx.GetTxRegion(sprite, "idle");
    }

    //--------------------------------------------------------------------------
    private boolean IsNewTexture(DescSprite sprite, String tx_cache_key) {
        // Check if texture is already in cache
        Renderer.TxCache tx_cache = Main.inst.renderer.GetTxCache();
        RendererTexture texture = tx_cache.Get(tx_cache_key);
        return (texture == null || !texture.name.equals(sprite.texture));
    }

    //--------------------------------------------------------------------------
    private String GetTxCacheKey(DescSprite sprite) {
        String sprite_type = sprite.obj_type;
        return
          sprite_type.equals("terrain") ? "terrain-tilemap" : 
          sprite_type.equals("water") ?   "water-tilemap" :
          sprite.texture;
    }

    //--------------------------------------------------------------------------
    private RendererTexture UpdateSpriteTexture(DescSprite sprite, String tx_cache_key, 
      boolean do_repeat, boolean make_shadow) {
        // Check if texture is already in cache
        Renderer.TxCache tx_cache = Main.inst.renderer.GetTxCache();
        RendererTexture texture = tx_cache.Get(tx_cache_key);
        if(texture == null || !texture.name.equals(sprite.texture)) {
            // Read original pixmap
            Pixmap pm_orig = new Pixmap(
              PlatformUtils.OpenInternalFile(sprite.texture, true));

            // Create pixmap with shadow
            Pixmap pm_with_shadow = null;
            if(make_shadow) {
                pm_with_shadow = RendererTexture.CreateShadowPixmap(pm_orig);
            }

            // Remove old texture from cache
            if(texture != null) {
                tx_cache.Delete(tx_cache_key, true);
            }

            // Create texture from pixmap and cache it
            texture = tx_cache.Put(tx_cache_key, 
              new RendererTexture(sprite.texture, 
                new Texture(make_shadow ? pm_with_shadow : pm_orig), 
                  do_repeat));

            // Dispose pixmap
            pm_orig.dispose();
            if(pm_with_shadow != null) {
                pm_with_shadow.dispose();
            }
        }

        // Finalize rects of the sprite
        sprite.FinalizeRects(texture.tx.getWidth(), texture.tx.getHeight());
        return texture;
    }

    //--------------------------------------------------------------------------
    public boolean UpdateSprite(DescSprite sprite) {
        // Save sprite descriptor
        if(!SaveSprite(sprite)) {
            Logger.e("Failed to update sprite, desc error :: type=%s id=%d", 
              sprite.obj_type, sprite.obj_id);
            return false;
        }

        // Update sprite
        switch(sprite.obj_type) {
        //......................................................................
        case "char": {
            UpdateCharTemplates(sprite);
        } break;

        //......................................................................
        case "item": {
            UpdateItemTemplates(sprite);
        } break;

        //......................................................................
        case "plant": {
            UpdatePlantTemplates(sprite);
        } break;

        //......................................................................
        case "terrain": {
            UpdateTerrainTemplates(sprite);
        } break;

        //......................................................................
        case "water": {
            UpdateWaterTemplates(sprite);
        } break;

        //......................................................................
        case "icon": {
            UpdateIconTemplates(sprite);
        } break;

        //......................................................................
        case "pfx-image": {
            UpdatePfxImageTemplates(sprite);
        } break;

        //......................................................................
        case "banner":
        case "dashboard-item":
        case "avatar":
        case "toolbar":
        case "status-pane":
        case "buff": {
            // No model
        } break;

        //......................................................................
        default: {
            Logger.e("Failed to update sprite, unknown type :: type=%s", 
              sprite.obj_type);
            return false;
        }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    private void UpdateCharTemplates(final DescSprite sprite) {
        // Update texture
        final String tx_cache_key = GetTxCacheKey(sprite);
        final UtilsClass.Callback update_sprite_texture = new UtilsClass.Callback() {
            @Override public Object Run(Object... args) {
                return UpdateSpriteTexture(sprite, tx_cache_key, false, true);
            }
        };

        // Char
        RenderableObjectType.CHAR.AddTemplateBuilder(sprite.obj_id,
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                update_sprite_texture.Run();
                return new MTmplSpriteChar(tx_cache_key, sprite).Build();
        }});
    }

    //--------------------------------------------------------------------------
    private void UpdateItemTemplates(final DescSprite sprite) {
        // Update sprite texture now
        final String tx_cache_key = GetTxCacheKey(sprite);
        UpdateSpriteTexture(sprite, tx_cache_key, false, true);

        // Create templates
        switch(MapEnum.ItemType.Get(sprite.obj_id)) {
        //..................................................................
        // Chests
        case CHEST:
        case LOCKED_CHEST:
        case CRYSTAL_CHEST: {
            RenderableObjectType.ITEM.AddTemplateBuilder(sprite.obj_id,
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteChest(tx_cache_key, sprite).Build();
            }});
        } break;

        //..................................................................
        // Tomb
        case TOMB: {
            RenderableObjectType.ITEM.AddTemplateBuilder(sprite.obj_id,
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteVertical(tx_cache_key, sprite).Build();
            }});
        } break;

        //..................................................................
        // Laying items
        default: {
            RenderableObjectType.ITEM.AddTemplateBuilder(sprite.obj_id,
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteHorizontal(tx_cache_key, sprite).Build();
            }});
        }}
    }

    //--------------------------------------------------------------------------
    private void UpdatePlantTemplates(final DescSprite sprite) {
        // Update sprite texture now
        final String tx_cache_key = GetTxCacheKey(sprite);
        UpdateSpriteTexture(sprite, tx_cache_key, false, true);

        //......................................................................
        // High grass
        RenderableObjectType.PLANT.AddTemplateBuilder(sprite.obj_id,
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplSpritePlant(tx_cache_key, sprite).Build();
        }});
    }

    //--------------------------------------------------------------------------
    private void UpdateTerrainTemplates(final DescSprite sprite) {
        // Update texture of packed tiles
        final String tx_cache_key = GetTxCacheKey(sprite);
        if(IsNewTexture(sprite, tx_cache_key)) {
            Main.inst.level.UpdatePackedTileSprite();
            m_terrain_texture = sprite.texture;
        }

        // Update sprite texture now
        UpdateSpriteTexture(sprite, tx_cache_key, false, true);

        TerrainType terrain_type = MapEnum.TerrainType.Get(sprite.obj_id);
        switch(terrain_type) {
        //......................................................................
        // Floor
        case EMPTY: {
            RenderableObjectType.FLOOR.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteFloor(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Wall
        case WALL: {
            RenderableObjectType.WALL.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteWall(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Low grass
        case GRASS: {
            RenderableObjectType.GRASS_LOW.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteGrass(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Embers
        case EMBERS: {
            RenderableObjectType.EMBERS.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteGrass(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Traps
        case TOXIC_TRAP:
        case FIRE_TRAP:
        case PARALYTIC_TRAP:
        case INACTIVE_TRAP: 
        case POISON_TRAP: 
        case ALARM_TRAP: 
        case LIGHTNING_TRAP:
        case GRIPPING_TRAP:
        case SUMMONING_TRAP: {
            RenderableObjectType trap = RenderableMan.traps.get(terrain_type);
            Utils.Assert(trap != null, "Failed to get type of trap, terrain-type=%s", 
              terrain_type.toString());

            trap.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteGrass(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Floor deco
        case EMPTY_DECO: {
            RenderableObjectType.DECO_FLOOR.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteGrass(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Wall deco
        case WALL_DECO: {
            RenderableObjectType.DECO_WALL.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteWallDeco(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // High grass
        case HIGH_GRASS: {
            RenderableObjectType.GRASS_HIGH.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteGrassHigh(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Empty space
        case EMPTY_SP: {
            RenderableObjectType.EMPTY_SPACE.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteFloor(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Pedestal
        case PEDESTAL: {
            RenderableObjectType.PEDESTAL.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpritePedestal(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Alchemy
        case ALCHEMY: {
            RenderableObjectType.ALCHEMY.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpritePedestal(tx_cache_key, sprite).Build();
            }});
        } break;


        //......................................................................
        // Well
        case WELL: {
            RenderableObjectType.WELL.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpritePedestal(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Empty well
        case EMPTY_WELL: {
            RenderableObjectType.WELL_EMPTY.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpritePedestal(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Open & closed doors
        case DOOR: {
            RenderableObjectType.DOOR.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteDoor(tx_cache_key, sprite, false).Build();
            }});

            RenderableObjectType.DOOR_OPEN.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteDoor(tx_cache_key, sprite, true).Build();
            }});
        } break;

        //......................................................................
        // Door frame
        case OPEN_DOOR: {
            RenderableObjectType.DOOR_FRAME.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteDoor(tx_cache_key, sprite, false).Build();
            }});
        } break;

        //......................................................................
        // Locked door
        case LOCKED_DOOR: {
            RenderableObjectType.DOOR_LOCKED.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteDoor(tx_cache_key, sprite, false).Build();
            }});
        } break;

        //......................................................................
        // Entrance
        case ENTRANCE: {
            RenderableObjectType.ENTRANCE.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteEntrance(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Exit
        case EXIT: {
            RenderableObjectType.EXIT.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteExit(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Sign
        case SIGN: {
            RenderableObjectType.SIGN.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteVertical(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Barricade
        case BARRICADE: {
            RenderableObjectType.BARRICADE.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteGrassHigh(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Statue
        case STATUE: {
            RenderableObjectType.STATUE.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteStatue(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Bookshelf
        case BOOKSHELF: {
            RenderableObjectType.BOOK_SHELF.AddTemplateBuilder(
              new ITemplateBuilder() {
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteBookshelf(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Locked exit
        case LOCKED_EXIT: {
            RenderableObjectType.EXIT_LOCKED.AddTemplateBuilder(
              new ITemplateBuilder() {
                  @Override public RenderableTemplate Build() {
                      return new MTmplSpriteWallDeco(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        // Unlocked exit
        case UNLOCKED_EXIT: {
            RenderableObjectType.EXIT_UNLOCKED.AddTemplateBuilder(
              new ITemplateBuilder() {
                  @Override public RenderableTemplate Build() {
                      return new MTmplSpriteWallDeco(tx_cache_key, sprite).Build();
            }});
        } break;

        //......................................................................
        default:
        }
    }

    //--------------------------------------------------------------------------
    private void UpdateWaterTemplates(final DescSprite sprite) {
        // Update texture of packed tiles
        final String tx_cache_key = GetTxCacheKey(sprite);
        if(IsNewTexture(sprite, tx_cache_key)) {
            Main.inst.level.UpdatePackedTileSprite();
        }

        // Update sprite texture now
        UpdateSpriteTexture(sprite, tx_cache_key, true, false);

        // Water
        RenderableObjectType.WATER.AddTemplateBuilder(
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplSpriteWater(tx_cache_key, sprite).Build();
        }});
    }

    //--------------------------------------------------------------------------
    private void UpdateIconTemplates(final DescSprite sprite) {
        // Update sprite texture now
        final String tx_cache_key = GetTxCacheKey(sprite);
        UpdateSpriteTexture(sprite, tx_cache_key, false, true);

        // Create templates
        switch(MapEnum.IconType.Get(sprite.obj_id)) {
        //......................................................................
        // Target icon
        case TARGET: {
            RenderableObjectType.TARGET.AddTemplateBuilder(
              new ITemplateBuilder(){
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteTarget(tx_cache_key, sprite).Build();
                }});
        } break;

        //......................................................................
        // Default
        default: {}
        }
    }

    //--------------------------------------------------------------------------
    private void UpdatePfxImageTemplates(final DescSprite sprite) {
        // Update sprite texture now
        final String tx_cache_key = GetTxCacheKey(sprite);
        UpdateSpriteTexture(sprite, tx_cache_key, false, false);

        // Create templates
        switch(MapEnum.PfxImage.Get(sprite.obj_id)) {
        //......................................................................
        // HP bar
        case HP: {
            int step_num = Main.inst.cfg.hp_step_num;

            // HP ranges
            DescRect rect = sprite.rects.get(sprite.obj_id);
            final float u = rect.unit_x;
            final float u_size = rect.unit_width;
            final float v_size = rect.unit_height / (step_num + 2);
            for(int i = 0; i < step_num + 2; i++) {
                final float v = rect.unit_y + i * v_size;
                RenderableObjectType.BILLBOARD_HP.AddTemplateBuilder(i, 
                  new RenderableObject.ITemplateBuilder() {
                    @Override public ITemplate Build() {
                        return new BillboardHpTmpl(
                          MapEnum.PfxImage.HP, tx_cache_key, tx_cache_key, 
                          Color.WHITE, u, v, u_size, v_size);
                    }
                });
            }
        } break;

        //......................................................................
        // SHADOW
        case SHADOW: {
            RenderableObjectType.SHADOW.AddTemplateBuilder(
              new ITemplateBuilder(){
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteShadow(tx_cache_key, sprite).Build();
                }});
        } break;

        //......................................................................
        // FX_RIPPLE
        case FX_RIPPLE: {
            RenderableObjectType.FX_RIPPLE.AddTemplateBuilder(
              new ITemplateBuilder(){
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteRipple(tx_cache_key, sprite).Build();
                }});
            RenderableObjectType.FX_RIPPLE_LEAKAGE.AddTemplateBuilder(
              new ITemplateBuilder(){
                @Override public RenderableTemplate Build() {
                    return new MTmplSpriteLeakageRipples(tx_cache_key, sprite).Build();
                }});
        } break;

        //......................................................................
        // Default
        default: {}
        }
    }

    //--------------------------------------------------------------------------
    public int GetHpTemplateId(float hp) {
        int step_num = Main.inst.cfg.hp_step_num;
        float range_step = (1.0f / (step_num));
        return 
          (hp <= 0.0f) ? 0 :
          (hp >= 1.0f) ? step_num + 1 :
          (int)(hp / range_step) + 1;
    }

    //--------------------------------------------------------------------------
    private void UpdateModelTemplates() {
        //......................................................................
        // MARKER_SELECT
        RenderableObjectType.MARKER_SELECT.AddTemplateBuilder(
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplBoxEx("marker-select", true, null, 
                  Color.YELLOW, 1.1f, Main.inst.cfg.model_marker_alpha);
        }});

        //......................................................................
        // MARKER_SEARCH
        RenderableObjectType.MARKER_SEARCH.AddTemplateBuilder(
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplBox("marker-search", true, null, 0.9f, 0.5f, 0.9f,
                  Color.YELLOW, Main.inst.cfg.model_marker_alpha);
        }});

        //......................................................................
        // MARKER_BLOOD
        RenderableObjectType.MARKER_BLOOD.AddTemplateBuilder(
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplBoxEx("marker-blood", true, null,
                  Color.RED, 1.0f, Main.inst.cfg.model_blood_alpha);
        }});

        //......................................................................
        // AXIS
        RenderableObjectType.AXIS.AddTemplateBuilder(
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplAxis("axis");
        }});

        //......................................................................
        // WIRE_BOX
        RenderableObjectType.WIRE_BOX.AddTemplateBuilder(
          new ITemplateBuilder() {
            @Override public RenderableTemplate Build() {
                return new MTmplBoxEx("wire-box", false, null, 
                  Color.WHITE, 1.0f, null);
        }});
    }

    //--------------------------------------------------------------------------
    private void UpdatePfxTemplates() {
        // Helper
        UtilsClass.Callback pfx_helper = new UtilsClass.Callback() {
            @Override public Object Run(Object... args) {
                final RenderableObjectType robj_type = (RenderableObjectType)args[0];
                final String particle_path = "particles/" + (String)args[1];
                final DescPfxMutator mutator = 
                  new DescPfxMutator((DescPfxMutator)args[2]);

                robj_type.AddTemplateBuilder(new ITemplateBuilder() {
                    @Override public ITemplate Build() {
                        return new ParticleTmpl(robj_type.toString().toLowerCase(), 
                          particle_path, mutator);
                    }
                });
                return null;
            }
        };

        //......................................................................
        // Fire
        DescPfxMutator m = new DescPfxMutator();
        m.Set(DescPfxMutator.Field.OFFSET, null, 0.4f, null);
        pfx_helper.Run(RenderableObjectType.PFX_FIRE, "fire.pfx3", 
          m.Set(DescPfxMutator.Field.COLORS, 0xFFF40C00, 0xFF000000));
        pfx_helper.Run(RenderableObjectType.PFX_FIRE_SACRIFICIAL, "fire.pfx3",
          m.Set(DescPfxMutator.Field.COLORS, 0xFF0CFF00, 0x5400DF00));

        //......................................................................
        // Gas
        m.Reset().Set(DescPfxMutator.Field.OFFSET, null, 0.5f, null);
        pfx_helper.Run(RenderableObjectType.PFX_GAS_TOXIC, "gas.pfx3", 
          m.Set(DescPfxMutator.Field.COLORS, 0xFFFFFFFF, 0x40CC5500, 0x40CC5500));
        pfx_helper.Run(RenderableObjectType.PFX_GAS_PARALYTIC, "gas.pfx3", 
          m.Set(DescPfxMutator.Field.COLORS, 0xFFFFFFFF, 0xCCCC5500, 0xCCCC5500));
        pfx_helper.Run(RenderableObjectType.PFX_GAS_CONFUSING, "gas.pfx3", 
          m.Set(DescPfxMutator.Field.COLORS, 0xB5856500, 0x0A556C00, 0xA7A54400));

        //......................................................................
        // Web
        pfx_helper.Run(RenderableObjectType.PFX_WEB, "web.pfx3", 
          m.Reset().Set(DescPfxMutator.Field.OFFSET, null, 0.5f, null));

        //......................................................................
        // Generic vapor
        pfx_helper.Run(RenderableObjectType.PFX_VAPOR, "vapor.pfx3", 
          m.Reset());

        //......................................................................
        // Vaporizing terrain
        m.Reset()
         .Set(DescPfxMutator.Field.OFFSET, null, 0.2f, null)
         .Set(DescPfxMutator.Field.SPAWN_SHAPE, 0.5f, 0.0f, 0.5f)
         .Set(DescPfxMutator.Field.PARTICLE_NUM, 1, 10);
        pfx_helper.Run(RenderableObjectType.PFX_WATER_OF_HEALING, "vapor.pfx3", 
          m.Set(DescPfxMutator.Field.IMAGE_ID, MapEnum.PfxImage.SPECK_HEALING));
        pfx_helper.Run(RenderableObjectType.PFX_WATER_OF_AWARENESS, "vapor.pfx3", 
          m.Set(DescPfxMutator.Field.IMAGE_ID, MapEnum.PfxImage.SPECK_QUESTION));
        pfx_helper.Run(RenderableObjectType.PFX_WATER_OF_TRANSMUTATION, "vapor.pfx3", 
          m.Set(DescPfxMutator.Field.IMAGE_ID, MapEnum.PfxImage.SPECK_CHANGE));
        pfx_helper.Run(RenderableObjectType.PFX_ALCHEMY, "vapor.pfx3", 
          m.Set(DescPfxMutator.Field.IMAGE_ID, MapEnum.PfxImage.SPECK_BUBBLE));

        //......................................................................
        // Scale'n fade
        pfx_helper.Run(RenderableObjectType.PFX_SCALE_N_FADE, "scale&fade.pfx3", 
          m.Reset());

        //......................................................................
        // Leakage
        pfx_helper.Run(RenderableObjectType.PFX_LEAKAGE, "leakage.pfx3", 
          m.Reset().Set(DescPfxMutator.Field.OFFSET, null, 0.5f, 0.6f));

        //......................................................................
        // Generic levitation
        pfx_helper.Run(RenderableObjectType.PFX_LEVITATION, "levitation.pfx3", 
          m.Reset());

        //......................................................................
        // Generic splash
        pfx_helper.Run(RenderableObjectType.PFX_SPLASH, "splash.pfx3", 
          m.Reset());

        //......................................................................
        // Generic fountain
        pfx_helper.Run(RenderableObjectType.PFX_FOUNTAIN, "fountain.pfx3", 
          m.Reset());
    }

    //--------------------------------------------------------------------------
    public RenderableObject CreateBillboardText(final String text) {
        Renderer r = Main.inst.renderer;
        TxCache tx_cache = r.GetTxCache();

        Integer text_id = m_billboard_text_id_map.get(text);
        if(text_id == null) {
            // Create new id
            text_id = m_billboard_text_id_map.size();
            m_billboard_text_id_map.put(text, text_id);
            final String tx_cache_key = "billboard-" + text_id;
            final TextRenderer.TextureDesc texture_desc = 
              Main.inst.text_renderer.RenderText("billboard", text);

            // Put text texture to cache
            tx_cache.Put(tx_cache_key, 
              new RendererTexture(tx_cache_key, texture_desc.GetTexture(), false));

            // Create billboard model
            RenderableObjectType.BILLBOARD_TEXT.AddTemplateBuilder(text_id,
              new RenderableObject.ITemplateBuilder() {
                  @Override public RenderableTemplate Build() {
                      return new BillboardTmpl(text, tx_cache_key, tx_cache_key, 
                        Color.WHITE, texture_desc.u, texture_desc.v, 
                        texture_desc.u_size, texture_desc.v_size);
            }});
        }
        return RenderableObjectType.BILLBOARD_TEXT.Create(text_id);
    }

    //--------------------------------------------------------------------------
    public RenderableObject CreateBillboardSprite(final Enum<?> sprite) {
        final DescSprite sprite_desc = GetSprite(sprite);
        String tx_cache_key = GetTxCacheKey(sprite_desc);
        UpdateSpriteTexture(sprite_desc, tx_cache_key, false, false);

        Integer sprite_id = m_billboard_sprite_id_map.get(sprite_desc);
        if(sprite_id == null) {
            sprite_id = m_billboard_sprite_id_map.size();
            m_billboard_sprite_id_map.put(sprite_desc, sprite_id);

            // Create billboard model
            RenderableObjectType.BILLBOARD_SPRITE.AddTemplateBuilder(sprite_id,
              new RenderableObject.ITemplateBuilder() {
                  @Override public RenderableTemplate Build() {
                      DescRect rect = sprite_desc.rects.get(sprite.ordinal());
                      return new BillboardTmpl(sprite, sprite_desc.texture, sprite_desc.texture, 
                        Color.WHITE, rect.unit_x, rect.unit_y, rect.unit_width, rect.unit_height);
            }});
        }
        return RenderableObjectType.BILLBOARD_SPRITE.Create(sprite_id);
    }

    //--------------------------------------------------------------------------
    public RenderableObject CreateBillboardEmotion(final Enum<?> sprite) {
        final DescSprite sprite_desc = GetSprite(sprite);
        String tx_cache_key = GetTxCacheKey(sprite_desc);
        UpdateSpriteTexture(sprite_desc, tx_cache_key, false, false);

        Integer sprite_id = m_billboard_emotion_id_map.get(sprite_desc);
        if(sprite_id == null) {
            sprite_id = m_billboard_emotion_id_map.size();
            m_billboard_emotion_id_map.put(sprite_desc, sprite_id);

            // Create billboard emotion model
            RenderableObjectType.BILLBOARD_EMOTION.AddTemplateBuilder(sprite_id,
              new RenderableObject.ITemplateBuilder() {
                  @Override public RenderableTemplate Build() {
                      DescRect rect = sprite_desc.rects.get(sprite.ordinal());
                      return new BillboardEmotionTmpl(sprite, sprite_desc.texture, sprite_desc.texture, 
                        Color.WHITE, rect.unit_x, rect.unit_y, rect.unit_width, rect.unit_height);
            }});
        }
        return RenderableObjectType.BILLBOARD_EMOTION.Create(sprite_id);
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Update runtime transformation of all templates
        for(RenderableObjectType type : RenderableObjectType.values()) {
            for(ITemplate template : type.GetTemplates()) {
                template.UpdateRuntimeTransform();
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodJson(JsonCtx ctx) {
        super.OnMethodJson(ctx);

        if(ctx.pre_children) {
            // Write snapshot
            if(ctx.targets.contains(SgNode.JsonTarget.COMMON)) {
                // Write sprite
                ctx.Write("\"sprites\" : {");
                ctx.UpdateLevel(+1);
                for(String type : m_sprite_map.keySet()) {
                    ctx.Write("\"%s\" : [", type);
                    ctx.UpdateLevel(+1);
                    for(DescSprite sprite : m_sprite_map.get(type)) {
                        ctx.Write("%s,", GsonUtils.Serialize(sprite, false));
                    }
                    ctx.UpdateLevel(-1);
                    ctx.Write("],");
                }
                ctx.UpdateLevel(-1);
                ctx.Write("}," );

                // Write model types
                ctx.Write("\"renderable-objects\" : {");
                ctx.UpdateLevel(+1);
                for(RenderableObjectType model_type : RenderableObjectType.values()) {
                    ctx.Write("\"%s\" : { \"type\" : [", model_type.toString().toLowerCase());
                    ctx.UpdateLevel(+1);
                    Collection<ITemplate> templates = model_type.GetTemplates();
                    for(ITemplate template : templates) {
                        ctx.Write("\"name\" : \"%s\",", template.getClass().getSimpleName());
                        ctx.Write("\"inst-num\" : %s,", template.GetSize());
                        ctx.Write("},");
                    }
                    ctx.UpdateLevel(-1);
                    ctx.Write("]}," );
                }
                ctx.UpdateLevel(-1);
                ctx.Write("}," );
            }
        }
        return true;
    }
}
