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

//-----------------------------------------------------------------------------
import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.desc.DescSnapshot;
import com.matalok.pd3d.engine.SceneGame;
import com.matalok.pd3d.msg.MsgUpdateScene;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.GsonUtils;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.scenegraph.SgNode;
import com.matalok.scenegraph.SgWriterFile;
import com.matalok.scenegraph.SgWriterLog;
import com.matalok.scenegraph.SgUtils.IWriter;

// -----------------------------------------------------------------------------
public class SnapshotMan 
  extends GameNode {
    // *************************************************************************
    // SnapshotMan
    // *************************************************************************
    private String[] m_files;
    private int m_next_file_idx;
    private UtilsClass.PeriodicTask m_autoswitch;

    //--------------------------------------------------------------------------
    public SnapshotMan() {
        super("snapshot-man", 1.0f);
        UpdateFileList();
    }

    //--------------------------------------------------------------------------
    public void StartAutoswitch() {
        if(m_autoswitch != null || !Main.inst.cfg.snap_autoswitch_enable) {
            return; // Already running
        }

        // Update file list
        UpdateFileList();

        // Start periodic task that will do snapthot switching
        int interval = Utils.SecToMsec(Main.inst.cfg.snap_autoswitch_interval);
        Logger.d("Starting snapshot autoswitch :: interval=%d", interval);
        m_autoswitch = new UtilsClass.PeriodicTask(
          Main.inst.timer.GetCur(), interval, 
          new UtilsClass.Callback() {
              @Override public Object Run(Object... args) {
                  LoadNextSnapshot();
                  return null;
          }});
    }

    //--------------------------------------------------------------------------
    public void StopAutoswitch() {
        if(m_autoswitch == null) {
            return; // Not running
        }
        Logger.d("Stopping snapshot autoswitch");
        m_autoswitch = null;
    }

    //--------------------------------------------------------------------------
    public void UpdateFileList() {
        m_files = Main.inst.cfg.snap_list;
        if(m_files != null) {
            return;
        }

        String path_prefix = PlatformUtils.api.GetInternalAssetsPath();
        FileHandle dir = PlatformUtils.OpenInternalFile(
          path_prefix + Main.inst.cfg.snap_directory, false);

        FileHandle hfiles[] = (dir.exists() && dir.isDirectory()) ? 
          dir.list() : new FileHandle[0];

        m_files = new String[hfiles.length];
        for(int i = 0; i < hfiles.length; i++) {
            m_files[i] = hfiles[i].name();
        }
        m_next_file_idx = 0;
    }

    //--------------------------------------------------------------------------
    public void SaveSnapshot() {
        FileHandle dest = PlatformUtils.OpenLocalFile(
          Main.inst.cfg.snap_directory, false);
        if(dest.exists() && !dest.isDirectory()) {
            dest.delete();
        }

        DescSnapshot snapshot = new DescSnapshot();
        for(GameNode node : new GameNode[] {
          Main.inst.snapshot, Main.inst.level}) {
            SgNode.Walk(node, true, SgNode.SNAPSHOT, 0, snapshot);
        }

        String file_path = String.format("%s/%03d-%s.json", 
          Main.inst.cfg.snap_directory, 
          Main.inst.level.GetDungeonDepth(),
          Main.inst.timer.GetTimestamp());
        for(IWriter writer : new IWriter[] {
          new SgWriterLog(Main.inst.sg_man), 
          new SgWriterFile(PlatformUtils.OpenLocalFile(file_path, true))}) {
            writer.Write(snapshot.ToJsonString());
        }
    }

    //--------------------------------------------------------------------------
    public void LoadSnapshot(String path) {
        // Read JSON
        String json_str = ReadSnapshotJson(path);
        if(json_str == null) {
            Logger.e("Failed to load snapshot, JSON is empty");
            return;
        }

        // Deserialize snapshot
        DescSnapshot snapshot = (DescSnapshot)GsonUtils.Deserialize(json_str, DescSnapshot.class);
        if(snapshot == null) {
            Logger.e("Failed to load snapshot, snapshot is empty");
            return;
        }

        // Validate snapshot
        if(!snapshot.Validate()) {
            Logger.e("Failed to load snapshot, snapshot is invalid");
            return;
        }

        // Simulate "MsgUpdateScene" message
        MsgUpdateScene msg = (MsgUpdateScene)MsgUpdateScene.CreateResponse(
          MsgUpdateScene.CreateRequest());
        msg.game_scene = snapshot.game;
        SceneGame.RecvMsgUpdateScene(null, msg);
    }

    //--------------------------------------------------------------------------
    public void LoadNextSnapshot() {
        if(m_files.length == 0) {
            Logger.e("Failed to load snapshot, no files");
            return;
        }

        if(m_next_file_idx >= m_files.length) {
            m_next_file_idx = 0;
        }
        Logger.d("Selecting next snapshot :: cur_idx=%d max=%d", 
          m_next_file_idx, m_files.length);
        LoadSnapshot(m_files[m_next_file_idx++]);
    }

    //--------------------------------------------------------------------------
    private String ReadSnapshotJson(String file_name) {
        String path = Main.inst.cfg.snap_directory + "/" + file_name;
        Logger.d("Reading snapshot :: path=%s", path);

        // Get file handle
        FileHandle dest = PlatformUtils.OpenInternalFile(path, false);
        if(!dest.exists() || (dest.exists() && dest.isDirectory())) {
            Logger.e("Failed to open snapshot file :: path=%s", path);
            return null;
        }

        // Read JSON
        return dest.readString();
    }

    // *************************************************************************
    // METHODS
    // *************************************************************************
    @Override public boolean OnMethodRender(RenderCtx ctx) {
        super.OnMethodRender(ctx);

        // Do auto-switching 
        if(m_autoswitch != null) {
            m_autoswitch.Run(Main.inst.timer.GetCur());
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public boolean OnMethodSnapshot(DescSnapshot snapshot) {
        snapshot.version = 
          Main.inst.cfg.app_version;
        snapshot.terrain_texture = 
          Main.inst.renderable_man.GetSprite("terrain", 0).texture;
        return false;
    }
}
