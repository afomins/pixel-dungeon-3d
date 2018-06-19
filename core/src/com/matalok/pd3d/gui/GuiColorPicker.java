//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

//------------------------------------------------------------------------------
public class GuiColorPicker 
  extends VisTable {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static final Drawable white = VisUI.getSkin().getDrawable("white");

    //--------------------------------------------------------------------------
    private Cell<GuiButton> m_button;
    private ColorPicker m_color_picker;
    private Color m_target;

    //**************************************************************************
    // GuiColorPicker
    //**************************************************************************
    public GuiColorPicker(String text, float width, float height, Color target) {
        m_target = target;

        // Color picker window
        m_color_picker = new ColorPicker(text, new ColorPickerAdapter() {
            @Override public void canceled (Color oldColor) {
                UpdateColor(oldColor);
            }
            @Override public void changed (Color newColor) {
                UpdateColor(newColor);
            }
            @Override public void finished (Color newColor) {
                UpdateColor(newColor);
            }
        });
        m_color_picker.setColor(target);

        // Label
        add(new VisLabel(text + ": "));

        // Button
        m_button = add(new GuiButton(null, null, width, height, null, null, false, 
          new ChangeListener() {
              @Override public void changed(ChangeEvent event, Actor actor) {
                  getStage().addActor(m_color_picker.fadeIn());
              }})
        );
        GuiImage img = m_button.getActor().GetImage();
        img.setDrawable(white);
        UpdateColor(target);
    }

    //--------------------------------------------------------------------------
    private void UpdateColor(Color color) {
        if(m_button == null) {
            return;
        }
        m_button.getActor().GetImage().setColor(color);
        m_target.set(color);
    }
}
