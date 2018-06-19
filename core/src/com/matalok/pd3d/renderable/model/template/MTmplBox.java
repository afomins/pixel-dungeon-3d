//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import java.util.EnumSet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.matalok.pd3d.GeomBuilder;
import com.matalok.pd3d.renderable.model.ModelAnim;
import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class MTmplBox 
  extends MTmpl {
    // *************************************************************************
    // TemplateBox
    // *************************************************************************
    public MTmplBox(String name, boolean is_solid, 
      EnumSet<GeomBuilder.CubeSide> sides, float width, float height, float depth, 
      Color color, Float alpha) {
        super(name, alpha, false);

        // Create single idle animation
        ModelAnim.Template anim_t = SetAnim("idle", 1, false);

        // Create box material
        Material material = 
          RendererModel.CreateMaterial(color, null, true, (alpha != null));

        // Solid or wireframe?
        int primitive_type = GL20.GL_LINES;
        long attributes = Usage.Position;
        int triangle_num = 0;
        if(is_solid) {
            primitive_type = GL20.GL_TRIANGLES;
            attributes = Usage.Position | Usage.Normal;
            triangle_num = ModelAnim.BOX_TR_NUM;
        }

        // Create box model
        Model model = null;
        if(sides == null || sides.size() == 6) {
            model = new ModelBuilder().createBox(
              width, height, depth, primitive_type, material, attributes);
        } else {
            // TODO
        }

        // Add frame
        anim_t.AddFrame(model, triangle_num);
    }
}
