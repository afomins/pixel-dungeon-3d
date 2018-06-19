//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.matalok.pd3d.shared.Logger;

//------------------------------------------------------------------------------
public class PlatformUtils {
    //--------------------------------------------------------------------------
    public static PlatformAPI api = null;

    //--------------------------------------------------------------------------
    public static FileHandle OpenInternalFile(String name, boolean try_pd3d) {
        FileHandle h_file = null;
        if(try_pd3d) {
            h_file = Gdx.files.internal("pd3d_" + name);
        }

        if(h_file == null || !h_file.exists()) {
            h_file = Gdx.files.internal(name);
        }
        return h_file;
    }

    //--------------------------------------------------------------------------
    public static FileHandle OpenLocalFile(String name, boolean do_clear) {
        FileHandle h_file = Gdx.files.local(name);
        if(do_clear) {
            h_file.writeString("", false);
        }
        return h_file;
    }

    //--------------------------------------------------------------------------
    public static void LogFile(String name) {
        // Open file
        FileHandle h_file = OpenLocalFile(name, false);
        Logger.i("Logging local file :: name=%s exists=%s", 
          name, h_file.exists() ? "yes" : "no");
        if(!h_file.exists()) {
            return;
        }

        // Line line-by-line
        Logger.i("vvvvvv");
        for(String line : h_file.readString().split("[\\r\\n]+")) {
            Logger.i(">>>" + line);
        }
        Logger.i("^^^^^^");
    }
}
