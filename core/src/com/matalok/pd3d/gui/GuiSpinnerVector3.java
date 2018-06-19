//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.math.Vector3;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

//------------------------------------------------------------------------------
public class GuiSpinnerVector3 
  extends VisTable {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //......................................................................
        public void OnValueChanged() {};
    }

    //**************************************************************************
    // GuiSpinnerVector3
    //**************************************************************************
    public GuiSpinnerVector3(String label, final Vector3 target, float step, 
      final Listener listener) {
        add(new VisLabel(label));

        // X
        add(new GuiSpinnerFloat("x: ", target.x, step, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  target.x = value;
                  if(listener != null) {
                      listener.OnValueChanged();
                  }
              };
        }));

        // Y
        add(new GuiSpinnerFloat("y: ", target.y, step, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  target.y = value;
                  if(listener != null) {
                      listener.OnValueChanged();
                  }
              };
        }));

        // Z
        add(new GuiSpinnerFloat("z: ", target.z, step, 1,
          new GuiSpinnerFloat.Listener() {
              @Override public void OnValueChanged(float value) {
                  target.z = value;
                  if(listener != null) {
                      listener.OnValueChanged();
                  }
              };
        }));
    }
}
