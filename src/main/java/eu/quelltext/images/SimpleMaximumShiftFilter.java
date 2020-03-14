package eu.quelltext.images;

/* This class moves through the array and assigns each field the class
 * which occurs maximum in all the fields covered with a radius.
 */
class SimpleMaximumShiftFilter implements MaximumShiftFilter {
    private final int[] array;
    private final int width;
    private final int height;

    public SimpleMaximumShiftFilter(int[] array, int width, int height) {
        this.array = array;
        this.width = width;
        this.height = height;
    }

    public int[] compute(int radius) {
        int[] result = new int[array.length];
        for (int y = 0; y < height; y++) {
            int minY = Math.max(0, y - radius);
            int maxY = Math.min(height - 1, y + radius);
            for (int x = 0; x < width; x++) {
                OccurrenceCounter counter = new OccurrenceCounter();
                int minX = Math.max(0, x - radius);
                int maxX = Math.min(width - 1, x + radius);
                for (int kx = minX ; kx <= maxX; kx++) {
                    for (int ky = minY ; ky <= maxY; ky++) {
                        int label = array[kx + ky * width];
                        counter.increase(label);
                    }
                }
                result[x + y * width] = counter.max();
            }
        }
        return result;
    }
}
