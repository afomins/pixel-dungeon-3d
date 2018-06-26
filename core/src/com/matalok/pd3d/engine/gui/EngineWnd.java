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
package com.matalok.pd3d.engine.gui;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.PlatformUtils;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.gui.Gui;
import com.matalok.pd3d.gui.GuiBusyIndicator;
import com.matalok.pd3d.gui.GuiButton;
import com.matalok.pd3d.gui.GuiClickListener;
import com.matalok.pd3d.gui.GuiColorPicker;
import com.matalok.pd3d.gui.GuiImage;
import com.matalok.pd3d.gui.GuiLog;
import com.matalok.pd3d.gui.GuiMinimap;
import com.matalok.pd3d.gui.GuiProgressBar;
import com.matalok.pd3d.gui.GuiSpinnerEx;
import com.matalok.pd3d.gui.GuiSpinnerFloat;
import com.matalok.pd3d.gui.GuiSpinnerInt;
import com.matalok.pd3d.gui.GuiSpinnerVector3;
import com.matalok.pd3d.gui.GuiTextBox;
import com.matalok.pd3d.gui.GuiToggleButton;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.renderer.RendererTexture;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.UtilsClass;

//------------------------------------------------------------------------------
public class EngineWnd 
  extends VisWindow {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static Cell<? extends Actor> SetCellSize(Cell<? extends Actor> cell, 
      Float width, Float height) {
        if(width != null && width != 0.0f) cell.width(width);
        if(height != null && height != 0.0f) cell.height(height);
        return cell;
    }

    //**************************************************************************
    // InputAction
    //**************************************************************************
    public enum InputAction {
        IGNORE,
        CONSUME,
        POP_STATE
    }

    //**************************************************************************
    // Visibility
    //**************************************************************************
    public enum Visibility {
        INVISIBLE,
        FADING_IN,
        VISIBLE,
        FADING_OUT
    }

    //**************************************************************************
    // ClickListener
    //**************************************************************************
    public static class ClickListener 
      extends GuiClickListener {
        //----------------------------------------------------------------------
        private EngineWnd m_wnd;
        private boolean m_is_touch_up_event_hack;

        //----------------------------------------------------------------------
        public ClickListener(EngineWnd wnd) {
            m_wnd = wnd;
        }

        //----------------------------------------------------------------------
        public boolean IsClickAllowed() {
            return (m_wnd == null || 
              m_wnd.GetVisbilityState() == Visibility.VISIBLE);
        }

        //----------------------------------------------------------------------
        @Override public void OnPressed(boolean is_long) {
            if(!IsClickAllowed()) {
                return;
            }

            if(is_long) {
                OnPressedLong();
            } else {
                OnPressed();
            }
        }

        //----------------------------------------------------------------------
        @Override public void OnReleased(boolean is_long) {
            if(!IsClickAllowed()) {
                return;
            }

            if(is_long) {
                OnReleasedLong();
            } else {
                OnReleased();
            }
        }

        //----------------------------------------------------------------------
        public void OnPressed() {
        }

        //----------------------------------------------------------------------
        public void OnPressedLong() {
        }

        //----------------------------------------------------------------------
        public void OnReleased() {
        }

        //----------------------------------------------------------------------
        public void OnReleasedLong() {
        }

        //**********************************************************************
        // InputListener
        //**********************************************************************
        // 
        // "m_is_touch_up_event_hack" ensures that ClickListener::clicked() 
        // will be called from ClickListener::touchUp() even if mouse was 
        // moved and released outside of button area.
        @Override public void touchUp(InputEvent event, float x, float y, 
          int pointer, int button) {
            m_is_touch_up_event_hack = true;
            super.touchUp(event, x, y, pointer, button);
            m_is_touch_up_event_hack = false;
        }

        //----------------------------------------------------------------------
        @Override public boolean isOver(Actor actor, float x, float y)  {
            return m_is_touch_up_event_hack ? true : super.isOver(actor, x, y);
        }
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    private Visibility m_visibility;
    private boolean m_is_enabled;
    private boolean m_is_centered;
    private Vector2 m_fixed_size, m_max_size;
    private float m_alpha, m_background_alpha;
    private Enum<?> m_background_sprite;
    private boolean m_has_title;

    //--------------------------------------------------------------------------
    public EngineWnd(String title, boolean is_centered, boolean is_modal, 
      float fixed_width, float fixed_height,
      final InputAction on_touch_in_area, final InputAction on_touch_out_area, 
      final InputAction on_key_press, final InputAction on_back, float alpha, 
      Enum<?> background_sprite, float background_alpha) {
        super((title == null) ? "title" : title);

        // Default settings
        m_visibility = Visibility.INVISIBLE;
        m_is_enabled = true;
        setVisible(false);
        setMovable(false);
        setModal(is_modal);

        // Update title
        Label title_label = getTitleLabel();
        if(title == null) {
            title_label.remove();
            padTop(getPadLeft());
            m_has_title = false;
        } else {
            title_label.setAlignment(Align.center);
            m_has_title = true;
        }

        // Background
        m_background_sprite = background_sprite;
        m_background_alpha = background_alpha;
        m_alpha = alpha;
        setColor(1.0f, 1.0f, 1.0f, 0.0f);

        // Default table spacing 
        TableUtils.setSpacingDefaults(this);

        // Position
        m_is_centered = is_centered;
        m_fixed_size = new Vector2();
        m_max_size = new Vector2();
        SetFixedSize(fixed_width, fixed_height);

        // Input handlers
        addListener(new InputListener() {
            //..................................................................
            private boolean ProcessInputAction(InputAction action) {
                // Run touch action 
                switch(action) {
                //..............................................................
                case CONSUME: {
                    return true;
                }

                //..............................................................
                case POP_STATE: {
                    if(GetVisbilityState() == Visibility.VISIBLE) {
                        Main.inst.engine.GetScene().PopState();
                    }
                    return true;
                } 

                //..............................................................
                case IGNORE:
                default: {
                    return false;
                }}
            }

            //..................................................................
            // touchDown
            @Override public boolean touchDown(InputEvent event, float x, float y, 
              int pointer, int button) {
                float w = getWidth(), 
                      h = getHeight();

                // Select touch action and run it
                boolean touch_wnd_area = (x >= 0.0f && y >= 0.0f && x < w && y < h);
                InputAction action = touch_wnd_area ? on_touch_in_area : on_touch_out_area;
                return ProcessInputAction(action);
            }

            //..................................................................
            // keyDown
            @Override public boolean keyDown(InputEvent event, int keycode) {
                switch(keycode) {
                case Keys.BACK:
                case Keys.ESCAPE: {
                    return ProcessInputAction(on_back);
                }
                default: {
                    return ProcessInputAction(on_key_press);
                }}
            }
        });
    }

    //--------------------------------------------------------------------------
    public boolean HasTitle() {
        return m_has_title;
    }

    //--------------------------------------------------------------------------
    public boolean IsEnabled() {
        return m_is_enabled;
    }

    //--------------------------------------------------------------------------
    public void SetSize(boolean make_relative, float fixed_width, float fixed_height, 
      float max_width, float max_height) {
        if(make_relative) {
            float w = Main.inst.gui.GetWidth(true);
            float h = Main.inst.gui.GetHeight(true);
            fixed_width /= w; fixed_height /= h;
            max_width /= w; max_height /= h;
        }
        SetFixedSize(fixed_width, fixed_height);
        SetMaxSize(max_width, max_height);
    }

    //--------------------------------------------------------------------------
    public void SetFixedSize(float width, float height) {
        m_fixed_size.x = width;
        m_fixed_size.y = height;
    }

    //--------------------------------------------------------------------------
    public void SetMaxSize(float width, float height) {
        m_max_size.x = width;
        m_max_size.y = height;
    }

    //--------------------------------------------------------------------------
    public void SetEnabled(boolean is_enabled) {
        m_is_enabled = is_enabled;
    }

    //--------------------------------------------------------------------------
    public Visibility GetVisbilityState() {
        return m_visibility;
    }

    //--------------------------------------------------------------------------
    public void SetTitle(String title, boolean capitalize) {
        if(capitalize) {
            title = title.substring(0, 1).toUpperCase() + 
              title.substring(1).toLowerCase();
        }
        SetTitle(title);
    }

    //--------------------------------------------------------------------------
    public void SetTitle(String title) {
        Label label = getTitleLabel();
        if(label != null) {
            label.setText(title);
        }
    }

    //--------------------------------------------------------------------------
    public EngineWnd Toggle() {
        return (isVisible()) ? Close(true) : Rebuild();
    }

    //--------------------------------------------------------------------------
    public void ToggleAlpha() {
        float a = getColor().a;
        setColor(1.0f, 1.0f, 1.0f, (a > 0.0f) ? 0.0f : m_alpha);
    }

    //--------------------------------------------------------------------------
    public EngineWnd Rebuild() {
        return Reset().Finalize();
    }

    //--------------------------------------------------------------------------
    public EngineWnd Close(boolean do_fade) {
        if(!do_fade) {
            m_visibility = Visibility.INVISIBLE;
            clearActions();
            Main.inst.gui.UnregisterClient(this);
            m_visibility = Visibility.INVISIBLE;
            setVisible(false);
            return this;
        }

        // Check if we should do fade-out
        if(m_visibility == Visibility.FADING_OUT || m_visibility == Visibility.INVISIBLE) {
            return this;
        }

        // Prepare for fade-out
        m_visibility = Visibility.FADING_OUT;
//        setTouchable(Touchable.disabled);
        clearActions();

        // Start fade-out
        final Actor wnd = this; 
        addAction(Actions.sequence(
          Actions.alpha(0.0f, Main.inst.cfg.gui_fade_duration), new Action() {
            @Override public boolean act(float delta) {
                Main.inst.gui.UnregisterClient(wnd);
                m_visibility = Visibility.INVISIBLE;
//                setTouchable(Touchable.disabled);
                wnd.setVisible(false);
                return true;
            }
        }));
        return this;
    }

    //--------------------------------------------------------------------------
    public EngineWnd Open() {
        // Check if we should do fade-in
        if(!m_is_enabled || m_visibility == Visibility.FADING_IN || 
          m_visibility == Visibility.VISIBLE) {
            return this;
        }

        // Prepare for fade-in
        m_visibility = Visibility.FADING_IN;
//        setTouchable(Touchable.disabled);
        clearActions();
        setVisible(true);
        Main.inst.gui.RegisterClient(this);

        // Start fade-in
        addAction(Actions.sequence(
          Actions.alpha(m_alpha, Main.inst.cfg.gui_fade_duration), new Action() {
            @Override public boolean act(float delta) {
                m_visibility = Visibility.VISIBLE;
//                setTouchable(Touchable.enabled);
                return true;
            }
        }));
        return this;
    }

    //--------------------------------------------------------------------------
    public EngineWnd Reset() {
        Skin skin = Main.inst.gui.GetSkin();
        if(getSkin() != Main.inst.gui.GetSkin()) {
            Logger.d("Updating window skin :: name=%s", getClass().getSimpleName());
            setSkin(skin);
            TableUtils.setSpacingDefaults(this);
            setStyle(skin.get("default", WindowStyle.class));
        }

        // Remove old children
        clearChildren();

        // Hide background
        if(m_background_sprite == MapEnum.ItemType.HIDDEN) {
            setBackground((Drawable)null);

        // Set background texture
        } else {
            RendererTexture texture = GetBackgroundTexture();
            if(texture != null) {
                setBackground(
                  new TiledDrawable(new TextureRegion(texture.tx)));
            }
        }

        // 1st update 
        return OnPostReset();
    }

    //--------------------------------------------------------------------------
    public EngineWnd OnPostReset() {
        return this;
    }

    //--------------------------------------------------------------------------
    public EngineWnd OnPreFinalize() {
        return this;
    }

    //--------------------------------------------------------------------------
    public EngineWnd Finalize() {
        // 2nd update
        OnPreFinalize();

        // Pack children
        pack();

        // Check if window size exceeds maximum 
        Float update_width = ValidateMaxSize(
          getWidth(), Main.inst.gui.GetWidth(true), m_max_size.x);
        Float update_height = ValidateMaxSize(
          getHeight(), Main.inst.gui.GetHeight(true), m_max_size.y);

        // Update size of the window
        if(update_width != null || update_height != null) {
            setSize(
               update_width == null ? getWidth() : Math.round(update_width),
               update_height == null ? getHeight() : Math.round(update_height));
            validate();
        }

        // Position
        if(m_is_centered) {
            centerWindow();
        }

        // Open window
        return Open();
    }

    //--------------------------------------------------------------------------
    private Float ValidateMaxSize(float cur_size, float screen_size, float max_size_rel) {
        float max_size = screen_size * max_size_rel;
        return (max_size_rel > 0 && cur_size > max_size) ? max_size : null;
    }

    //--------------------------------------------------------------------------
    public RendererTexture GetBackgroundTexture() {
        if(m_background_sprite == null) {
            return null;
        }

        //
        // Get existing texture from cache
        //
        String tx_cache_key = "wnd-" + toString();
        Renderer.TxCache tx_cache = Main.inst.renderer.GetTxCache();
        RendererTexture texture = tx_cache.Get(tx_cache_key);
        if(texture != null) {
            return texture;
        }

        //
        // Generate new texture
        //

        // Get descriptor of the sprite
        DescSprite sprite = Main.inst.renderable_man.GetSprite(m_background_sprite);
        DescRect rect = sprite.GetFirstAnimRect("idle");

        // Pixmap of texture atlas
        Pixmap pm_orig = new Pixmap(
          PlatformUtils.OpenInternalFile(sprite.texture, true));

        // Extract sprite from atlas
        float scale = Main.inst.renderer.GetWidth() / sprite.tile_width / 5.0f;
        Pixmap pm = RendererTexture.CutPixmap(
          pm_orig, rect.x, rect.y, rect.width, rect.height, scale, scale);
        RendererTexture.SetPixmapAlpha(pm, m_background_alpha);

        texture = tx_cache.Put(tx_cache_key, 
          new RendererTexture(sprite.texture, new Texture(pm), true));

        pm_orig.dispose();
        pm.dispose();
        return texture;
    }

    //--------------------------------------------------------------------------
    public float GetIconSize() {
        return GetSquareSize(Main.inst.cfg.gui_icon_size);
    }

    //--------------------------------------------------------------------------
    public float GetSquareSize(float size) {
        return GetSquareSize(size, size);
    }

    //--------------------------------------------------------------------------
    public float GetSquareSize(float static_size, float max_size) {
        Gui g = Main.inst.gui;
        float size = g.GetSmallest(false, static_size) * g.GetIconScale();
        float max = g.GetSmallest(true, max_size);
        return (size > max) ? max : size;
    }

    //--------------------------------------------------------------------------
    public float GetButtonSize() {
        int font_scale = Main.inst.cfg.gui_font_scale;
        float btn_size =  Main.inst.gui.GetSmallest(
          false, Main.inst.cfg.gui_btn_rel_height * 
            ((font_scale == 1) ? 1.5f : font_scale * 1.0f));
        return btn_size;
    }

    //--------------------------------------------------------------------------
    public Cell<VisTable> AddCellButton(TextureRegion tx, String label, int align, 
      Float width, Float height, ClickListener listener) {
        // Table
        Cell<VisTable> btn_table_cell = add(new VisTable()).align(align);
        VisTable btn_table = btn_table_cell.getActor();

        // Button
        VisImageButton btn = new VisImageButton(new TextureRegionDrawable(tx));
        btn.align(Align.bottomLeft);
        btn.getImage().setFillParent(true);
        btn.getImage().setScaling(Scaling.stretch);

        // Listener
        if(listener != null) {
            btn.addListener(listener);
        }

        // Button cell
        Cell<VisImageButton> btn_cell = btn_table.add(btn);
        EngineWnd.SetCellSize(btn_cell, width, height);

        // Label is located below the button
        if(label != null) {
            btn_table.row();
            btn_table.add(new VisLabel(label));
        }
        return btn_table_cell;
    }

    //--------------------------------------------------------------------------
    public Cell<GuiTextBox> AddCellTextBox(String text) {
        return add(new GuiTextBox(text));
    }

    //--------------------------------------------------------------------------
    public Cell<? extends Actor> AddCellWidget(Actor widget, Cell<VisTable> dest, 
      Float width, Float height) {
        Cell<Actor> cell = (dest == null) ? add(widget) : dest.getActor().add(widget);
        return EngineWnd.SetCellSize(cell, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<VisTable> AddCellTable(Cell<VisTable> dest, Float width, Float height, 
      boolean do_default_spacing) {
        VisTable tbl = new VisTable();
        if(do_default_spacing) {
            TableUtils.setSpacingDefaults(tbl);
        }
        return (Cell<VisTable>)AddCellWidget(tbl, dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiToggleButton> AddCellToggleButton(Cell<VisTable> dest, 
      String label, boolean init, Float width, Float height, GuiToggleButton.Listener listener) {
        return (Cell<GuiToggleButton>)AddCellWidget(
          new GuiToggleButton(label, init, listener), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<VisTextButton> AddCellButton(Cell<VisTable> dest, String label, 
      Float width, Float height, ClickListener listener) {
        VisTextButton btn = new VisTextButton(label);
        if(listener != null) {
            btn.addListener(listener);
        }
        return (Cell<VisTextButton>)AddCellWidget(btn, dest, width, height);
    }

    //--------------------------------------------------------------------------
    public Cell<VisSelectBox<String>> AddCellSelectBox(Cell<VisTable> dest, Object[] items, 
      Float width, Float height, Integer selection_idx, ChangeListener listener) {
        String item_names[] = new String[items.length];
        for(int i = 0; i < item_names.length; i++) {
            item_names[i] = items[i].toString();
        }
        return AddCellSelectBox(dest, item_names, width, height, selection_idx, listener);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<VisSelectBox<String>> AddCellSelectBox(Cell<VisTable> dest, String[] items, 
      Float width, Float height, Integer selection_idx, ChangeListener listener) {
        VisSelectBox<String> select_box = new VisSelectBox<String>();
        if(items != null) {
            select_box.setItems(items);
            if(selection_idx != null) {
                select_box.setSelectedIndex(selection_idx);
            }
        }
        if(listener != null) {
            select_box.addListener(listener);
        }
        return (Cell<VisSelectBox<String>>)AddCellWidget(select_box, dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiColorPicker> AddCellColorPicker(Cell<VisTable> dest, 
      String label, Color target) {
        Float width = 16.0f, height = 16.0f;
        GuiColorPicker color_picker = new GuiColorPicker(label, width, height, target);
        return (Cell<GuiColorPicker>)AddCellWidget(color_picker, dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiButton> AddCellButtonOverlay(Cell<VisTable> dest, TextureRegion tx, 
      String label, Float width, Float height, Gui.TablePad img_pad, 
      Gui.TablePad text_pad, boolean draw_background, ClickListener listener) {
        return (Cell<GuiButton>)AddCellWidget( 
          new GuiButton(tx, label, width, height, img_pad, text_pad, draw_background, listener), 
          dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<VisImage> AddCellImage(Cell<VisTable> dest, TextureRegion tx_reg, 
      Float width, Float height) {
        return (Cell<VisImage>)AddCellWidget( 
          new VisImage(tx_reg), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiSpinnerEx<Integer>> AddCellSpinnerExInt(Cell<VisTable> dest, 
      String label, int init, int min, int max, int step, int precission, Float width, Float height, 
      GuiSpinnerEx.Listener<Integer> listener) {
        return (Cell<GuiSpinnerEx<Integer>>)AddCellWidget(
          new GuiSpinnerEx<Integer>(label, init, min, max, step, precission, 
            UtilsClass.TemplateMathInt.inst, listener), 
          dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiSpinnerEx<Float>> AddCellSpinnerExFloat(Cell<VisTable> dest, 
      String label, float init, float min, float max, float step, int precission, 
      Float width, Float height, GuiSpinnerEx.Listener<Float> listener) {
        return (Cell<GuiSpinnerEx<Float>>)AddCellWidget(
          new GuiSpinnerEx<Float>(label, init, min, max, step, precission, 
            UtilsClass.TemplateMathFloat.inst, listener), 
          dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<Spinner> AddCellSpinnerVector3(Cell<VisTable> dest, 
      Float width, Float height, String label, Vector3 init, float step,
      final GuiSpinnerVector3.Listener listener) {
        return (Cell<Spinner>)AddCellWidget( 
          new GuiSpinnerVector3(label, init, step, listener), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<Spinner> AddCellSpinnerFloat(Cell<VisTable> dest, 
      Float width, Float height, String label, float init, float step,
      final GuiSpinnerFloat.Listener listener) {
        return (Cell<Spinner>)AddCellWidget( 
          new GuiSpinnerFloat(label, init, step, 1, listener), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiSpinnerInt> AddCellSpinnerInt(Cell<VisTable> dest, 
      Float width, Float height, String label, int init, int step,
      final GuiSpinnerInt.Listener listener) {
        return (Cell<GuiSpinnerInt>)AddCellWidget( 
          new GuiSpinnerInt(label, init, step, listener), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<VisTextField> AddCellTextField(Cell<VisTable> dest, 
      Float width, Float height, String label) {
        return (Cell<VisTextField>)AddCellWidget( 
          new VisTextField(label), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiImage> AddCellImage(Cell<VisTable> dest, Enum<?> sprite, 
      Float width, Float height) {
        return (Cell<GuiImage>)AddCellWidget( 
          new GuiImage(sprite), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiMinimap> AddCellMinimap(Cell<VisTable> dest, Float width, 
      Float height) {
        return (Cell<GuiMinimap>)AddCellWidget( 
          new GuiMinimap(), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiBusyIndicator> AddCellBusyIndicator(Cell<VisTable> dest, 
      TextureRegion tx, Float width, Float height) {
        return (Cell<GuiBusyIndicator>)AddCellWidget( 
          new GuiBusyIndicator(tx), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiProgressBar> AddCellProgressBar(Cell<VisTable> dest, Float width,
      Float height, Color col_left, Color col_right) {
        return (Cell<GuiProgressBar>)AddCellWidget( 
          new GuiProgressBar(col_left, col_right, width), dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<VisLabel> AddCellLabel(Cell<VisTable> dest, String text, Float width, 
      Float height) {
        return (Cell<VisLabel>)AddCellWidget( 
          (text == null) ? new VisLabel() : new VisLabel(text), 
          dest, width, height);
    }

    //--------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public Cell<GuiLog> AddCellLog(Cell<VisTable> dest, int line_num, Float width, 
      Float height) {
        GuiLog log = new GuiLog(line_num);
        return (Cell<GuiLog>)AddCellWidget(log, dest, width, height);
    }

    //**************************************************************************
    // WidgetGroup
    //**************************************************************************
    @Override public float getPrefWidth() {
        return (m_fixed_size.x > 0.0f) ? 
          Main.inst.renderer.GetWidth() * m_fixed_size.x : super.getPrefWidth();
    }

    //--------------------------------------------------------------------------
    @Override public float getPrefHeight() {
        return (m_fixed_size.y > 0.0f) ? 
          Main.inst.renderer.GetHeight() * m_fixed_size.y : super.getPrefHeight();
    }
}
