package android.content.pm;

public class PackageManager {
    public static PackageManager stub = new PackageManager();

    @SuppressWarnings("serial")
    public class NameNotFoundException 
      extends Exception {
    }

    public PackageInfo getPackageInfo(String packageName, int i) 
      throws NameNotFoundException{
        // TODO Auto-generated method stub
        return PackageInfo.stub;
    }
}
