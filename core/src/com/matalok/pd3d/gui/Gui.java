/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

//------------------------------------------------------------------------------
package com.matalok.pd3d.gui;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.InputMan;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformUtils;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class Gui 
  extends GameNode 
  implements InputMan.IClient {
    //**************************************************************************
    // TablePad
    //**************************************************************************
    public static class TablePad {
        //----------------------------------------------------------------------
        public static TablePad CreateAbs(float top, float left, float bottom, 
          float right) {
            return new TablePad(top, left, bottom, right);
        }

        //----------------------------------------------------------------------
        public static TablePad CreateAbs(float value) {
            return new TablePad(value, value, value, value);
        }

        //----------------------------------------------------------------------
        public static TablePad CreateRel(float origin, float left, float right, 
          float top, float bottom) {
            return new TablePad(origin * top, origin * left, origin * bottom, origin * right);
        }

        //----------------------------------------------------------------------
        public static TablePad CreateRel(float origin, float value) {
            value *= origin;
            return new TablePad(value, value, value, value);
        }

        //----------------------------------------------------------------------
        public float top, left, bottom, right;

        //----------------------------------------------------------------------
        public TablePad(float top, float left, float bottom, float right) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        //----------------------------------------------------------------------
        public void Apply(VisTable table) {
            table.pad(top, left, bottom, right);
        }
    }

    //**************************************************************************
    // Gui
    //**************************************************************************
    private Stage m_stage;
    private int m_skin_idx;

    //--------------------------------------------------------------------------
    public Gui() {
        super("gui2d", 1.0f);

        // Stage
        m_stage = new Stage(new ScreenViewport());
        m_stage.setDebugAll(Main.inst.cfg.dbg_gui);

        // Register input
        Main.inst.input_man.RegisterClient(this, "gui");

        // Select initial scaling
        m_skin_idx = -1;
        Scale(GetFontScale(), GetIconScale(), false);
    }

    //--------------------------------------------------------------------------
    public Skin LoadSkin(int idx) {
        if(m_skin_idx != idx) {
            String path = "skins/x" + idx + "/uiskin.json";
            Logger.d("Loading GUI sking :: idx=%d path=%s", idx, path);
            Utils.Assert(PlatformUtils.OpenInternalFile(path, false).exists(), 
              "Failed to load GUI skin, wrong path :: path=%s", path);
            if(VisUI.isLoaded()) {
                VisUI.dispose();
            }
            VisUI.load(path);
            m_skin_idx = idx;
        }
        return GetSkin();
    }

    //--------------------------------------------------------------------------
    public Skin GetSkin() {
        return VisUI.getSkin();
    }

    //--------------------------------------------------------------------------
    public void RegisterClient(Actor client) {
        Logger.d("Registering GUI client :: client=%s", 
          client.getClass().getSimpleName());
        m_stage.addActor(client);
    }

    //--------------------------------------------------------------------------
    public void UnregisterClient(Actor client) {
        Logger.d("Unregistering GUI client :: client=%s", 
          client.getClass().getSimpleName());
        client.remove();
    }

    //--------------------------------------------------------------------------
    public boolean IsOnStage(Actor actor) {
        return m_stage.getRoot().isAscendantOf(actor);
    }

    //--------------------------------------------------------------------------
    public void SetScrollFocus(Actor actor) {
        m_stage.setScrollFocus(actor);
    }

    //--------------------------------------------------------------------------
    public void SetKeyboardFocus(Actor actor) {
        m_stage.setKeyboardFocus(actor);
    }

    //--------------------------------------------------------------------------
    public boolean IsLandscape() {
        return Main.inst.renderer.IsLandscape();
    }

    //--------------------------------------------------------------------------
    public float GetWidth(boolean is_real) {
        int w_real = Main.inst.renderer.GetWidth();
        int w_static = Main.inst.cfg.gui_static_screen_width;
        if(is_real || w_real < w_static) {
            return Main.inst.renderer.GetWidth();
        } else {
            return IsLandscape() ? 
              Main.inst.cfg.gui_static_screen_width : 
              Main.inst.cfg.gui_static_screen_height;
        }
    }

    //--------------------------------------------------------------------------
    public float GetHeight(boolean is_real) {
        int h_real = Main.inst.renderer.GetHeight();
        int h_static = Main.inst.cfg.gui_static_screen_height;
        if(is_real || h_real < h_static) {
            return Main.inst.renderer.GetHeight();
        } else {
            return IsLandscape() ? 
              Main.inst.cfg.gui_static_screen_height : 
              Main.inst.cfg.gui_static_screen_width;
        }
    }

    //--------------------------------------------------------------------------
    public float GetWidth(boolean is_real, float val) {
        return GetWidth(is_real) * val;
    }

    //--------------------------------------------------------------------------
    public float GetHeight(boolean is_real, float val) {
        return GetHeight(is_real) * val;
    }

    //--------------------------------------------------------------------------
    public float GetSmallest(boolean is_real, float val) {
        return IsLandscape() ? 
          GetHeight(is_real, val) : GetWidth(is_real, val);
    }

    //--------------------------------------------------------------------------
    public float GetBiggest(boolean is_real, float val) {
        return !IsLandscape() ? 
          GetHeight(is_real, val) : GetWidth(is_real, val);
    }

    //--------------------------------------------------------------------------
    public int GetFontScale() {
        return Main.inst.cfg.gui_font_scale;
    }

    //--------------------------------------------------------------------------
    public float GetIconScale() {
        return Main.inst.cfg.gui_icon_scale;
    }

    //--------------------------------------------------------------------------
    public void Scale(Integer font_scale, Float icon_scale, boolean update_gui) {
        Logger.d("Scaling GUI :: font=%s icon=%s", 
          (font_scale == null) ? "none" : Integer.toString(font_scale),
          (icon_scale == null) ? "none" : Float.toString(icon_scale));

        // Skin scaling
        if(font_scale != null) { 
            Main.inst.cfg.gui_font_scale = font_scale;
            LoadSkin(font_scale);
        }

        // Icon scaling
        if(icon_scale != null) {
            Main.inst.cfg.gui_icon_scale = icon_scale;
        }

        // Update GUI by faking resize event
        if(update_gui) {
            Main.inst.FakeResize();
        }
        Main.inst.SaveConfig();
    }

    //**************************************************************************
    // InputMan.IClient
    //**************************************************************************
    @Override public InputProcessor GetInputProcessor() {
        return m_stage;
    }

    //--------------------------------------------------------------------------
    @Override public GestureDetector GetGestureDetector() {
        return null;
    }

    //--------------------------------------------------------------------------
    @Override public void Process() {
    }

    //**************************************************************************
    // METHODS
    //**************************************************************************
    @Override public boolean OnMethodResize(int width, int height) {
        super.OnMethodResize(width, height);

        m_stage.getViewport().update(width, height, true);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodPostRender(RenderCtx ctx, boolean pre_children) {
        super.OnMethodPostRender(ctx, pre_children);

        // Ignore post-children
        if(!pre_children) {
            return true;
        }

        m_stage.act(Gdx.graphics.getDeltaTime());
        m_stage.draw();
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        VisUI.dispose();
        m_stage.dispose();
        return true;
    }
}
