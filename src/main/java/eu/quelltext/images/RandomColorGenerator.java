package eu.quelltext.images;

import java.util.Random;

/* This class generates the same sequence of colors.
 *
 */
public class RandomColorGenerator {

    private final Random random;

    public RandomColorGenerator() {
        random = new Random(1828379928l);
    }

    public int bright() {
        int randomColor = random.nextInt(0xffffff);
        int result = 0xff000000 | (0xff << random.nextInt(3)) | randomColor;
        return result;
    }

    public int[] bright(int length) {
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = bright();
        }
        return result;
    }
}
