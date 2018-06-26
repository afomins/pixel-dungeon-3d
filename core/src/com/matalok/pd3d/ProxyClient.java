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
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import com.matalok.pd3d.proxy.WsClient;
import com.matalok.pd3d.proxy.LocalQueue;
import com.matalok.pd3d.map.Map;
import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.proxy.Interfaces.IProxy;
import com.matalok.pd3d.proxy.Interfaces.IProxyBase;
import com.matalok.pd3d.proxy.Interfaces.IProxyListener;
import com.matalok.pd3d.proxy.LocalClient;
import com.matalok.pd3d.proxy.ProxyBase;
import com.matalok.pd3d.shared.ClientAPI;

//------------------------------------------------------------------------------
public class ProxyClient 
  extends GameNode 
  implements IProxyBase {
    //**************************************************************************
    // ProxyClient
    //**************************************************************************
    private ProxyBase m_base;

    //--------------------------------------------------------------------------
    public ProxyClient() {
        super("proxy-client", 1.0f);

        // Create proxy client base
        m_base = new ProxyBase() {
            //....................................................................
            @Override public IProxy CreateBackend(int log_level, String address, 
              int port) {
                // Remote client via WebSocket
                if(Main.inst.cfg.server_is_remote) {
                    log_level = 1;
                    return new WsClient(log_level, WsClient.CreateUri(address, port));

                // Local client via SharedQueue
                } else {
                    ClientAPI client_api = new ClientAPI() {
                        @Override public Map GetMap(int width, int height) {
                            return Main.inst.level.InitializeTerrain(width, height);
                        }
                    };
                    return new LocalClient(log_level, LocalQueue.client, LocalQueue.server, 
                      client_api);
                }
            }
        };
    }

    // *************************************************************************
    // IProxyBase
    // *************************************************************************
    @Override public void SetListener(IProxyListener listener) {
        m_base.SetListener(listener);
    }

    //------------------------------------------------------------------------------
    @Override public boolean IsConnected() {
        return m_base.IsConnected();
    }

    //------------------------------------------------------------------------------
    @Override public void Start(int log_level, String address, int port) {
        m_base.Start(log_level, address, port);
    }

    //------------------------------------------------------------------------------
    @Override public void Stop() {
        m_base.Stop();
    }

    //------------------------------------------------------------------------------
    @Override public void Send(Msg msg) {
        m_base.Send(msg);
    }

    //------------------------------------------------------------------------------
    @Override public void Process() {
        m_base.Process();
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        Process();
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnMethodCleanup() {
        super.OnMethodCleanup();

        Stop();
        return true;
    }
}
