//------------------------------------------------------------------------------
package android.content.res;

//------------------------------------------------------------------------------
import java.io.IOException;
import java.io.InputStream;

//------------------------------------------------------------------------------
public class AssetManager {
    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static AssetManager stub = new AssetManager();

    //**************************************************************************
    // AssetManager
    //**************************************************************************
    public AssetFileDescriptor openFd(String assetName) 
      throws IOException {
        return new AssetFileDescriptor(assetName);
    }

    //--------------------------------------------------------------------------
    public final InputStream open(String src) 
      throws IOException {
        return null;
    }
}
