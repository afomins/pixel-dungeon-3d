//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//------------------------------------------------------------------------------
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.renderer.RendererAttrib;

//------------------------------------------------------------------------------
public class GuiAttribTexture 
  extends GuiAttrib {
    //**************************************************************************
    // GuiAttribTexture
    //**************************************************************************
    public GuiAttribTexture(RendererAttrib.ATexture.Cfg cfg, 
      GuiAttrib.Listener listener) {
        super(RendererAttrib.Type.TEXTURE, cfg, listener);
    }

    //**************************************************************************
    // GuiAttrib
    //**************************************************************************
    @Override public void OnCreateBody() {
        Set<String> names = Main.inst.renderer.GetTxCache().GetKeys();
        final RendererAttrib.ATexture.Cfg cfg = (RendererAttrib.ATexture.Cfg)m_cfg;

        if(!names.contains(cfg.name)) {
            cfg.name = "default";
        }

        final GuiSelectBox sbox = add(new GuiSelectBox(names.toArray(new String[]{}))
          .SetName(cfg.name, false)).expandX().fillX().getActor();
        sbox.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                cfg.name = sbox.GetName();
                OnUpdate();
            }
        });
    }
}
