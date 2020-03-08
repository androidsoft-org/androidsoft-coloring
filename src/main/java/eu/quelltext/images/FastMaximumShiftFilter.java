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
        System.out.println("-------------------------------");
        int[] result = new int[array.length];
        OccurrenceCounter startCounter = new OccurrenceCounter();
        // initialize counter
        for (int y = 0; y < radius && y < height; y++) {
            for (int x = 0; x <= radius && x < width; x++) {
                startCounter.increase(array[x + y * width]);
            }
        }
        for (int y = 0; y < height; y++) {
            System.out.println("new line " + y);
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
                System.out.println("pixel " + x + "," + y);
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
