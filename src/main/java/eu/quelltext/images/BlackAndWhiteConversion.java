package eu.quelltext.images;

public class BlackAndWhiteConversion {

    private final int colorBright;
    private final int colorDark;
    private static final int BINARY_COLOR_THRESHOLD = 3 * 0xff / 2;


    public BlackAndWhiteConversion(int colorBright, int colorDark) {
        this.colorBright = colorBright;
        this.colorDark = colorDark;
    }

    public void toBlackAndWhite(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int brightness = (pixel & 0xff) + ((pixel >> 8) & 0xff) + ((pixel >> 16) & 0xff) +
                    (3 * (0xff - (0xff & (pixel >> 24)))); // transparency, see https://github.com/niccokunzmann/coloring-book/issues/86
            pixels[i] = brightness > BINARY_COLOR_THRESHOLD ? colorBright : colorDark;
        }
    }
}
