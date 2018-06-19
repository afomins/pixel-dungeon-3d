//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTextButton;

//------------------------------------------------------------------------------
public class GuiButtonChangeVector3
  extends VisTextButton {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static GuiWndAttribVector3 vector3_wnd = null;

    //--------------------------------------------------------------------------
    public static String Vector3toStr(Vector3 value) {
        return String.format("%.2f,%.2f,%.2f", value.x, value.y, value.z);
    }

    //**************************************************************************
    // GuiButtonChangeVector3
    //**************************************************************************
    public GuiButtonChangeVector3(final Vector3 value, final GuiAttrib gui_attrib) {
        super(Vector3toStr(value));

        // Show color picker window when button is pressed
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if(vector3_wnd == null) {
                    vector3_wnd = new GuiWndAttribVector3();
                };

                vector3_wnd.Init(value, 
                  new GuiWndAttribVector3.Listener() {
                      @Override public void OnUpdate(Vector3 v) {
                          value.set(v);
                          setText(Vector3toStr(v));
                          gui_attrib.OnUpdate();
                      };
                });
                getStage().addActor(vector3_wnd.fadeIn());
            }
        });
    }
}
