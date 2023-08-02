package eu.quelltext.images;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ArrayMapperTest {

    @Test
    public void testMapSmallArray() {
        int[] keys = new int[]{22, 66, 44, 33};
        ArrayMapper.Result mappingResult = ArrayMapper.mapTo(new int[]{22, 33, 44, 66, 44, 66, 22}, keys);
        assertThat(mappingResult.getArray(), equalTo(new int[]{0, 3, 2, 1, 2, 1, 0}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(keys));
    }

    @Test
    public void testMapUnkownKeys() {
        int[] keys = new int[]{};
        ArrayMapper.Result mappingResult = ArrayMapper.mapTo(new int[]{22, 33, 44, 1, 44, 1, 22}, keys);
        assertThat(mappingResult.getArray(), equalTo(new int[]{0, 1, 2, 3, 2, 3, 0}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(new int[]{22, 33, 44, 1}));
    }

    @Test
    public void testMapUnkownKeysWithOneArgument() {
        ArrayMapper.Result mappingResult = ArrayMapper.mapTo(new int[]{22, 33, 44, 1, 44, 1, 22});
        assertThat(mappingResult.getArray(), equalTo(new int[]{0, 1, 2, 3, 2, 3, 0}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(new int[]{22, 33, 44, 1}));
    }

    @Test
    public void testMapUnknownKey() {
        int[] keys = new int[]{3, 4};
        ArrayMapper.Result mappingResult = ArrayMapper.mapTo(new int[]{3, 4, 1, 3, 4, 4}, keys);
        assertThat(mappingResult.getArray(), equalTo(new int[]{0, 1, 2, 0, 1, 1}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(new int[]{3, 4, 1}));
    }

    @Test
    public void testMapUnusedKey() {
        int[] keys = new int[]{3, 4};
        ArrayMapper.Result mappingResult = ArrayMapper.mapTo(new int[]{3, 3, 3}, keys);
        assertThat(mappingResult.getArray(), equalTo(new int[]{0, 0, 0}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(new int[]{3, 4}));
    }

    @Test
    public void testUnMapSmallArray() {
        int[] keys = new int[]{22, 66, 44, 33};
        ArrayMapper.Result mappingResult = ArrayMapper.mapFrom(new int[]{0, 3, 2, 1, 2, 1, 0}, keys);
        assertThat(mappingResult.getArray(), equalTo(new int[]{22, 33, 44, 66, 44, 66, 22}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(keys));
    }

    @Test
    public void testUnMapUnknownKey() {
        int[] keys = new int[]{3, 4};
        ArrayMapper.Result mappingResult = ArrayMapper.mapFrom(new int[]{0, 1, 2, 0, 1, 1, 3}, keys);
        assertThat(mappingResult.getArray(), equalTo(new int[]{3, 4, 5, 3, 4, 4, 6}));
        assertThat(mappingResult.getValuesInOrder(), equalTo(new int[]{3, 4, 5, 6}));
    }

    int[] expectedArray = new int[]{0, 1, 2, 2, 1, 3};
    int[] values = new int[]{2, 1, 4, 0, 3};
    int[] array = new int[]{3, 1, 0, 0, 1, 4};

    @Test
    public void testUnMapUnknownKeys() {
        int[] keys = new int[]{};
        ArrayMapper.Result mappingResult = ArrayMapper.mapFrom(array, keys);
        assertThat(mappingResult.getArray(), equalTo(expectedArray));
        assertThat(mappingResult.getValuesInOrder(), equalTo(values));
    }

    @Test
    public void testUnMapWithOneArgument() {
        ArrayMapper.Result mappingResult = ArrayMapper.mapFrom(array);
        assertThat(mappingResult.getArray(), equalTo(expectedArray));
        assertThat(mappingResult.getValuesInOrder(), equalTo(values));
    }
    @Test
    public void testMappingBack() {
        ArrayMapper.Result result2 = ArrayMapper.mapTo(expectedArray, values);
        assertThat(result2.getValuesInOrder(), equalTo(values));
        assertThat(result2.getArray(), equalTo(array));
    }
}
