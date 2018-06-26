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
package com.matalok.pd3d.renderer;

//------------------------------------------------------------------------------
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader.ParticleEffectLoadParameter;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier;
import com.badlogic.gdx.graphics.g3d.particles.influencers.DynamicsModifier.BrownianAcceleration;
import com.badlogic.gdx.graphics.g3d.particles.influencers.Influencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.RegionInfluencer.AspectTextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ScaleInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.influencers.SpawnInfluencer;
import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue;
import com.badlogic.gdx.graphics.g3d.particles.values.RangedNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.SpawnShapeValue;
import com.matalok.pd3d.desc.DescPfxMutator;
import com.matalok.pd3d.shared.Utils;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class RendererParticle
  implements IManaged {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static AssetManager pfx_ass_man;
    private static ParticleEffectLoadParameter pfx_loader;

    //--------------------------------------------------------------------------
    public static ParticleEffect LoadPfx(String path) {
        if(pfx_ass_man == null) {
            ParticleSystem system = new ParticleSystem();
            PointSpriteParticleBatch batch = new PointSpriteParticleBatch();
            system.add(batch);
            pfx_loader = new ParticleEffectLoadParameter(system.getBatches());
        }
        pfx_ass_man.load(path, ParticleEffect.class, pfx_loader);
        pfx_ass_man.finishLoading();
        return (ParticleEffect)pfx_ass_man.get(path);
    }

    //**************************************************************************
    // Manager
    //**************************************************************************
    public static class Manager {
        //----------------------------------------------------------------------
        public ParticleSystem system;
        public PointSpriteParticleBatch batch;
        public ParticleEffectLoadParameter loader;
        public AssetManager ass_man;

        //----------------------------------------------------------------------
        public Manager() {
            system = new ParticleSystem();
            batch = new PointSpriteParticleBatch();
            system.add(batch);
            loader = new ParticleEffectLoadParameter(system.getBatches());
            ass_man = new AssetManager();
        }

        //----------------------------------------------------------------------
        public ParticleEffect Load(String path) {
            ass_man.load(path, ParticleEffect.class, loader);
            ass_man.finishLoading();
            return (ParticleEffect)ass_man.get(path);
        }
    }
    public static Manager manager = new Manager();

    //**************************************************************************
    // RendererParticle
    //**************************************************************************
    public ParticleEffect inst;
    public RegularEmitter emitter;
    public ColorInfluencer.Single inf_color;
    public SpawnInfluencer inf_spawn;
    public RegionInfluencer.Single inf_region;
    public ScaleInfluencer inf_scale;
    public DynamicsInfluencer inf_dyn;
    public BrownianAcceleration dyn_brow_acc;

    //--------------------------------------------------------------------------
    public RendererParticle(ParticleEffect pfx) {
        inst = pfx.copy();

        // Init emitter 
        ParticleController ctrl = inst.getControllers().first();
        emitter = (RegularEmitter)ctrl.emitter;

        // Init influencers
        inf_color = (ColorInfluencer.Single)GetInfluencer(ctrl, ColorInfluencer.Single.class);
        inf_spawn = (SpawnInfluencer)GetInfluencer(ctrl, SpawnInfluencer.class);
        inf_region = (RegionInfluencer.Single)GetInfluencer(ctrl, RegionInfluencer.Single.class);
        inf_scale = (ScaleInfluencer)GetInfluencer(ctrl, ScaleInfluencer.class);
        inf_dyn = (DynamicsInfluencer)GetInfluencer(ctrl, DynamicsInfluencer.class);

        // Dynamic modifiers 
        for(DynamicsModifier dyn_mod: inf_dyn.velocities) {
            if(dyn_mod instanceof BrownianAcceleration) {
                dyn_brow_acc = (BrownianAcceleration)dyn_mod;
            }
        }
    }

    //--------------------------------------------------------------------------
    public Influencer GetInfluencer(ParticleController ctrl, 
      Class<? extends Influencer> inf_class) {
        try {
            return ctrl.findInfluencer(inf_class);

        } catch(Exception ex) {
            Utils.LogException(ex, 
              "Failed to get particle influencer :: class=%s", inf_class);
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public long StopEmitter() {
        emitter.setEmissionMode(RegularEmitter.EmissionMode.EnabledUntilCycleEnd);
        return (long)emitter.durationValue.getLowMax();
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetColorNum(int num) {
        float[] colors, timeline;
        inf_color.colorValue.setColors(colors = new float[num * 3]);
        inf_color.colorValue.setTimeline(timeline = new float[num]);
        for(int i = 0; i < num; i++) {
            float val = (float)i / (num - 1);
            int col_idx = i * 3;
            colors[col_idx + 0] = colors[col_idx + 1] = 
              colors[col_idx + 2] = timeline[i] = val;
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetColors(Integer... cols) {
        if(cols == null) {
            return this;
        }

        SetColorNum(cols.length);
        for(int i = 0; i < cols.length; i++) {
            SetColor(i, new Color(cols[i]));
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetColors(Color... cols) {
        SetColorNum(cols.length);
        for(int i = 0; i < cols.length; i++) {
            SetColor(i, cols[i]);
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetColor(int idx, Color col) {
        float colors[] = inf_color.colorValue.getColors();

        idx *= 3;
        colors[idx + 0] = col.r; 
        colors[idx + 1] = col.g; 
        colors[idx + 2] = col.b;
        return this;
    }

    //--------------------------------------------------------------------------
    public boolean SetNumericalValue(Float low, RangedNumericValue num_val) {
        boolean is_active = (low != null);
        num_val.setActive(is_active);
        if(is_active) {
            num_val.setLow(low);
        }
        return is_active;
    }

    //--------------------------------------------------------------------------
    public boolean SetNumericalValue(Float low, Float high, ScaledNumericValue num_val) {
        boolean is_active = SetNumericalValue(low, (RangedNumericValue)num_val);
        if(is_active) {
            num_val.setHigh(high);
        }
        return is_active;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetOffset(Float x, Float y, Float z) {
        SpawnShapeValue pos = inf_spawn.spawnShapeValue;
        boolean is_active = 
          SetNumericalValue(x, pos.xOffsetValue) |
          SetNumericalValue(y, pos.yOffsetValue) |
          SetNumericalValue(z, pos.zOffsetValue);
        pos.setActive(is_active);
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetSpawnShape(Float x, Float y, Float z) {
        if(inf_spawn.spawnShapeValue instanceof PrimitiveSpawnShapeValue) {
            PrimitiveSpawnShapeValue prim = (PrimitiveSpawnShapeValue)inf_spawn.spawnShapeValue;
            SetNumericalValue(x, x, prim.spawnWidthValue);
            SetNumericalValue(y, x, prim.spawnHeightValue);
            SetNumericalValue(z, x, prim.spawnDepthValue);
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetParticleNum(Integer min, Integer max) {
        if(min != null) {
            emitter.minParticleCount = min;
        }
        if(max != null) {
            emitter.maxParticleCount = max;
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetRegion(int idx) {
        AspectTextureRegion reg = inf_region.regions.first();
        float u_size = reg.u2 - reg.u;
        float v_size = reg.v2 - reg.v;
        int col_num = (int) (1.0f / u_size);
        int x = idx % col_num;
        int y = idx / col_num;
        reg.u = x * u_size; reg.v = y * v_size;
        reg.u2 = reg.u + u_size; reg.v2 = reg.v + v_size;
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetScale(float low, float high) {
        SetNumericalValue(low, high, inf_scale.value);
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle SetBrowStrength(float low, float high) {
        SetNumericalValue(low, high, dyn_brow_acc.strengthValue);
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle Reset() {
        inst.init();
        inst.reset();
        return this;
    }

    //--------------------------------------------------------------------------
    public RendererParticle ApplyMutator(DescPfxMutator m) {
        // Color influencer
        if(inf_color != null) {
            SetColors(m.colors);
        }

        // Spawn influencer
        if(inf_spawn != null) {
            if(m.offset != null) { 
                SetOffset(m.offset.x, m.offset.y, m.offset.z);
            }
            if(m.spawn_shape != null) {
                SetSpawnShape(m.spawn_shape.x, m.spawn_shape.y, m.spawn_shape.z);
            }
        }

        // Emitter
        if(m.particle_num != null && emitter != null) {
            SetParticleNum(m.particle_num.low, m.particle_num.high);
        }

        // Region influencer
        if(m.image_id != null) {
           SetRegion(m.image_id);
        }

        // Browning dynamics
        if(m.brow_strength != null && dyn_brow_acc != null) {
            SetBrowStrength(m.brow_strength.low, m.brow_strength.high);
        }

        // Scale influencer
        if(m.scale != null && inf_scale != null) {
            SetScale(m.scale.low, m.scale.high);
        }
        return this;
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    @Override public void OnCleanup() {
        inst.dispose();
        inst = null;
    }
}
