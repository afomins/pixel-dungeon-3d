// -----------------------------------------------------------------------------
package com.matalok.pd3d;

//-----------------------------------------------------------------------------
import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.msg.MsgCommand;
import com.matalok.pd3d.node.GameNode;
import com.matalok.pd3d.shared.Logger;

// -----------------------------------------------------------------------------
public class SaveGameMan 
  extends GameNode {
    // *************************************************************************
    // SaveGameMan
    // *************************************************************************
    private String[] m_names;

    //--------------------------------------------------------------------------
    public SaveGameMan() {
        super("save-game-man", 1.0f);
    }

    //--------------------------------------------------------------------------
    public String[] Refresh() {
        // Create savegame directory if missing
        FileHandle dir = PlatformUtils.OpenLocalFile("savegame", false);
        if(!dir.exists()) {
            dir.mkdirs();
            Logger.d(" >> none");
            return new String[0];
        }

        // Read heroes
        FileHandle files[] = dir.list();
        m_names = new String[files.length];
        for(int i = 0; i < files.length; i++) {
            m_names[i] = files[i].name();
        }
        return m_names;
    }

    //--------------------------------------------------------------------------
    public String GetPath(String savegame) {
        return "savegame/" + savegame;
    }

    //--------------------------------------------------------------------------
    public void Load(String savegame) {
        String src_path = GetPath(savegame);
        Logger.d("Loading savegame :: path=%s", src_path);
        FileHandle src = PlatformUtils.OpenLocalFile(src_path, false);
        FileHandle dst = PlatformUtils.OpenLocalFile("", false);
        if(!src.exists() || !src.isDirectory() || !dst.exists() || !dst.isDirectory()) {
            Logger.e("Failed to load savegame :: src=%s:%s dst=%s:%s",
              src.exists(), src.isDirectory(), dst.exists(), dst.isDirectory());
            return;
        }

        // Update savegame files
        for(FileHandle file : src.list()) {
            file.copyTo(dst);
        }

        // Continue game
        MsgCommand msg = MsgCommand.CreateRequest();
        msg.game_op = "load-continue";
        Main.inst.proxy_client.Send(msg);
    }

    //--------------------------------------------------------------------------
    public void SaveStart(String hero, String savegame) {
        MsgCommand msg = MsgCommand.CreateRequest();
        msg.game_op = "save";
        msg.game_args = new String[] { hero, savegame };
        Main.inst.proxy_client.Send(msg);
    }

    //--------------------------------------------------------------------------
    public void Save(String hero, String savegame) {
        String dst_path = GetPath(savegame);
        Logger.d("Creating savegame :: path=%s", dst_path);
        FileHandle src = PlatformUtils.OpenLocalFile("", false);
        FileHandle dst = PlatformUtils.OpenLocalFile(dst_path, false);
        if(!dst.isDirectory()) {
            dst.mkdirs();
        }

        for(FileHandle file : src.list()) {
            if(!file.name().startsWith(hero)) {
                continue;
            }
            file.copyTo(dst);
        }
    }

    //--------------------------------------------------------------------------
    public void Delete(String savegame) {
        String src_path = GetPath(savegame);
        Logger.d("Deleting savegame :: path=%s", src_path);
        FileHandle src = PlatformUtils.OpenLocalFile(src_path, false);
        if(src.exists()) {
            src.deleteDirectory();
        }
    }
}
