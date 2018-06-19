//------------------------------------------------------------------------------
package android.content.res;

//------------------------------------------------------------------------------
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

//------------------------------------------------------------------------------
public class AssetFileDescriptor {
    //**************************************************************************
    // AssetFileDescriptor
    //**************************************************************************
    private String m_name;
    private FileHandle m_handle;

    //--------------------------------------------------------------------------
    public AssetFileDescriptor(String name) {
        m_name = name;
    }

    //--------------------------------------------------------------------------
    public Object getFileDescriptor() {
        if(m_handle == null) {
            m_handle = Gdx.files.internal(m_name);
        }
        return m_handle;
    }

    //--------------------------------------------------------------------------
    public Object getStartOffset() {
        return null;
    }

    //--------------------------------------------------------------------------
    public Object getLength() {
        return null;
    }

    //--------------------------------------------------------------------------
    public void close() {
        m_handle = null;
    }
}
