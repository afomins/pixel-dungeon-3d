//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererModel;
import com.matalok.pd3d.renderer.RendererTexture;
import com.matalok.pd3d.desc.DescAnim;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.renderable.model.ModelAnim;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class MTmplSprite 
  extends MTmpl {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static void CreatePlane(GeomBuilder builder, long attributes, Material material, 
      float width, float height, DescRect rect, int shadow_num, float shadow_depth, 
      boolean do_backface, Matrix4 transform_base) {
        CreatePlane(builder, attributes, material, width, height, 
          rect.unit_x, rect.unit_y, rect.unit_width, rect.unit_height,
          shadow_num, shadow_depth, do_backface, transform_base);
    }

    //--------------------------------------------------------------------------
    public static void CreatePlane(GeomBuilder builder, long attributes, Material material, 
      float width, float height, float u, float v, float size_u, float size_v,
      int shadow_num, float shadow_depth, boolean do_backface, Matrix4 transform_base) {

        Matrix4 transform = (transform_base == null) ? 
          null : new Matrix4(transform_base);

        for(int j = 0; j < 2; j++) {
            // Front plane
            builder.InitVertex4(width, height, 0.0f, 0.0f, 0.0f, transform);
            builder.CreatePlane(attributes, material, u, v, size_u, size_v, false, transform);

            // Shadow is located at the lower half of texture 
            float vv = v + 0.5f;

            // Shadow planes
            float shadow_step = (shadow_num == 0) ? 0 : shadow_depth / shadow_num;
            for(int i = 0; i < shadow_num; i++) {
                builder.InitVertex4(width, height, 0.0f, -(i + 1) * shadow_step, 0.0f, transform);
                builder.CreatePlane(attributes, material, u, vv, size_u, size_v, false, transform);
            }

            // Repeat planes in reverse for backface
            if(do_backface) {
                if(transform_base == null) {
                    transform = new Matrix4();
                } else {
                    transform.set(transform_base);
                }
                transform
                  .translate(0.0f, -shadow_depth, 0.0f)
                  .rotate(Vector3.Z, 180);
            } else {
                break;
            }
        }
    }

    //**************************************************************************
    // TemplateSprite
    //**************************************************************************
    private DescSprite m_sprite;
    private boolean m_is_unit_size;
    private Color m_color;

    //--------------------------------------------------------------------------
    public MTmplSprite(String tx_cache_key, DescSprite sprite, Float alpha, 
      boolean has_runtime_transform, boolean is_unit_size, Color color) {
        super(tx_cache_key, alpha, has_runtime_transform);
        m_sprite = sprite;
        m_is_unit_size = is_unit_size;
        m_color = color;
    }

    //--------------------------------------------------------------------------
    public MTmplSprite Build() {
        String tx_cache_key = this.name;
        Logger.d("Creating sprite template :: id=%d texture=%s anim-num=%d", 
          m_sprite.obj_id, tx_cache_key, m_sprite.anims.size());
//        for(AnimDesc anim : sprite.anims.values()) {
//            Logger.d(" >> anim=%s fps=%d is-looped=%s frames=%s", 
//              anim.name, anim.fps, Boolean.toString(anim.is_looped), 
//              anim.frames.toString());
//        }

        // Animation should be present
        if(m_sprite.anims.size() == 0) {
            Logger.e("Failed to create sprite model :: animation is missing");
            return this;
        }

        // Get texture from cache
        Renderer.TxCache tx_cache = Main.inst.renderer.GetTxCache();
        RendererTexture texture = tx_cache.Get(tx_cache_key);
        Utils.Assert(texture != null, "Failed to create sprite template, not texture");

        // Create material
        Material material = 
          RendererModel.CreateMaterial(m_color, texture.tx, true, true);

        // Get number of tiled frames in row
        int tile_width = (m_sprite.tile_width != null) ? m_sprite.tile_width : 0;
        int tile_offset = (m_sprite.tile_offset != null) ? m_sprite.tile_offset : 0;
        int tiles_per_row = (tile_width > 0) ? 
          texture.tx.getWidth() / tile_width : 0;

        // Create model for each animation frame
        GeomBuilder builder = Main.inst.geom_builder;
        for(DescAnim anim : m_sprite.anims.values()) {
            // Model animation
            ModelAnim.Template anim_t = 
              SetAnim(anim.name, anim.fps, anim.is_looped);

            // Walk animation frames
            for(Integer frame_idx : anim.frames) {
                int rect_idx = frame_idx + tiles_per_row * tile_offset;
                DescRect rect = m_sprite.rects.get(rect_idx);
                Utils.Assert(rect != null, 
                  "Failed to create sprite template, wrong rect idx :: anim=%s frame-idx=%d texture-key=%d",
                  anim.name, frame_idx, m_sprite.tile_offset);

                // Get aspect ratio of the sprite
                float width = (float)rect.width;
                float height = (float)rect.height;
                if(m_is_unit_size) {
                    width = 1.0f;
                    height = (float)rect.height / rect.width;
                }

                // Create model
                BuildMesh(builder, 
                  Usage.Position | Usage.Normal | Usage.TextureCoordinates, 
                  material, width, height, rect);
                anim_t.AddFrame(builder.FinalizeModel(), builder.GetTriangleNum());
            }
        }
        return this;
    }

    //--------------------------------------------------------------------------
    protected void BuildMesh(GeomBuilder builder, long attributes, Material material, 
      float width, float height, DescRect rect) {
        MTmplSprite.CreatePlane(builder, attributes, material, 
          width, height, rect, 2, 0.1f, true, null);
    }

    //**************************************************************************
    // Template
    //**************************************************************************
    @Override public void UpdateInitialTransform() {
        init_transform.idt();
    }
}
