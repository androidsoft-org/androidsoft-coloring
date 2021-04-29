package eu.quelltext.images;

import java.util.LinkedList;
import java.util.Queue;

/* Fill areas with a color but do not cross the border color.
 * This algorithm fills bordered areas regardless of the color in the area.
 */
public class FloodFill {

    private final int[] pixels;
    private final int width;
    private final int height;
    private final int borderColor;
    private final Queue<Pixel> queue = new LinkedList<>();

    public FloodFill(int[] pixels, int width, int height, int borderColor) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.borderColor = borderColor;
    }

    public void fillAt(int x, int y, int color) {
        fillPixel(x, y, color);
        while (!queue.isEmpty()) {
            Pixel p = queue.remove();
            fillPixel(p.x + 1, p.y, color);
            fillPixel(p.x - 1, p.y, color);
            fillPixel(p.x, p.y + 1, color);
            fillPixel(p.x, p.y - 1, color);
        }
    }

    private void fillPixel(int x, int y, int color) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        int pixelColor = pixels[x + y * width];
        if (pixelColor == color || pixelColor == borderColor) {
            return;
        }
        queue.add(new Pixel(x, y));
        pixels[x + y * width] = color;
    }

    private static class Pixel {
        public Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x;
        public int y;
    }

    public int[] getPixels() {
        return pixels;
    }
}
