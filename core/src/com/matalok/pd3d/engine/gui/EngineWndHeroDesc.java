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
import java.util.LinkedHashMap;
import java.util.Map;
import com.matalok.pd3d.Main;
import com.matalok.pd3d.desc.DescHero;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.MsgRunGame;

//------------------------------------------------------------------------------
public class EngineWndHeroDesc 
  extends EngineWndInfo {
    //**************************************************************************
    // EngineWndHeroDesc
    //**************************************************************************
    private DescHero m_hero;

    //--------------------------------------------------------------------------
    public EngineWndHeroDesc() {
        super(false);
    }

    //--------------------------------------------------------------------------
    public EngineWndHeroDesc Init(DescHero hero) {
        m_hero = hero;
        return this;
    }

    //**************************************************************************
    // EngineWnd
    //**************************************************************************
    @Override public EngineWnd OnPostReset() {
        super.OnPostReset();

        final String hero_name = 
          MapEnum.AvatarType.Get(m_hero.id).toString().toLowerCase();

        // Body
        String text = "\n";
        for(String perk : m_hero.perks) {
            if(text.length() > 1) {
                text += "\n\n";
            }
            text += perk;
        }

        // Start new game button
        Map<String, ClickListener> buttons = new LinkedHashMap<String, ClickListener>();
        buttons.put("Start new game", 
          new ClickListener(this) {
              @Override public void OnReleased() {
                  Main.inst.engine.GetScene().ClearInitDestructor();
                  Main.inst.proxy_client.Send(
                    MsgRunGame.CreateRequest(hero_name, "new"));
          }});

        // Continue old game button
        if(m_hero.level > 0) {
            buttons.put("Continue old game", 
              new ClickListener(this) {
                  @Override public void OnReleased() {
                      Main.inst.engine.GetScene().ClearInitDestructor();
                      Main.inst.proxy_client.Send(
                      MsgRunGame.CreateRequest(hero_name, "continue"));
              }});
        }

        // Set window
        Set(hero_name, MapEnum.AvatarType.Get(m_hero.id), text, buttons, -1);
        return this;
    }
}
