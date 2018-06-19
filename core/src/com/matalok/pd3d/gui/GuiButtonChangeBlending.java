//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiButtonChangeBlending
  extends VisTextButton {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    private static GuiWndAttribBlending blending_wnd = null;

    //**************************************************************************
    // GuiButtonChangeBlending
    //**************************************************************************
    public GuiButtonChangeBlending(final GuiAttribBlending gui_attrib) {
        super("update");

        // Show window
        addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if(blending_wnd == null) {
                    blending_wnd = new GuiWndAttribBlending();
                };

                final RendererAttrib.ABlending.Cfg cfg = 
                  (RendererAttrib.ABlending.Cfg)gui_attrib.GetCfg();
                blending_wnd.Init(cfg.is_blended, cfg.alpha, cfg.src_func, cfg.dst_func, 
                  new GuiWndAttribBlending.Listener() {
                      @Override public void OnUpdate(boolean is_blended, 
                        float alpha, int src_func, int dst_func) {
                          cfg.is_blended = is_blended;
                          cfg.alpha = alpha;
                          cfg.src_func = src_func;
                          cfg.dst_func = dst_func;
                          setText("blending");
                          gui_attrib.OnUpdate();
                      };
                });
                getStage().addActor(blending_wnd.fadeIn());
            }
        });
    }
}
