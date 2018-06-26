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
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public abstract class RendererAttrib<T> {
    //**************************************************************************
    // Type
    //**************************************************************************
    public enum Type {
        //----------------------------------------------------------------------
        COLOR("col"), DIR_LIGHT("dirl"), TEXTURE("tex"), INT("int"), FLOAT("ftl"), 
        BLENDING("bln");

        //----------------------------------------------------------------------
        public String name;
        public UtilsClass.IdNameMap id_name_map;
        public long mask;

        //----------------------------------------------------------------------
        private Type(String name) {
            this.name = name;
            id_name_map = new UtilsClass.IdNameMap();
            mask = 0;

            switch(ordinal()) {
            //..................................................................
            // COLOR
            case 0: {
                id_name_map.put(ColorAttribute.Diffuse,         "diffuse");
                id_name_map.put(ColorAttribute.Specular,        "specular");
                id_name_map.put(ColorAttribute.Ambient,         "ambient");
                id_name_map.put(ColorAttribute.Emissive,        "emissive");
                id_name_map.put(ColorAttribute.Reflection,      "reflection");
                id_name_map.put(ColorAttribute.AmbientLight,    "ambient-light");
                id_name_map.put(ColorAttribute.Fog,             "fog");
            } break;

            //..................................................................
            // DIR_LIGHT
            case 1: {
                id_name_map.put((long)1 <<  0, "dirl-01"); id_name_map.put((long)1 <<  1, "dirl-02");
                id_name_map.put((long)1 <<  2, "dirl-03"); id_name_map.put((long)1 <<  3, "dirl-04");
                id_name_map.put((long)1 <<  4, "dirl-05"); id_name_map.put((long)1 <<  5, "dirl-06");
                id_name_map.put((long)1 <<  6, "dirl-07"); id_name_map.put((long)1 <<  7, "dirl-08");
                id_name_map.put((long)1 <<  8, "dirl-09"); id_name_map.put((long)1 <<  9, "dirl-10");
                id_name_map.put((long)1 << 10, "dirl-11"); id_name_map.put((long)1 << 11, "dirl-12");
                id_name_map.put((long)1 << 12, "dirl-13"); id_name_map.put((long)1 << 13, "dirl-14");
                id_name_map.put((long)1 << 14, "dirl-15"); id_name_map.put((long)1 << 15, "dirl-16");
                mask = DirectionalLightsAttribute.Type;
            } break;

            //..................................................................
            // TEXTURE
            case 2: {
                id_name_map.put(TextureAttribute.Diffuse,       "diffuse");
                id_name_map.put(TextureAttribute.Specular,      "specular");
                id_name_map.put(TextureAttribute.Bump,          "bump");
                id_name_map.put(TextureAttribute.Normal,        "normal");
                id_name_map.put(TextureAttribute.Ambient,       "ambient");
                id_name_map.put(TextureAttribute.Emissive,      "emisive");
                id_name_map.put(TextureAttribute.Reflection,    "reflection");
            } break;

            //..................................................................
            // INT
            case 3: {
                id_name_map.put(IntAttribute.CullFace,          "cull-face");
            } break;

            //..................................................................
            // FLOAT
            case 4: {
                id_name_map.put(FloatAttribute.AlphaTest,       "alpha-test");
                id_name_map.put(FloatAttribute.Shininess,       "shininess");
            } break;

            //..................................................................
            // BLENDING
            case 5: {
                id_name_map.put(BlendingAttribute.Type,         "blending");
                mask = BlendingAttribute.Type;
            } break;
            }

            // Initialize id-name map
            id_name_map.Init();

            // Create ID mask
            if(mask == 0) {
                for(Long id : id_name_map.keySet()) {
                    mask |= id;
                }
            }
        }

        //----------------------------------------------------------------------
        public String GetShortName(long id) {
            return name + "-" + Utils.GetFirstSetBit(id);
        }

        //----------------------------------------------------------------------
        @SuppressWarnings("rawtypes")
        public ArrayList CreateAttribCfgArray() {
            return 
              (this == Type.COLOR) ?     new ArrayList<RendererAttrib.AColor.Cfg>() :
              (this == Type.DIR_LIGHT) ? new ArrayList<RendererAttrib.ADirLight.Cfg>() :
              (this == Type.TEXTURE) ?   new ArrayList<RendererAttrib.ATexture.Cfg>() :
              (this == Type.INT) ?       new ArrayList<RendererAttrib.AInt.Cfg>() :
              (this == Type.FLOAT) ?     new ArrayList<RendererAttrib.AFloat.Cfg>() :
              (this == Type.BLENDING) ?  new ArrayList<RendererAttrib.ABlending.Cfg>() : null;
        }

        //----------------------------------------------------------------------
        public Cfg CreateAttribCfg() {
            return 
              (this == Type.COLOR) ? new RendererAttrib.AColor.Cfg(
                ColorAttribute.Diffuse, Color.RED) :
              (this == Type.DIR_LIGHT) ? new RendererAttrib.ADirLight.Cfg(
                0, Color.YELLOW, Vector3.X) :
              (this == Type.TEXTURE) ? new RendererAttrib.ATexture.Cfg(
                TextureAttribute.Diffuse, "default") :
              (this == Type.INT) ? new RendererAttrib.AInt.Cfg(
                IntAttribute.CullFace, GL20.GL_FRONT) :
              (this == Type.FLOAT) ? new RendererAttrib.AFloat.Cfg(
                FloatAttribute.AlphaTest, 0.5f) :
              (this == Type.BLENDING) ? new RendererAttrib.ABlending.Cfg(
                0, true, 0.7f, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA) : null;
        }

        //----------------------------------------------------------------------
        @SuppressWarnings("rawtypes")
        public RendererAttrib CreateAttrib() {
            return 
              (this == Type.COLOR) ?     new RendererAttrib.AColor() : 
              (this == Type.DIR_LIGHT) ? new RendererAttrib.ADirLight() : 
              (this == Type.TEXTURE) ?   new RendererAttrib.ATexture() :
              (this == Type.INT) ?       new RendererAttrib.AInt() : 
              (this == Type.FLOAT) ?     new RendererAttrib.AFloat() :
              (this == Type.BLENDING) ?  new RendererAttrib.ABlending() : null;
        }
    };
    public static final Type[] type_array = Type.values();

    //**************************************************************************
    // Cfg
    //**************************************************************************
    public static class Cfg {
        //----------------------------------------------------------------------
        public long id;
        public boolean on;

        //----------------------------------------------------------------------
        public Cfg(long id) {
            this.id = id;
            this.on = true;
        }

        //----------------------------------------------------------------------
        public Cfg(Cfg cfg) {
            this.id = cfg.id;
            this.on = cfg.on;
        }
    }

    //**************************************************************************
    // RendererAttrib
    //**************************************************************************
    protected HashMap<Long, T> m_id_attrib_map;
    protected Type m_type;

    //--------------------------------------------------------------------------
    public RendererAttrib(Type type) {
        m_id_attrib_map = new HashMap<Long, T>();
        m_type = type;
    }

    //--------------------------------------------------------------------------
    public void Set(Cfg cfg, RendererAttribStack stack) {
        if(!cfg.on) {
            return;
        }

        T attrib = m_id_attrib_map.get(cfg.id);
        if(attrib != null) {
            UpdateAttrib(cfg, attrib);
        } else {
            attrib = CreateAttrib(cfg);
            m_id_attrib_map.put(cfg.id, attrib);
            stack.GetOwner().SetAttrib(attrib);
        }
    }

    //--------------------------------------------------------------------------
    public RendererAttrib<T> Refresh(ArrayList<? extends Cfg> cfgs, 
      RendererAttribStack stack) {
        // Clear old attributes
        m_id_attrib_map.clear();
        stack.GetOwner().ClearAttrib(m_type.mask);

        // Stop if config is empty
        if(cfgs == null) {
            return this;
        }

        // Set new attributes
        for(Cfg cfg : cfgs) {
            Set(cfg, stack);
        }
        return this;
    }

    //--------------------------------------------------------------------------
    public abstract T CreateAttrib(Cfg cfg);
    public abstract T UpdateAttrib(Cfg cfg, T attrib);

    //**************************************************************************
    // AColor
    //**************************************************************************
    public static class AColor
      extends RendererAttrib<ColorAttribute> {
        //----------------------------------------------------------------------
        public static class Cfg
          extends RendererAttrib.Cfg {
            //------------------------------------------------------------------
            public Color color;

            //------------------------------------------------------------------
            public Cfg(long id, Color color) {
                super(id);
                this.color = new Color(color);
            }

            //------------------------------------------------------------------
            public Cfg(Cfg cfg) {
                this(cfg.id, cfg.color);
            }
        }

        //----------------------------------------------------------------------
        public AColor() {
            super(Type.COLOR);
        }

        //----------------------------------------------------------------------
        @Override public ColorAttribute CreateAttrib(RendererAttrib.Cfg cfg) {
            return new ColorAttribute(cfg.id, ((Cfg)cfg).color);
        }

        //----------------------------------------------------------------------
        @Override public ColorAttribute UpdateAttrib(RendererAttrib.Cfg cfg,
          ColorAttribute attrib) {
            attrib.color.set(((Cfg)cfg).color);
            return attrib;
        }
    }

    //**************************************************************************
    // ADirLight
    //**************************************************************************
    public static class ADirLight
      extends RendererAttrib<DirectionalLight> {
        //----------------------------------------------------------------------
        public static class Cfg 
          extends RendererAttrib.Cfg {
            //------------------------------------------------------------------
            public Color color;
            public Vector3 dir;

            //------------------------------------------------------------------
            public Cfg(long id, Color color, Vector3 dir) {
                super(id);
                this.color = new Color(color);
                this.dir = new Vector3(dir);
            }

            //------------------------------------------------------------------
            public Cfg(Cfg cfg) {
                this(cfg.id, cfg.color, cfg.dir);
            }
        }

        //----------------------------------------------------------------------
        public ADirLight() {
            super(Type.DIR_LIGHT);
        }

        //----------------------------------------------------------------------
        @Override public DirectionalLight CreateAttrib(RendererAttrib.Cfg cfg) {
            return new DirectionalLight().set(((Cfg)cfg).color, ((Cfg)cfg).dir);
        }

        //----------------------------------------------------------------------
        @Override public DirectionalLight UpdateAttrib(RendererAttrib.Cfg cfg,
          DirectionalLight attrib) {
            attrib.set(((Cfg)cfg).color, ((Cfg)cfg).dir);
            return attrib;
        }
    }

    //**************************************************************************
    // ATexture
    //**************************************************************************
    public static class ATexture
      extends RendererAttrib<TextureAttribute> {
        //----------------------------------------------------------------------
        public static class Cfg
          extends RendererAttrib.Cfg {
            //------------------------------------------------------------------
            public String name;

            //------------------------------------------------------------------
            public Cfg(long id, String texture_name) {
                super(id);
                this.name = new String(texture_name);
            }

            //------------------------------------------------------------------
            public Cfg(Cfg cfg) {
                this(cfg.id, cfg.name);
            }
        }

        //----------------------------------------------------------------------
        public ATexture() {
            super(Type.TEXTURE);
        }

        //----------------------------------------------------------------------
        @Override public TextureAttribute CreateAttrib(RendererAttrib.Cfg cfg) {
            // XXX: Get texture from cache
            return new TextureAttribute(cfg.id, (Texture)null);
        }

        //----------------------------------------------------------------------
        @Override public TextureAttribute UpdateAttrib(RendererAttrib.Cfg cfg,
          TextureAttribute attrib) {
            // XXX: Get texture from cache
            attrib.textureDescription.texture = null;
            return attrib;
        }
    }

    //**************************************************************************
    // AInt
    //**************************************************************************
    public static class AInt
      extends RendererAttrib<IntAttribute> {
        //----------------------------------------------------------------------
        public static class Cfg
          extends RendererAttrib.Cfg {
            //------------------------------------------------------------------
            public int value;

            //------------------------------------------------------------------
            public Cfg(long id, int value) {
                super(id);
                this.value = value;
            }

            //------------------------------------------------------------------
            public Cfg(Cfg cfg) {
                this(cfg.id, cfg.value);
            }
        }

        //----------------------------------------------------------------------
        public AInt() {
            super(Type.INT);
        }

        //----------------------------------------------------------------------
        @Override public IntAttribute CreateAttrib(RendererAttrib.Cfg cfg) {
            return new IntAttribute(cfg.id, ((Cfg)cfg).value);
        }

        //----------------------------------------------------------------------
        @Override public IntAttribute UpdateAttrib(RendererAttrib.Cfg cfg,
          IntAttribute attrib) {
            attrib.value = ((Cfg)cfg).value;
            return attrib;
        }
    }

    //**************************************************************************
    // AFloat
    //**************************************************************************
    public static class AFloat
      extends RendererAttrib<FloatAttribute> {
        //----------------------------------------------------------------------
        public static class Cfg
          extends RendererAttrib.Cfg {
            //------------------------------------------------------------------
            public float value;

            //------------------------------------------------------------------
            public Cfg(long id, float value) {
                super(id);
                this.value = value;
            }

            //------------------------------------------------------------------
            public Cfg(Cfg cfg) {
                this(cfg.id, cfg.value);
            }
        }

        //----------------------------------------------------------------------
        public AFloat() {
            super(Type.FLOAT);
        }

        //----------------------------------------------------------------------
        @Override public FloatAttribute CreateAttrib(RendererAttrib.Cfg cfg) {
            return new FloatAttribute(cfg.id, ((Cfg)cfg).value);
        }

        //----------------------------------------------------------------------
        @Override public FloatAttribute UpdateAttrib(RendererAttrib.Cfg cfg,
          FloatAttribute attrib) {
            attrib.value = ((Cfg)cfg).value;
            return attrib;
        }
    }

    //**************************************************************************
    // ABlending
    //**************************************************************************
    public static class ABlending
      extends RendererAttrib<BlendingAttribute> {
        //----------------------------------------------------------------------
        public static class Cfg
          extends RendererAttrib.Cfg {
            //------------------------------------------------------------------
            public boolean is_blended;
            public float alpha;
            public int src_func, dst_func;

            //------------------------------------------------------------------
            public Cfg(long id, boolean is_blended, float alpha, int src_func, 
              int dst_func) {
                super(id);
                this.is_blended = is_blended;
                this.alpha = alpha;
                this.src_func = src_func;
                this.dst_func = dst_func;
            }

            //------------------------------------------------------------------
            public Cfg(Cfg cfg) {
                this(cfg.id, cfg.is_blended, cfg.alpha, cfg.src_func, cfg.dst_func);
            }
        }

        //----------------------------------------------------------------------
        public ABlending() {
            super(Type.BLENDING);
        }

        //----------------------------------------------------------------------
        @Override public BlendingAttribute CreateAttrib(RendererAttrib.Cfg cfg) {
            return new BlendingAttribute(((Cfg)cfg).is_blended, ((Cfg)cfg).src_func, 
              ((Cfg)cfg).dst_func, ((Cfg)cfg).alpha);
        }

        //----------------------------------------------------------------------
        @Override public BlendingAttribute UpdateAttrib(RendererAttrib.Cfg cfg,
          BlendingAttribute attrib) {
            attrib.blended = ((Cfg)cfg).is_blended;
            attrib.opacity = ((Cfg)cfg).alpha;
            attrib.sourceFunction = ((Cfg)cfg).src_func;
            attrib.destFunction = ((Cfg)cfg).dst_func;
            return attrib;
        }
    }
}
