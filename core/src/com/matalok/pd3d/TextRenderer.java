//------------------------------------------------------------------------------
package com.matalok.pd3d;

//-----------------------------------------------------------------------------
import java.util.HashMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class TextRenderer 
  extends GameNode {
    //**************************************************************************
    // TextureDesc
    //**************************************************************************
    public class TextureDesc {
        //----------------------------------------------------------------------
        public FrameBuffer fbo;
        public float u, v, u_size, v_size;

        //----------------------------------------------------------------------
        public TextureDesc(GlyphLayout layout, FrameBuffer fbo) {
            this.fbo = fbo;
            this.u = 0.0f; 
            this.v = 1.0f; // Flip horizontally
            this.u_size = layout.width / fbo.getWidth();
            this.v_size = -layout.height / fbo.getHeight();
        }

        //----------------------------------------------------------------------
        public Texture GetTexture() {
            return fbo.getColorBufferTexture();
        }
    }

    //**************************************************************************
    // FontDesc
    //**************************************************************************
    public class FontDesc {
        //----------------------------------------------------------------------
        public BitmapFont font;
        public boolean own;
        public HashMap<String, TextureDesc> text_cache;

        //----------------------------------------------------------------------
        public FontDesc(BitmapFont font, boolean own) {
            this.font = font;
            this.own = own;
            this.text_cache = new HashMap<String, TextureDesc>();
        }
    }

    //**************************************************************************
    // TextRenderer
    //**************************************************************************
    private HashMap<String, FontDesc> m_fonts;
    private SpriteBatch m_sprite_batch;

    //--------------------------------------------------------------------------
    public TextRenderer() {
        super("text-renderer", 1.0f);
        m_fonts = new HashMap<String, FontDesc>();
        m_sprite_batch = new SpriteBatch();
    }

    //--------------------------------------------------------------------------
    public void Add(String name, BitmapFont font, boolean own) {
        Logger.d("Caching font :: name=%s own=%s", name, own);
        m_fonts.put(name, new FontDesc(font, own));
    }

    //--------------------------------------------------------------------------
    public void Add(String name, String font_path) {
        BitmapFont font = new BitmapFont(
          PlatformUtils.OpenInternalFile(font_path, false));
        Add(name, font, true);
    }

    //--------------------------------------------------------------------------
    public TextureDesc RenderText(String name, String text) {
        Utils.Assert(m_fonts.containsKey(name), 
          "Failed to render text, no font :: font=%s", name);

        // Sea
        FontDesc font_desc = m_fonts.get(name);
        TextureDesc txt_cache = font_desc.text_cache.get(text);
        if(txt_cache == null) {
            txt_cache = RenderText(font_desc.font, text);
            font_desc.text_cache.put(text, txt_cache);
        }
        return txt_cache;
    }

    //--------------------------------------------------------------------------
    public TextureDesc RenderText(BitmapFont font, String text) {
        // Get layout of the text
        BitmapFontCache font_cache = font.getCache();
        font_cache.clear();
        GlyphLayout layout = font_cache.addText(text, 0, 0);

        // Create FBO that fits text layout
        FrameBuffer fbo = new FrameBuffer(Format.RGBA8888,
          MathUtils.nextPowerOfTwo((int)layout.width), 
          MathUtils.nextPowerOfTwo((int)layout.height), false);

        // Render text
        fbo.begin();
            Gdx.gl.glViewport(0, 0, fbo.getWidth(), fbo.getHeight());
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            m_sprite_batch.getProjectionMatrix().setToOrtho2D(0, 0, fbo.getWidth(), fbo.getHeight());
            m_sprite_batch.begin();
                font.setColor(Color.WHITE);
                font.draw(m_sprite_batch, text, 0, fbo.getHeight());
            m_sprite_batch.end();
        fbo.end();
        return new TextureDesc(layout, fbo);
    }

    //--------------------------------------------------------------------------
    public void Clear() {
        for(FontDesc font : m_fonts.values()) {
            for(TextureDesc tx_desc : font.text_cache.values()) {
                tx_desc.fbo.dispose();
            }
            if(font.own) {
                font.font.dispose();
            }
            font.font = null;
            font.text_cache.clear();
            font.text_cache = null;
        }
        m_fonts.clear();
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        Clear();
        m_fonts = null;
        return true;
    }
}
