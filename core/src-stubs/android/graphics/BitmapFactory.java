package android.graphics;

import android.content.res.Resources;

public class BitmapFactory {

    public static class Options {

        public boolean inScaled;
        public boolean inDither;
        public String inPreferredConfig;

    }

    public static Bitmap decodeResource(Resources resources, Integer src, Options bitmapOptions) {
        return Bitmap.stub;
    }

    public static Bitmap decodeStream(Object open, Object object, Options bitmapOptions) {
        return Bitmap.stub;
    }

    public static Bitmap decodeResource(Resources resources, int resID) {
        return Bitmap.stub;
    }

}
