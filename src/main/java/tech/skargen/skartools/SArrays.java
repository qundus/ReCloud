package tech.skargen.skartools;

/**
 * Useful array handling methods by github.com/cypherskar
 */
public class SArrays {
  /**
   * Clone array.
   * @param source Array to be copied.
   * @return
   */
  public static int[][] clone(int[][] source) {
    int[][] dest = new int[source.length][source[0].length];

    for (int i = 0; i < source.length; i++) {
      for (int j = 0; j < source[i].length; j++) {
        dest[i][j] = source[i][j];
      }
    }

    return dest;
  }

  /**
   * Clone array.
   *
   * @param source Array to be copied.
   * @return
   */
  public static int[] clone(int[] source) {
    int[] dest = new int[source.length];

    for (int i = 0; i < source.length; i++) {
      dest[i] = source[i];
    }

    return dest;
  }

  /**
   * Clone array.
   *
   * @param source Array to be copied.
   * @return
   */
  public static int[][] clone(double[][] source) {
    int[][] dest = new int[source.length][source[0].length];

    for (int i = 0; i < source.length; i++) {
      for (int j = 0; j < source[i].length; j++) {
        dest[i][j] = (int) source[i][j];
      }
    }

    return dest;
  }

  /**
   * Clone array.
   *
   * @param source Array to be copied.
   * @return
   */
  public static double[] clone(double[] source) {
    double[] dest = new double[source.length];

    for (int i = 0; i < source.length; i++) {
      dest[i] = source[i];
    }

    return dest;
  }

  /**
   * Expand array and insert additional values in it.
   * @param source Source array to expand.
   * @param index Index to insert values at.
   * @param values Values to insert in source array.
   * @return
   */
  public static int[] insert(int[] source, int index, int... values) {
    if (source == null && values == null) {
      return null;
    } else if (source == null && values != null) {
      return values;
    } else if (source != null && values == null) {
      return source;
    }

    int[] result = new int[source.length + values.length];
    int sourceIndex = 0;
    int valuesIndex = 0;
    index = (index <= -1) ? 0 : index;
    index = (index >= source.length) ? source.length : index;
    while (sourceIndex + valuesIndex < result.length) {
      if (sourceIndex == index) {
        result[sourceIndex + valuesIndex] = values[valuesIndex];
        valuesIndex++;

        if (valuesIndex == values.length) {
          index = -1;
        }
      } else {
        result[sourceIndex + valuesIndex] = source[sourceIndex];
        sourceIndex++;
      }
    }
    return result;
  }

  /**
   * Expand array and insert additional values in it.
   *
   * @param source Source array to expand.
   * @param index  Index to insert values at.
   * @param values Values to insert in source array.
   * @return
   */
  public static double[] insert(double[] source, int index, double... values) {
    if (source == null && values == null) {
      return null;
    } else if (source == null && values != null) {
      return values;
    } else if (source != null && values == null) {
      return source;
    }

    double[] result = new double[source.length + values.length];
    int sourceIndex = 0;
    int valuesIndex = 0;
    index = (index <= -1) ? 0 : index;
    index = (index >= source.length) ? source.length : index;
    while (sourceIndex + valuesIndex < result.length) {
      if (sourceIndex == index) {
        result[sourceIndex + valuesIndex] = values[valuesIndex];
        valuesIndex++;

        if (valuesIndex == values.length) {
          index = -1;
        }
      } else {
        result[sourceIndex + valuesIndex] = source[sourceIndex];
        sourceIndex++;
      }
    }
    return result;
  }

  /**
   * Expand array and insert additional values in it.
   *
   * @param source Source array to expand.
   * @param index  Index to insert values at.
   * @param values Values to insert in source array.
   * @return
   */
  public static long[] insert(long[] source, int index, long... values) {
    if (source == null && values == null) {
      return null;
    } else if (source == null && values != null) {
      return values;
    } else if (source != null && values == null) {
      return source;
    }

    long[] result = new long[source.length + values.length];
    int sourceIndex = 0;
    int valuesIndex = 0;
    index = (index <= -1) ? 0 : index;
    index = (index >= source.length) ? source.length : index;
    while (sourceIndex + valuesIndex < result.length) {
      if (sourceIndex == index) {
        result[sourceIndex + valuesIndex] = values[valuesIndex];
        valuesIndex++;

        if (valuesIndex == values.length) {
          index = -1;
        }
      } else {
        result[sourceIndex + valuesIndex] = source[sourceIndex];
        sourceIndex++;
      }
    }
    return result;
  }
}