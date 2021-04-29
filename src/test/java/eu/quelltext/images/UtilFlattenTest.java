package eu.quelltext.images;

import org.junit.Test;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class UtilFlattenTest {
    private int[][] a1 = new int[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
    private int[] a2 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};

    @Test
    public void testArrayIsFlat() {
        int[] result = Util.flatten(a1);
        assertThat(a2, equalTo(result));
    }

    @Test
    public void testArrayUnflattened() {
        int[][] result = Util.unflatten(a2, a1.length);
        assertThat(a1, equalTo(result));
    }

}
