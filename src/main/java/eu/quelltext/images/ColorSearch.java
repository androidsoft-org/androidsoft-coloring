package eu.quelltext.images;

/* Search for a color at a position using a comparator. The closest occurrence is searched for.
 *
 */
public class ColorSearch {
    private final int[] colors;
    private final int width;
    private final int height;
    private boolean success = false;
    private int foundX;
    private int foundY;

    public ColorSearch(int[] colors, int width, int height) {
        this.colors = colors;
        this.width = width;
        this.height = height;
    }

    public void startSearch(int startX, int startY, ColorComparator comparator, int searchRadius) {
        success = false;
        for (int currentRadius = 0; currentRadius <= searchRadius; currentRadius++) {
            for (int delta = -currentRadius; delta <= currentRadius; delta++) {
                if (
                        found(startX + delta, startY + currentRadius, comparator) ||
                        found(startX + delta, startY - currentRadius, comparator) ||
                        found(startX + currentRadius, startY + delta, comparator) ||
                        found(startX - currentRadius, startY + delta, comparator)
                ) {
                    return;
                }
            }
        }
    }

    private boolean found(int x, int y, ColorComparator comparator) {
        if (x < 0 || y < 0 || x >= width || y >= height ) {
            return false;
        }
        if (comparator.equals(colors[x + y * width])) {
            success = true;
            foundX = x;
            foundY = y;
        }
        return success;
    }

    public int getX() {
        return foundX;
    }

    public int getY() {
        return foundY;
    }

    public boolean wasSuccessful() {
        return success;
    }
}
