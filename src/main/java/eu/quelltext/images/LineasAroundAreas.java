package eu.quelltext.images;

import android.graphics.Bitmap;

public class LineasAroundAreas {
    private final int[] areas;
    private final int width;
    private final int height;

    public LineasAroundAreas(int[] areas, int width, int height) {
        this.areas = areas;
        this.width = width;
        this.height = height;
    }

    public int[] draw(int lineWidth, int backgroundColor, int lineColor) {
        final int lineWidthHalf = lineWidth / 2 + lineWidth % 2;
        final int lineWidthHalfSquared = lineWidthHalf * lineWidthHalf;
        int[] result = new int[areas.length];
        int xMax = width - 1;
        int yMax = height - 1;
        for (int x = 0; x < xMax; x++) {
            for (int y = 0; y < yMax; y++) {
                int i = x + y * width;
                int cls = areas[i];
                if (cls != areas[x+1 + y * width] || cls != areas[x + (y+1) * width]) {
                    // we are at a border - draw a circle!
                    for (int dx = -lineWidthHalf; dx <= lineWidthHalf; dx++) {
                        for (int dy = -lineWidthHalf; dy <= lineWidthHalf; dy++) {
                            // check that we paint a dot
                            if (dx*dx + dy*dy > lineWidthHalfSquared) {
                                continue;
                            }
                            // check bounds
                            int kx = x + dx;
                            if (kx < 0 || kx >= width) {
                                continue;
                            }
                            int ky = y + dy;
                            if (ky < 0 || ky >= height) {
                                continue;
                            }
                            result[kx + ky * width] = lineColor;
                        }
                    }
                } else if (result[i] != lineColor) {
                    result[i] = backgroundColor;
                }
            }
        }
        return result;
    }
}
