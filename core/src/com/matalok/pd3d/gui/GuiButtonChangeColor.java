//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

//------------------------------------------------------------------------------
public class GuiButtonChangeColor
  extends GuiButton {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static Drawable white = VisUI.getSkin().getDrawable("white");
    private static ColorPicker color_picker_wnd = null;

    //**************************************************************************
    // GuiButtonColorPicker
    //**************************************************************************
    public GuiButtonChangeColor(final Color color, final GuiAttrib gui_attrib) {
        super(null, null, 16.0f, 16.0f, null, null, false, null);

        // Show window
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if(color_picker_wnd == null) {
                    color_picker_wnd = new ColorPicker();
                }

                color_picker_wnd.setListener(null);
                color_picker_wnd.setColor(color);
                color_picker_wnd.setListener(new ColorPickerAdapter() {
                    @Override public void canceled(Color oldColor) {
                        finished(oldColor);
                    }
                    @Override public void changed(Color newColor) {
                        finished(newColor);
                    }
                    @Override public void finished(Color newColor) {
                        GetImage().setColor(newColor);
                        color.set(newColor);
                        gui_attrib.OnUpdate();
                    }
                });
                getStage().addActor(color_picker_wnd.fadeIn());
            }
        });

        // Update after creation
        GetImage().setDrawable(white);
        GetImage().setColor(color);
    }
}
