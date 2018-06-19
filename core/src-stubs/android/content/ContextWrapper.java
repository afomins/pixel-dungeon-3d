package android.content;

import android.content.pm.PackageManager;

public class ContextWrapper 
  extends Context {

    public PackageManager getPackageManager() {
        return PackageManager.stub;
    }
    
    public String getPackageName() {
        return null;
    }
}
