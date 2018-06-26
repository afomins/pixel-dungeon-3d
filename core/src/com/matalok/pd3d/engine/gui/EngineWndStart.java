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
import java.util.LinkedList;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescHero;
import com.matalok.pd3d.engine.SceneStart;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.renderable.RenderableMan;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class EngineWndStart 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndStart
    //**************************************************************************
    private DescHero m_heroes[];

    //--------------------------------------------------------------------------
    public EngineWndStart() {
        super(null, true, false, 1.0f, 1.0f, 
          InputAction.IGNORE,       // OnTouchInArea
          InputAction.IGNORE,       // OnTouchOutArea
          InputAction.IGNORE,       // OnKeyPress
          InputAction.POP_STATE,    // OnBack
          0.8f, MapEnum.TerrainType.EMPTY, 0.4f);
    }

    //--------------------------------------------------------------------------
    public EngineWndStart Init(LinkedList<DescHero> heroes) {
        Utils.Assert(heroes.size() == 4, 
          "Failed to init heroes window, wrong hero count :: cnt=%d", heroes.size());
        m_heroes = heroes.toArray(new DescHero[]{});
        return this;
    }

    //--------------------------------------------------------------------------
    private void AddHeroButton(MapEnum.AvatarType avatar_type, final DescHero hero, 
      int align, float width, float height, boolean do_expand) {
        final String name = avatar_type.toString().toLowerCase();

        Cell<VisTable> cell = AddCellButton(
          Main.inst.renderable_man.GetTextureRegion(avatar_type, null),
          Utils.CapitalizeFirst(name), align, width, height, 
          new ClickListener(this) {
              @Override public void OnReleased() {
                  // Show hero info
                  Main.inst.engine.GetScene().PushState(
                    new SceneStart.StateShowHeroInfo(hero));
          }});
        if(do_expand) {
            cell.expandX();
        }
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        //..................................................................
        // Param
        RenderableMan rm = Main.inst.renderable_man;
        Renderer r = Main.inst.renderer;
        boolean is_portrait = !r.IsLandscape();
        float header_size = r.GetRelHeight(0.14f);
        float logo_height = r.GetRelHeight(is_portrait ? 0.2f : 0.25f);
        float logo_width = r.GetRelWidth(is_portrait ? 1.0f : 0.0f);
        float btn_size = r.GetRelWidth(is_portrait ? 0.25f : 0.1f);
        float separator_size = r.GetRelWidth(is_portrait ? 0.1f : 0.05f);
        int col_num = is_portrait ? 3 : 7;

        //...................................................................]
        // Header
        AddCellLabel(null, null, null, header_size).colspan(col_num);

        //..................................................................
        // Logo
        row();
        AddCellImage(null,
          rm.GetTextureRegion(MapEnum.BannerType.SELECT_HERO, null),
          logo_width, logo_height)
        .colspan(col_num).getActor()
        .setScaling(is_portrait ? Scaling.fillX : Scaling.fillY);

        // Horizontal separator
        if(!is_portrait) {
            row();
            AddCellLabel(null, null, null, separator_size).colspan(col_num);
        }

        //..................................................................
        // Buttons

        // 1st row
        row();
        AddHeroButton(MapEnum.AvatarType.Get(m_heroes[0].id), m_heroes[0], Align.right, 
          btn_size, btn_size, true);
        AddCellLabel(null, null, separator_size, null);
        AddHeroButton(MapEnum.AvatarType.Get(m_heroes[1].id), m_heroes[1], Align.left, 
          btn_size, btn_size, is_portrait);

        // Horizontal separator
        if(is_portrait) {
            row();
            AddCellLabel(null, null, null, separator_size / 2.0f).colspan(col_num);
        }

        // 2nd row
        if(is_portrait) {
            row();
        } else {
            AddCellLabel(null, null, separator_size, null);
        }
        AddHeroButton(MapEnum.AvatarType.Get(m_heroes[2].id), m_heroes[2], Align.right, 
          btn_size, btn_size, is_portrait);
        AddCellLabel(null, null, separator_size, null);
        AddHeroButton(MapEnum.AvatarType.Get(m_heroes[3].id), m_heroes[3], Align.left, 
          btn_size, btn_size, true);

        //..................................................................
        // Footer
        row();
        AddCellLabel(null, null, null, null).expand().colspan(col_num);
        return this;
    }
}
