//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

//------------------------------------------------------------------------------
public class GuiSpinnerFloat 
  extends Spinner {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //----------------------------------------------------------------------
        public void OnValueChanged(float value) {};
    }

    //**************************************************************************
    // GuiSpinnerFloat
    //**************************************************************************
    public GuiSpinnerFloat(String label, float init, float step, int precission,
      final Listener listener) {
        this(label, init, -42424242, +42424242, step, precission, listener);
    }

    //--------------------------------------------------------------------------
    public GuiSpinnerFloat(String label, float init, float min, float max, 
      float step, int precission, final Listener listener) {
        super(label, new SimpleFloatSpinnerModel(init, min, max, step, precission));
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if(listener != null) {
                    listener.OnValueChanged(GetValue());
                }
            }
        });
    }

    //--------------------------------------------------------------------------
    public float GetValue() {
        return ((SimpleFloatSpinnerModel)getModel()).getValue();
    }

    //--------------------------------------------------------------------------
    public void SetValue(float value, boolean fire_event) {
        ((SimpleFloatSpinnerModel)getModel()).setValue(value, fire_event);
    }
}
