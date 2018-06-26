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

// -----------------------------------------------------------------------------
package com.matalok.pd3d;

// -----------------------------------------------------------------------------
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Utils;

// -----------------------------------------------------------------------------
public class Timer
  extends GameNode {
    // *************************************************************************
    // STATIC
    // *************************************************************************
    public static long GetMsec() { 
        return Calendar.getInstance().getTimeInMillis(); 
    }

    // *************************************************************************
    // Timer
    // *************************************************************************
    private long m_startup, m_cur, m_delta;
    private float m_cur_sec, m_delta_sec;

    // -------------------------------------------------------------------------
    public Timer() {
        super("timer", 1.0f);
        m_startup = GetMsec();
        UpdateTime();
    }

    // -------------------------------------------------------------------------
    public long GetCur() { return m_cur; }
    public long GetDelta() { return m_delta; }
    public float GetCurSec() { return m_cur_sec; }
    public float GetDeltaSec() { return m_delta_sec; }

    //--------------------------------------------------------------------------
    public String GetTimestamp() {
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmssMs");
        return ft.format(Calendar.getInstance().getTime());
    }

    // -------------------------------------------------------------------------
    private void UpdateTime() {
        long prev = m_cur; 
        m_cur = GetMsec() - m_startup;
        m_cur_sec = Utils.MsecToSec(m_cur);
        m_delta = m_cur - prev;
        m_delta_sec = Utils.MsecToSec(m_delta);
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        UpdateTime();
        return true;
    }
}
