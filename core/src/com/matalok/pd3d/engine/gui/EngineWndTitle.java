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
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import com.matalok.pd3d.Config;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.engine.Scene;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.MsgSwitchScene;
import com.matalok.pd3d.renderable.RenderableMan;
import com.matalok.pd3d.renderer.Renderer;
import com.matalok.pd3d.shared.Utils;

//------------------------------------------------------------------------------
public class EngineWndTitle 
  extends EngineWnd {
    //**************************************************************************
    // EngineWndTitle
    //**************************************************************************
    public EngineWndTitle() {
        super(null, true, false, 1.0f, 1.0f, 
          InputAction.IGNORE,       // OnTouchInArea
          InputAction.IGNORE,       // OnTouchOutArea
          InputAction.IGNORE,       // OnKeyPress
          InputAction.POP_STATE,    // OnBack
          0.8f, MapEnum.TerrainType.EMPTY, 0.4f);
    }

    //--------------------------------------------------------------------------
    private void AddDashboardButton(final MapEnum.DashboardItemType dashboard_item, 
      final String switch_scene, int align, float width, float height, 
      boolean do_expand, final String info_body) {
        final String name = dashboard_item.toString().toLowerCase();
        Cell<VisTable> cell = AddCellButton(
          Main.inst.renderable_man.GetTextureRegion(dashboard_item, null),
          Utils.CapitalizeFirst(name), align, width, height, 
          new ClickListener(this) {
              @Override public void OnReleased() {
                  // Show info window
                  if(switch_scene == null) {
                      Main.inst.engine.GetScene().PushState(
                        new Scene.StateShowInfo(
                          name, dashboard_item, info_body));

                  // Switch to new scene
                  } else {
                      Main.inst.engine.GetScene().ClearInitDestructor();
                      Main.inst.proxy_client.Send(
                        MsgSwitchScene.CreateRequest(switch_scene));
                  }
            }});
        if(do_expand) {
            cell.expandX();
        }
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        //......................................................................
        // Params
        RenderableMan rm = Main.inst.renderable_man;
        Renderer r = Main.inst.renderer;
        boolean is_portrait = !r.IsLandscape();
        float header_size = r.GetRelHeight(is_portrait ? 0.05f : 0.01f);
        float logo_size = r.GetRelHeight(is_portrait ? 0.3f : 0.5f);
        float btn_size = r.GetRelWidth(is_portrait ? 0.25f : 0.1f);
        float separator_size = r.GetRelWidth(is_portrait ? 0.1f : 0.05f);
        int col_num = is_portrait ? 3 : 7;

        //......................................................................
        // Header
        AddCellLabel(null, null, null, header_size).expandX().colspan(col_num);

        //......................................................................
        // Logo
        row();
        AddCellImage(null,
          rm.GetTextureRegion(MapEnum.BannerType.PIXEL_DUNGEON, null),
          null, logo_size)
        .colspan(col_num).getActor().setScaling(Scaling.fillY);

        //......................................................................
        // Buttons

        // 1st row
        row();
        AddDashboardButton(MapEnum.DashboardItemType.PLAY, "scene-start", 
          Align.right, btn_size, btn_size, true, null);
        AddCellLabel(null, null, separator_size, null);
        AddDashboardButton(MapEnum.DashboardItemType.HIGHSCORES, null, 
          Align.left, btn_size, btn_size, is_portrait, "$TODO");

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
        AddDashboardButton(MapEnum.DashboardItemType.BADGES, null, 
          Align.right, btn_size, btn_size, is_portrait, "$TODO");
        AddCellLabel(null, null, separator_size, null);
        AddDashboardButton(MapEnum.DashboardItemType.ABOUT, null, 
          Align.left, btn_size, btn_size, true, 
          "\nThis is 3D-mod for vanilla \"Pixel Dungeon\"\n\n" + 
          "3D-mod code: Alex Fomins (@matalokgames)\n" +
          "Vanilla \"Pixel Dungeon\" code & graphics: Watabou\n" +
          "Music: Cube_Code\n\n" +
          "Packages:\n" + 
          " * pixel-dungeon-3d: " + Config.version + "\n" +
          " * pixel-dungeon-vanilla: v1.9.1\n" +
          " * libgdx: v" + com.badlogic.gdx.Version.VERSION + "\n" +
          " * visui: v" + VisUI.VERSION);

        //......................................................................
        // Footer
        row();
        AddCellLabel(null, Config.version + " ", null, null)
          .align(Align.bottomRight)
          .expand().colspan(col_num);
        return this;
    }
}
