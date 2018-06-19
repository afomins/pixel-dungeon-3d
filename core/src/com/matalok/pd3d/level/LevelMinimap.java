//------------------------------------------------------------------------------
package com.matalok.pd3d.level;

//------------------------------------------------------------------------------
import java.util.Arrays;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.renderable.RenderableObjectType;
import com.matalok.pd3d.shared.UtilsClass.Vector2i;
import com.matalok.scenegraph.SgUtils.IManaged;

//------------------------------------------------------------------------------
public class LevelMinimap 
 extends GameNode {
    //**************************************************************************
    // Brush
    //**************************************************************************
    private static class Brush {
        //----------------------------------------------------------------------
        int color;
        char symbol;

        //----------------------------------------------------------------------
        public Brush(Color color, float alpha, char symbol) {
            this.color = (Color.rgba8888(color) & 0xffffff00);
            this.color |= (int)(255 * alpha);
            this.symbol = symbol;
        }
    }

    //**************************************************************************
    // Map
    //**************************************************************************
    private static class Map 
      implements IManaged{
        //----------------------------------------------------------------------
        public Pixmap pm;
        public char ascii[];

        //----------------------------------------------------------------------
        void UpdateSize(int width, int height, boolean do_reset) {
            if(pm == null || pm.getWidth() != width || pm.getHeight() != height) {
                if(pm != null) {
                    pm.dispose();
                }
                pm = new Pixmap(width, height, Format.RGBA8888);
                ascii = new char[width * height];
                do_reset = true;
            }

            if(do_reset) {
                pm.setColor(Main.inst.cfg.minimap_fill_color);
                pm.fill();
                Arrays.fill(ascii, ' ');
            }
        }

        //----------------------------------------------------------------------
        void Update(int idx, Brush brush) {
            if(brush == null) {
                return;
            }
            int x = idx % pm.getWidth(), y = idx / pm.getWidth();
            pm.drawPixel(x, y, brush.color);
            ascii[idx] = brush.symbol;
        }

        //----------------------------------------------------------------------
        @Override public void OnCleanup() {
            pm.dispose();
            pm = null;
            ascii = null;
        }
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static int GetBrushKey(RenderableObjectType model_type, int model_id) {
        return (((model_type.ordinal() & 0xff) << 0) |
                ((model_id             & 0xff) << 8));
    }

    //--------------------------------------------------------------------------
    private static Brush empty_brush = new Brush(new Color(), 0.0f, ' ');

    //**************************************************************************
    // LevelMinimap
    //**************************************************************************
    private Map m_terrain;
    private HashMap<Integer, Brush> m_brushes;
    private Vector2i m_center;
    private Texture m_texture;

    //--------------------------------------------------------------------------
    public LevelMinimap() {
        super("minimap", 0.0f);

        // Center of the map
        m_center = new Vector2i();

        // Terrain brushes
        float alpha = Main.inst.cfg.minimap_alpha;
        m_brushes = new HashMap<Integer, Brush>();
//        Color floor = Color.DARK_GRAY;
        InitBrush(RenderableObjectType.FLOOR, 0, Color.DARK_GRAY, alpha, '.');
        InitBrush(RenderableObjectType.EMPTY_SPACE, 0, Color.DARK_GRAY, alpha, '.');
        InitBrush(RenderableObjectType.WALL, 0, Color.YELLOW, alpha, '#');
        InitBrush(RenderableObjectType.DOOR_FRAME, 0, Color.LIGHT_GRAY, alpha, '~');
        InitBrush(RenderableObjectType.BARRICADE, 0, Color.LIGHT_GRAY, alpha, '~');
        InitBrush(RenderableObjectType.BOOK_SHELF, 0, Color.LIGHT_GRAY, alpha, '~');
        InitBrush(RenderableObjectType.ENTRANCE, 0,  Color.CYAN, alpha, '^');
        InitBrush(RenderableObjectType.EXIT, 0, Color.GREEN, alpha, 'v');

        // Overlay brushes
        InitBrush(RenderableObjectType.CHAR, MapEnum.CharType.WARRIOR0.ordinal(),
          Color.WHITE, 1.0f, '@');
        InitBrush(RenderableObjectType.CHAR, MapEnum.CharType.RAT.ordinal(),
          Color.RED, alpha, '&');
        InitBrush(RenderableObjectType.CHAR, MapEnum.CharType.BLACKSMITH.ordinal(),
          Color.GREEN, alpha, '%');

        // Maps
        m_terrain = new Map();
    }

    //--------------------------------------------------------------------------
    public Texture GetTexture() {
        return m_texture;
    }

    //--------------------------------------------------------------------------
    public Vector2i GetCenter() {
        return m_center;
    }

    //--------------------------------------------------------------------------
    public void SetCenter(int idx) {
        m_center.Set(
          idx % m_terrain.pm.getWidth(), idx / m_terrain.pm.getWidth());
    }

    //--------------------------------------------------------------------------
    private void InitBrush(RenderableObjectType model_type, int model_id, 
      Color color, float alpha, char symbol) {
        m_brushes.put(GetBrushKey(model_type, model_id), new Brush(color, alpha, symbol));
    }

    //--------------------------------------------------------------------------
    public void SetSize(int width, int height) {
        m_terrain.UpdateSize(width, height, true);
    }

    //--------------------------------------------------------------------------
    public void UpdateTerrain(int idx, RenderableObjectType obj_type, int obj_id) {
        m_terrain.Update(idx, (obj_type == null) ? 
          empty_brush : m_brushes.get(GetBrushKey(obj_type, obj_id)));
    }

    //--------------------------------------------------------------------------
    public void UpdateChar(int idx, RenderableObjectType obj_type, int obj_id) {
        m_terrain.Update(idx, (obj_type == null) ? 
          empty_brush : m_brushes.get(GetBrushKey(obj_type, obj_id)));
    }

    //--------------------------------------------------------------------------
    public void Update() {
        if(m_texture != null) {
            m_texture.dispose();
        }
        m_texture = new Texture(m_terrain.pm);
    }

    //**************************************************************************
    // IManaged
    //**************************************************************************
    public boolean OnMethodJson(JsonCtx ctx) {
        super.OnMethodJson(ctx);

        int w = m_terrain.pm.getWidth(), h = m_terrain.pm.getHeight();
        if(ctx.pre_children && 
           ctx.targets.contains(JsonTarget.COMMON)) {
            ctx.Write("\"map-width\" : %d, \"map-height\" : %d,", w, h);
            for(int y = 0 ; y < h; y++) {
                String line = "";
                for(int x = 0; x < w; x++) {
                    line += m_terrain.ascii[x + y * h];
                }

                if(y == 0) {
                    ctx.Write("\"map\" : [ \"%s\",", line);
                } else if (y == h - 1) {
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
        m_brushes.clear();
        m_brushes = null;

        m_terrain.OnCleanup();
        return true;
    }
}
