//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.renderer.RendererAttribStack;
import com.matalok.pd3d.renderer.RendererAttrib;
import com.matalok.pd3d.shared.GsonUtils;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class Config {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static final String file_name = "config.boot"; 
    public static final String version = "v0.2.8";
    public static boolean default_created = false;

    //--------------------------------------------------------------------------
    public static boolean Save(Config cfg) {
        FileHandle h_file = PlatformUtils.OpenLocalFile(file_name, false);
        if(h_file == null) {
            return false;
        }

        Logger.d("Saving config");
        try {
            h_file.writeString(GsonUtils.Serialize(cfg, true), false);
            return true;

        } catch(Exception ex) {
            Utils.LogException(ex, "Failed to save config");
            return false;
        }
    }

    //--------------------------------------------------------------------------
    public static Config Load() {
        Config cfg = null;
        FileHandle h_file = PlatformUtils.OpenLocalFile(file_name, false);

        // Try reading existing config
        if(h_file != null && h_file.exists()) {
            Logger.d("Loading config");
            cfg = (Config)GsonUtils.Deserialize(h_file.readString(), Config.class);
        }

        // Remove old config
        if(cfg != null && cfg.app_version.compareTo("v0.2.8") < 0) {
            h_file.delete();
            cfg = null;
        }

        // Create & save default config
        if(cfg == null) {
            Logger.d("Creating default config");
            cfg = LoadDefault();
            Save(cfg);
            default_created = true;
        } else {
            default_created = false;
        }
        return cfg;
    }

    //--------------------------------------------------------------------------
    public static Config LoadDefault() {
        return new Config();
    }

    //**************************************************************************
    // Config
    //**************************************************************************

    // App
    public String  app_version                              = Config.version;
    public int     app_width                                = 800;
    public int     app_height                               = 480;
    public boolean app_landscape                            = true;
    public boolean app_logging_enable                       = true;
    public float   app_music_volume                         = 1.0f;
    public float   app_sound_volume                         = 1.0f;

    //--------------------------------------------------------------------------
    // Native PD preferences
    public HashMap<String, Object> pd_preferences           = new HashMap<String, Object>(); {
        pd_preferences.put("music", true);
        pd_preferences.put("soundfx", true);
        pd_preferences.put("intro", false);
    }

    //--------------------------------------------------------------------------
    // PD commands
    public transient boolean cmd_iddqd = false;
    public transient boolean cmd_item_info_ext = false;

    //--------------------------------------------------------------------------
    // Profiler
    public transient boolean prof_enable                    = false;
    public transient float   prof_log_interval              = 1.0f;
    public transient int     prof_gl_log_pad                = 26;
    public transient int     prof_perf_cnt_log_pad          = 41;

    //--------------------------------------------------------------------------
    // Snapshot
    public transient String   snap_directory                = "snapshot";
    public transient boolean  snap_autoswitch_enable        = true;
    public transient float    snap_autoswitch_interval      = 16.0f;
    public transient String[] snap_list                     = new String[] {
        "001-170812093411811.json", "001-170812093654854.json",
        "001-170812093710810.json", "001-170812105729829.json",
        "001-17081309350888.json",  "001-170813095049849.json",
        "001-170814083440840.json", "001-170817081330830.json",
    };

    //--------------------------------------------------------------------------
    // Debug
    public transient long    dbg_slowdown                   = 0 * 500 * 10000;
    public transient boolean dbg_render_axis                = false;
    public transient boolean dbg_render_non_visible_obj     = false;
    public transient boolean dbg_log_recv_msg               = true;
    public transient boolean dbg_log_json_req_msg           = false;
    public transient boolean dbg_gui                        = false;

    //--------------------------------------------------------------------------
    // Server
    public transient boolean server_is_remote               = false;
    public transient String  server_addr                    = "localhost";
    public transient int     server_port                    = 50002;

    //--------------------------------------------------------------------------
    // Gui
    public           int      gui_font_scale                  = 1;
    public           float    gui_icon_scale                  = 1.0f;
    public transient float    gui_icon_size                   = 0.2f;
    public transient float    gui_inventory_icon_size         = 0.18f;
    public transient int      gui_static_screen_width         = 800;
    public transient int      gui_static_screen_height        = 480;
    public transient float    gui_fade_duration               = 0.2f;
    public transient float    gui_long_click_duration         = 0.5f;
    public transient float    gui_disabled_object_alpha       = 0.3f;
    public transient Color    gui_btn_color                   = new Color(Color.DARK_GRAY);
    public transient float    gui_btn_rel_height              = 0.1f;
    public transient float    gui_busy_rot_speed              = -1000.0f;
    public transient float    gui_busy_rot_after_stop         = 0.3f;

    //--------------------------------------------------------------------------
    // Gui item
    public transient Color gui_item_normal_color            = new Color(Color.WHITE);
    public transient Color gui_item_non_identified_color    = new Color(Color.PURPLE);
    public transient Color gui_item_cursed_color            = new Color(Color.RED);

    //--------------------------------------------------------------------------
    // Minimap
    public transient Color minimap_fill_color               = new Color(/*Color.RED*/);
    public transient float minimap_alpha                    = 0.6f;
    public transient float minimap_rotate_speed             = 0.5f;
    public transient float minimap_size                     = 0.4f;

    //--------------------------------------------------------------------------
    // Camera
    public transient float camera_rotate_follower_speed     = 7.0f;
    public transient float camera_swing_duration            = 0.2f;
    public transient float camera_circular_360_rot_duration = 20.0f;
    public transient float camera_draw_distance             = 12.0f;
    public transient float camera_horizontal_tilt           = -7.0f;
    public transient float camera_sway_max                  = 0.1f;
    public transient float camera_sway_speed                = 25.0f;

    //--------------------------------------------------------------------------
    // Level
    public transient int     lvl_hero_id                    = 1;
    public           boolean lvl_hero_auto_move             = true;
    public transient float   lvl_cell_size                  = 1.0f;
    public transient float   lvl_cell_bottom                = -3.0f;
    public transient float   lvl_cell_elevation             = 0.3f;

    public transient boolean lvl_pack                       = true;
    public transient boolean lvl_pack_duplicates            = false;
    public transient int     lvl_pack_max_size              = 8;

    public transient boolean lvl_pack_color                 = false;
    public transient long    lvl_pack_color_floor           = 0xffff0000;
    public transient long    lvl_pack_color_empty_space     = 0xf0f00f00;
    public transient long    lvl_pack_color_water           = 0xff00ff00;
    public transient long    lvl_pack_color_wall            = 0x00ffff00;
    public transient long    lvl_pack_color_grass           = 0xf0ff0f00;

    //--------------------------------------------------------------------------
    // Billboard
    public transient String billboard_font                  = "fonts/billboard.fnt";
    public transient float  billboard_scale                 = 1.0f / 96.0f;

    //--------------------------------------------------------------------------
    // Model
    public transient float   model_fade_in_duration           = 0.3f;
    public transient float   model_fade_out_duration          = 0.7f;
    public transient float   model_move_duration              = 0.3f;
    public transient float   model_rotate_duration            = 0.2f;
    public transient float   model_text_fade_duration         = 1.0f;
    public transient float   model_text_scale                 = 0.015f;
    public transient float   model_blood_alpha                = 0.3f;
    public transient float   model_blood_fade_duration        = 0.5f;
    public transient float   model_throw_unit_duration        = 0.05f;
    public transient boolean model_has_shadow                 = true;

    //--------------------------------------------------------------------------
    // Model-marker
    public transient float model_marker_alpha               = 0.3f;
    public transient float model_marker_fade_duration       = 0.5f;

    //--------------------------------------------------------------------------
    // Emotion
    public transient float emotion_scale_size               = 1.8f;
    public transient float emotion_scale_duration           = 1.0f;
    public transient float emotion_vertical_offset          = 1.3f;

    //--------------------------------------------------------------------------
    // HP
    public transient int   hp_step_num                      = 30;
    public transient float hp_scale_width                   = 1.0f;
    public transient float hp_scale_height                  = 4.0f;
    public transient float hp_vertical_offset               = 1.1f;

    //--------------------------------------------------------------------------
    // Grass
    public transient float grass_rotate_angle               = 3.0f;
    public transient float grass_rotate_duration            = 10.0f;

    //--------------------------------------------------------------------------
    // Fog
    public transient float   fog_alpha                      = 0.5f;
    public transient Color   fog_color                      = new Color(0.07f, 0.06f, 0.05f, 1.0f);
    public transient int     fog_layer_num                  = 1;
    public transient float   fog_fade_out_duration_factor   = 5.0f;
    public transient float   fog_fade_in_duration_factor    = 1.0f;
    public transient float   fog_update_interval            = 0.2f;
    public transient boolean fog_fading_enabled             = true;

    //--------------------------------------------------------------------------
    // Water
    public transient float   water_alpha                    = 1.0f;//0.7f;
    public transient float   water_move_speed               = 0.1f;
    public transient Vector2 water_move_dir                 = new Vector2(0.5f, 0.7f);

    //--------------------------------------------------------------------------
    // Terrain
    public transient boolean terrain_smooth_transition_enable   = true;
    public transient Vector3 terrain_move_from                  = new Vector3(0.0f, -3.0f, 0.0f);
    public transient Vector3 terrain_move_to                    = new Vector3(0.0f, 0.0f, 0.0f);
    public transient float   terrain_move_duration              = 3.0f;

    //--------------------------------------------------------------------------
    // Renderer
    public           Color  render_clear_color                  = new Color(0.0f, 0.0f, 0.0f, 1.0f);

    //--------------------------------------------------------------------------
    // Renderer environment
    public transient RendererAttribStack.Cfg render_env_001_basic = 
      new RendererAttribStack.Cfg("env-basic", null,
        // Color
        new RendererAttrib.AColor.Cfg[] {
            new RendererAttrib.AColor.Cfg(ColorAttribute.AmbientLight, new Color(0.6f, 0.6f, 0.6f, 1.0f)),
            new RendererAttrib.AColor.Cfg(ColorAttribute.Fog,          new Color(0.0f, 0.0f, 0.0f, 1.0f)),
        },

        // Dir-light
        new RendererAttrib.ADirLight.Cfg[] {
            new RendererAttrib.ADirLight.Cfg(1, new Color(0.8f, 0.8f, 0.8f, 1.0f), new Vector3(-0.9f, -0.6f, -0.1f)),
            new RendererAttrib.ADirLight.Cfg(2, new Color(1.0f, 0.7f, 0.2f, 1.0f), new Vector3(0.6f, -0.1f, 0.2f)),
          },

        // Texture/Int/Float/Blending
        null, null, null, null
    );
    public transient RendererAttribStack.Cfg render_env_002_basic_specular = 
      new RendererAttribStack.Cfg("env-basic-specular", render_env_001_basic,
        // Color
        null,

        // Dir-light
        new RendererAttrib.ADirLight.Cfg[] {
          new RendererAttrib.ADirLight.Cfg(10, new Color(1.0f, 1.0f, 1.0f, 1.0f), new Vector3(1.0f, -1.0f, 0.0f)),
        },

        // Texture/Int/Float/Blending
        null, null, null, null
    );
    public transient RendererAttribStack.Cfg render_env_003_pfx = 
      new RendererAttribStack.Cfg("env-pfx", null,
        // Color
        new RendererAttrib.AColor.Cfg[] {
            new RendererAttrib.AColor.Cfg(ColorAttribute.AmbientLight, new Color(1.0f, 1.0f, 1.0f, 1.0f)),
        },

        // Dir-light
        null,

        // Texture/Int/Float/Blending
        null, null, null, null
    );
    public transient RendererAttribStack.Cfg render_mat_water = 
        new RendererAttribStack.Cfg("mat-water", null,
          // Color/Dir-light
          null, null,

          // Texture
          new RendererAttrib.ATexture.Cfg[] {
              new RendererAttrib.ATexture.Cfg(TextureAttribute.Diffuse, "water"),
          },

          // Int
          new RendererAttrib.AInt.Cfg[] {
              new RendererAttrib.AInt.Cfg(IntAttribute.CullFace, GL20.GL_BACK),
          }, 

          // Float
          new RendererAttrib.AFloat.Cfg[] {
              new RendererAttrib.AFloat.Cfg(FloatAttribute.AlphaTest, 0.1f),
          }, 

          // Blending
          new RendererAttrib.ABlending.Cfg[] {
              new RendererAttrib.ABlending.Cfg(BlendingAttribute.Type, false, 1.0f, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
          }
    );

    public transient Map<String, RendererAttribStack.Cfg> renderer_attrib_stacks = 
      new TreeMap<String, RendererAttribStack.Cfg>(); {
        renderer_attrib_stacks.put(render_env_001_basic.name,          render_env_001_basic);
        renderer_attrib_stacks.put(render_env_002_basic_specular.name, render_env_002_basic_specular);
        renderer_attrib_stacks.put(render_env_003_pfx.name,            render_env_003_pfx);
        renderer_attrib_stacks.put(render_mat_water.name,              render_mat_water);
    }
    public transient HashMap<String, String> render_env_layers = new HashMap<String, String>(); {
        render_env_layers.put("render-opaque-terrain",       "env-basic");
        render_env_layers.put("render-opaque-water",         "env-basic-specular");
        render_env_layers.put("render-opaque-sky",           null);
        render_env_layers.put("render-transparent-terrain",  "env-basic");
        render_env_layers.put("render-transparent-item",     "env-basic");
        render_env_layers.put("render-particles",            "env-pfx");
        render_env_layers.put("render-transparent-fog",      null);
        render_env_layers.put("render-billboard",            null);
        render_env_layers.put("render-overlay-dbg",          null);
    }
}
