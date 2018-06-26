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
import com.badlogic.gdx.graphics.Color;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescEvent;
import com.matalok.pd3d.desc.DescPfxMutator;
import com.matalok.pd3d.desc.DescPfxMutator.Field;
import com.matalok.pd3d.level.object.LevelObjectCell;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class LevelEventHandler {
    //***************************************************************************
    // IHandler
    //***************************************************************************
    public interface IHandler {
        //----------------------------------------------------------------------
        public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m);
    }

    //***************************************************************************
    // MarkerSearchHandler
    //***************************************************************************
    public static class MarkerSearchHandler 
      implements IHandler {
        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            lvl.CreateMarker(cell, RenderableObjectType.MARKER_SEARCH);
        }
    }

    //***************************************************************************
    // CameraShakeHandler
    //***************************************************************************
    public static class CameraShakeHandler 
      implements IHandler {
        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            Main.inst.level_camera.ShakeCamera(0.01f, 1000);
        }
    }

    //***************************************************************************
    // PfxEarchHandler
    //***************************************************************************
    public static class PfxEarthHandler
      implements IHandler {
        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            lvl.CreateParticle(cell, RenderableObjectType.PFX_SPLASH, 
              m.Set(Field.OFFSET, null, 0.4f, null)
               .Set(Field.COLORS, Color.rgba8888(Color.BROWN))
               .Set(Field.IMAGE_ID, MapEnum.PfxImage.PFX_SQUARE10));
        }
    }

    //***************************************************************************
    // PfxLeafHandler
    //***************************************************************************
    public static class PfxLeafHandler
      implements IHandler {
        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            lvl.CreateParticle(cell, RenderableObjectType.PFX_SPLASH, 
              m.Set(Field.OFFSET, null, 0.4f, null)
               .Set(Field.PARTICLE_NUM, 3, 4)
               .Set(Field.SCALE, 0.2f, 0.5f)
               .Set(Field.BROW_STRENGTH, 0.0f, 10.0f)
               .Set(Field.IMAGE_ID, MapEnum.PfxImage.PFX_SQUARE10));
        }
    }

    //***************************************************************************
    // PfxSpecHandler
    //***************************************************************************
    public static class PfxSpecHandler
      implements IHandler {
        //----------------------------------------------------------------------
        // Copy paste from com.watabou.pixeldungeon.effects.Speck
        public static final int HEALING     = 0;
        public static final int STAR        = 1;
        public static final int LIGHT       = 2;
        public static final int QUESTION    = 3;
        public static final int UP          = 4;
        public static final int SCREAM      = 5;
        public static final int BONE        = 6;
        public static final int WOOL        = 7;
        public static final int ROCK        = 8;
        public static final int NOTE        = 9;
        public static final int CHANGE      = 10;
        public static final int HEART       = 11;
        public static final int BUBBLE      = 12;
        public static final int STEAM       = 13;
        public static final int COIN        = 14;
        public static final int DISCOVER    = 101;
        public static final int EVOKE       = 102;
        public static final int MASTERY     = 103;
        public static final int KIT         = 104;
        public static final int RATTLE      = 105;
        public static final int JET         = 106;
        public static final int TOXIC       = 107;
        public static final int PARALYSIS   = 108;
        public static final int DUST        = 109;
        public static final int FORGE       = 110;
        public static final int CONFUSION   = 111;

        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            RenderableObjectType robj_type = null;
            switch(m.image_id) {
            // Simple
            case HEALING:
            case STAR:
            case LIGHT:
            case QUESTION:
            case UP:
            case SCREAM:
            case WOOL:
            case ROCK:
            case NOTE:
            case CHANGE:
            case HEART:
            case BUBBLE:
            case STEAM: {
                robj_type = RenderableObjectType.PFX_SCALE_N_FADE;
                m.image_id += MapEnum.PfxImage.SPECK_HEALING.ordinal();
            } break;

            case COIN: {
                robj_type = RenderableObjectType.PFX_FOUNTAIN;
                m.image_id = MapEnum.PfxImage.SPECK_COIN.ordinal();
            } break;

            // Complex 
            case DISCOVER: {
                robj_type = RenderableObjectType.PFX_SPLASH;
                m.image_id = MapEnum.PfxImage.SPECK_LIGHT.ordinal();
            } break;

            case EVOKE:
            case MASTERY:
            case KIT: 
            case FORGE: {
                robj_type = RenderableObjectType.PFX_SPLASH;
                m.image_id = MapEnum.PfxImage.SPECK_STAR.ordinal();
            } break;

            case BONE:
            case RATTLE: {
                robj_type = RenderableObjectType.PFX_FOUNTAIN;
                m.image_id = MapEnum.PfxImage.SPECK_BONE.ordinal();
            } break;

            case JET:
            case TOXIC:
            case PARALYSIS:
            case DUST:
            case CONFUSION: {
                robj_type = RenderableObjectType.PFX_SPLASH;
                m.image_id = MapEnum.PfxImage.SPECK_STEAM.ordinal();
            } break;
            }

            // PFX_VAPOR
            if(robj_type == RenderableObjectType.PFX_VAPOR) {
                m.Set(DescPfxMutator.Field.OFFSET, null, 1.2f, null);

            // PFX_SPLASH
            } else if(robj_type == RenderableObjectType.PFX_SPLASH) {
                m.Set(DescPfxMutator.Field.OFFSET, null, 0.2f, null);

            // PFX_FOUNTAIN
            } else if(robj_type == RenderableObjectType.PFX_FOUNTAIN) {
                m.Set(DescPfxMutator.Field.OFFSET, null, 0.5f, null)
                 .Set(DescPfxMutator.Field.SCALE, 0.4f, 0.7f);

            // PFX_SCALE_N_FADE
            } else if(robj_type == RenderableObjectType.PFX_SCALE_N_FADE) {
                m.Set(DescPfxMutator.Field.OFFSET, null, 1.2f, null);

            // ERROR
            } else if(robj_type == null) {
                Logger.e("Failed to run SPECK event");
            }

            // Create particle
            lvl.CreateParticle(cell, robj_type, m);
        }
    }


    //***************************************************************************
    // PfxFlameHandler
    //***************************************************************************
    public static class PfxFlameHandler
      implements IHandler {
        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            lvl.CreateParticle(cell, RenderableObjectType.PFX_SPLASH, 
              m.Set(Field.OFFSET, null, 0.4f, null)
               .Set(Field.PARTICLE_NUM, 8, 10)
               .Set(Field.SCALE, 0.3f, 0.6f)
               .Set(Field.BROW_STRENGTH, 0.0f, 20.0f)
               .Set(Field.COLORS, 0xFFF40C00, 0xFF000000)
               .Set(Field.IMAGE_ID, MapEnum.PfxImage.PFX_SQUARE10));
        }
    }

    //***************************************************************************
    // PfxSparkHandler
    //***************************************************************************
    public static class PfxSparkHandler
      implements IHandler {
        //----------------------------------------------------------------------
        @Override public void Run(Level lvl, LevelObjectCell cell, DescPfxMutator m) {
            lvl.CreateParticle(cell, RenderableObjectType.PFX_SPLASH, 
              m.Set(Field.OFFSET, null, 0.4f, null)
               .Set(Field.PARTICLE_NUM, 8, 10)
               .Set(Field.SCALE, 0.3f, 0.6f)
               .Set(Field.BROW_STRENGTH, 0.0f, 16.0f)
               .Set(Field.COLORS, 0xFFFFFFFF, 0x00000000)
               .Set(Field.IMAGE_ID, MapEnum.PfxImage.SPECK_LIGHT));
        }
    }

    //***************************************************************************
    // LevelEventHandler
    //***************************************************************************
    private DescPfxMutator m_pfx_mutator;
    private IHandler m_handler[];

    //--------------------------------------------------------------------------
    public LevelEventHandler() {
        m_pfx_mutator = new DescPfxMutator();
        m_handler = new IHandler[] {
            new MarkerSearchHandler(),      // MapEnum.EventType.MARKER_SEARCH
            new CameraShakeHandler(),       // MapEnum.EventType.CAMERA_SHAKE
            new PfxEarthHandler(),          // MapEnum.EventType.PFX_EARTH
            new PfxLeafHandler(),           // MapEnum.EventType.PFX_LEAF
            new PfxSpecHandler(),           // MapEnum.EventType.PFX_SPEC
            new PfxFlameHandler(),          // MapEnum.EventType.PFX_FLAME
            new PfxSparkHandler(),          // MapEnum.EventType.PFX_SPARK
        };
        Utils.Assert(m_handler.length == MapEnum.EventType.GetSize(), 
          "Failed to initialize event handler");
    }

    //--------------------------------------------------------------------------
    public void Run(Level lvl, DescEvent event) {
        MapEnum.EventType event_type = MapEnum.EventType.Get(event.event_id);
        if(event_type == null) {
            Logger.e("Failed to run event handler :: id=%d", event.event_id);
            return;
        } 

        // Run handler
        Logger.d("Running level event :: name=%s", event_type.toString());
        m_handler[event.event_id].Run(lvl, 
          (event.cell_id != null) ? lvl.GetCell(event.cell_id) : null, 
          (event.pfx_mutator != null) ? event.pfx_mutator : m_pfx_mutator.Reset());
    }
}
