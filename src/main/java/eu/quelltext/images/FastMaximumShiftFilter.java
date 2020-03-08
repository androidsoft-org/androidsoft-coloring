package eu.quelltext.images;

/* This counter has a run time complexity O(width * height * radius)
 *
 */
public class FastMaximumShiftFilter implements MaximumShiftFilter {

    private final int[] array;
    private final int width;
    private final int height;

    public FastMaximumShiftFilter(int[] array, int width, int height) {
        this.array = array;
        this.width = width;
        this.height = height;
    }

    @Override
    public int[] compute(int radius) {
        //System.out.println("-------------------------------");
        int[] result = new int[array.length];
        OccurrenceCounter startCounter = new OccurrenceCounter();
        // initialize counter
        for (int y = 0; y < radius && y < height; y++) {
            for (int x = 0; x <= radius && x < width; x++) {
                startCounter.increase(array[x + y * width]);
            }
        }
        for (int y = 0; y < height; y++) {
            //System.out.println("new line " + y);
            int minY = y - radius - 1; // index to remove line, not in range
            int maxY = y + radius;     // index to add line, in range
            if (minY >= 0) {
                for (int x = 0; x <= radius; x++) {
                    startCounter.decrease(array[x + minY * width]);
                }
            }
            if (maxY < height) {
                for (int x = 0; x <= radius; x++) {
                    startCounter.increase(array[x + maxY * width]);
                }
            }
            int yTimesWidth = y * width;
            result[yTimesWidth] = startCounter.max();
            // start of the row is initialized
            OccurrenceCounter counter = startCounter.copy();
            // this defines the range we operate on
            minY = Math.max(minY + 1, 0);          // out of range
            maxY = Math.min(maxY, height - 1); // in range
            for (int x = 1; x < width; x++) {
                //System.out.println("pixel " + x + "," + y);
                int minX = x - radius - 1; // index to remove line
                int maxX = x + radius;     // index to add line
                // remove the line we leave
                if (minX >= 0) {
                    for (int ky = minY; ky <= maxY; ky++) {
                        counter.decrease(array[minX + ky * width]);
                    }
                }
                // add the line that is reachable
                if (maxX < width) {
                    for (int ky = minY; ky <= maxY; ky++) {
                        counter.increase(array[maxX + ky * width]);
                    }
                }
                result[x + yTimesWidth] = counter.max();
            }
        }
        return result;
    }
}

/*
        int width = classifiedPixels.length;
        int height = classifiedPixels[0].length;

        smoothedColors = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        smoothedPixels = new int[width][height];

        int resultColorsIndex = 0;
        int[] resultColors = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] surroundingCls = new int[NUMBER_OF_COLORS];
                int kx_min = Math.max(x - AREA_RADIUS, 0);
                int ky_min = Math.max(y - AREA_RADIUS, 0);
                int kx_max = Math.min(x + AREA_RADIUS + 1, width);
                int ky_max = Math.min(y + AREA_RADIUS + 1, height);

                // use a kernel like this
                // +--+--+
                // |  |  |
                // +--+--+
                // |  |  |
                // +--+--+
                for (int ky = ky_min; ky < ky_max; ky++) {
                    if (ky_min <= ky && ky < ky_max) {
                        for (int kx = kx_min; kx < kx_max; kx += AREA_RADIUS) {
                            int cls = classifiedPixels[kx][ky];
                            surroundingCls[cls]++;
                        }
                    }
                }
                for (int kx = kx_min; kx < kx_max; kx++) {
                    if (kx_min <= kx && kx < kx_max) {
                        for (int ky = ky_min; ky < ky_max; ky += AREA_RADIUS) {
                            int cls = classifiedPixels[kx][ky];
                            surroundingCls[cls]++;
                        }
                    }
                }
                // use a kernel like this
                // \|/
                // -+-
                // /|\
                for (int k = 0; k <= AREA_RADIUS; k++) {
                    int kyMin = Math.max(y - k, ky_min);
                    int kyMax = Math.min(y + k, ky_max - 1);
                    int kxMin = Math.max(x - k, kx_min);
                    int kxMax = Math.min(x + k, kx_max - 1);
                    //surroundingCls[classifiedPixels[kxMin][y]]++;
                    //surroundingCls[classifiedPixels[kxMax][y]]++;
                    //surroundingCls[classifiedPixels[ x   ][kyMin]]++;
                    surroundingCls[classifiedPixels[kxMin][kyMin]]++;
                    surroundingCls[classifiedPixels[kxMax][kyMin]]++;
                    //surroundingCls[classifiedPixels[ x   ][kyMax]]++;
                    surroundingCls[classifiedPixels[kxMin][kyMax]]++;
                    surroundingCls[classifiedPixels[kxMax][kyMax]]++;
                }
                int maxValue = surroundingCls[0];
                int bestClass = 0;
                for (int i = 1; i < NUMBER_OF_COLORS; i++) {
                    if (maxValue < surroundingCls[i]) {
                        maxValue = surroundingCls[i];
                        bestClass = i;
                    }
                }
                smoothedPixels[x][y] = bestClass;
                int color = centroidColors[bestClass];
                resultColors[resultColorsIndex] = color;
                resultColorsIndex++;
            }
        }
        progress.stepShowSmoothedImage();
        smoothedColors.setPixels(resultColors, 0, width, 0, 0, width, height);

 */
