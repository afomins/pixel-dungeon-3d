//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;

//------------------------------------------------------------------------------
public class GuiSpinnerInt 
  extends Spinner {
    //**************************************************************************
    // Listener
    //**************************************************************************
    public static abstract class Listener {
        //......................................................................
        public void OnValueChanged(int value) {};
    }

    //**************************************************************************
    // GuiSpinnerInt
    //**************************************************************************
    public GuiSpinnerInt(String label, int init, int step, 
      final Listener listener) {
        this(label, init, -42424242, +42424242, step, listener);
    }

    //--------------------------------------------------------------------------
    public GuiSpinnerInt(String label, int init, int min, int max, 
      int step, final Listener listener) {
        super(label, new IntSpinnerModel(init, min, max, step));
        addListener(new ChangeListener() {
            @Override public void changed (ChangeEvent event, Actor actor) {
                if(listener != null) {
                    listener.OnValueChanged(GetValue());
                }
            }
        });
    }

    //--------------------------------------------------------------------------
    public int GetValue() {
        return ((IntSpinnerModel)getModel()).getValue();
    }

    //--------------------------------------------------------------------------
    public void SetValue(int val) {
        ((IntSpinnerModel)getModel()).setValue(val);
    }
}
