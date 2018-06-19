//------------------------------------------------------------------------------
package com.matalok.pd3d.renderable.model.template;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.matalok.pd3d.renderable.model.ModelAnim;
import com.matalok.pd3d.renderer.RendererModel;

//------------------------------------------------------------------------------
public class MTmplAxis 
  extends MTmpl {
    // *************************************************************************
    // TemplateAxis
    // *************************************************************************
    public MTmplAxis(String name) {
        super(name, null, false);

        // Define axis
        Object[] params = new Object[] {
            new Object[] { Vector3.X, 
              RendererModel.CreateMaterial(Color.RED, null, false, false)},
            new Object[] { Vector3.Y,
              RendererModel.CreateMaterial(Color.GREEN, null, false, false)},
            new Object[] { Vector3.Z,
              RendererModel.CreateMaterial(Color.BLUE, null, false, false)},
        };

        // Default animation
        ModelAnim.Template anim_t = SetAnim("idle", 1, false);

        // Model builder
        ModelBuilder model_builder = new ModelBuilder();
        model_builder.begin();

        // Create axis lines
        for(Object param : params) {
            Object[] __param = (Object[]) param; 
            Vector3 axis = (Vector3)__param[0];
            Material material = (Material)__param[1];

            MeshPartBuilder part_builder = model_builder.part(
              "line", GL20.GL_LINES, Usage.Position, material);
            part_builder.line(Vector3.Zero, axis);
        }

        // Create axis model
        anim_t.AddFrame(model_builder.end(), 0);
    }
}
