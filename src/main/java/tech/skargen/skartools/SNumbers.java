package tech.skargen.skartools;

import java.util.PrimitiveIterator.OfInt;
import java.util.Random;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helps populating random numbers by github.com/cypherskar
 */
public final class SNumbers {
  private static Logger _LOG;
  static {
    _LOG = LogManager.getLogger();
  }

  /**
   * The random number generation pattern style.
   */
  public enum RandomStyle {
    /**
     * To generate random numbers with fixed pace between min&max.
     * </p>
     * <li>example: min=100, max=500, arraySize=4 then:
     * <li>(i = 0) => min => 100
     * <li>(i = 1) => ((500-100)/4)+(i-1) => 200
     * <li>(i = 2) => ((500-100)/4)+(i-1) => 300
     * <li>(i = 3) => ((500-100)/4)+(i-1) => 400
     */
    Fixed_Pace,

    /** To generate random numbers arbitrarily between min&max. */
    Arbitrary
  }

  /**
   * Generates random numbers according to array size and random style between min
   * and max, or create an array of random numbers provided.
   *
   * @param arraySize Random numbers pool size.
   * @param randStyle Random style based on the given options, works only with
   *                  values array of 2 elements.
   * @param values    Values to generate random numbers with. For example:
   *                  <ul>
   *                  <li>Passing {22} returns an array of 1 element only that is
   *                  22.
   *                  <li>Passing {22, 44} returns an array of arraySize elements
   *                  between 22 & 44.
   *                  <li>Passing {22, 44, 55} returns an array of arraySize elements
   *                  that has mixing numbers of 22 & 44 & 55.
   *                  </ul>
   * @return an array containing random numbers based on the given options and
   *         parameters.
   */
  public int[] create(int arraySize, RandomStyle randStyle, int... values) {
    if (values == null || values.length <= 0) {
      _LOG.error("can't create random array because values array is empty");
      return null;
    }

    int[] result = new int[arraySize];
    switch (values.length) {
      // no numbers to randomize (no range), just return an array of one element.
      case 1:

        result = null;
        break;

      case 2:

        final int difference = Math.abs(values[1] - values[0]);
        if (difference == 0) {
          _LOG.error("can't create random array because the first 2 values are identical");
          return null;
        }

        // sorting incase 2 numbers only.
        if (values[0] > values[1]) {
          int temp = values[0];
          values[0] = values[1];
          values[1] = temp;
        }

        final int min = values[0];
        if (arraySize <= 1) {
          arraySize = 1;
          randStyle = RandomStyle.Arbitrary;
        }

        double value = 0; // difference / arrSize;
        double diff = 0;
        for (int i = 0; i < arraySize; i++) {
          switch (randStyle) {
            case Fixed_Pace:
              if (i > 0) {
                value = (1.0 / arraySize) * (i + 1);
              }
              break;

            case Arbitrary:
              value = Math.random();
              diff = 1 - value;
              break;

            default:
              _LOG.error("random style enum not implemented");
              break;
          }
          result[i] = (int) (min + (value * (difference + diff)));
        }
        break;

      default:

        OfInt randIdx = new Random().ints(arraySize, 0, values.length).iterator();
        for (int i = 0; i < arraySize && randIdx.hasNext(); i++) {
          result[i] = values[randIdx.nextInt()];
        }
        break;
    }

    return result;
  }

  /**
   * Generates random numbers according to array size and random style between min
   * and max, or create an array of random numbers provided.
   *
   * @param arraySize Random numbers pool size.
   * @param randStyle Random style based on the given options, works only with
   *                  values array of 2 elements.
   * @param values    Values to generate random numbers with. For example:
   *                  <ul>
   *                  <li>Passing {22} returns an array of 1 element only that is
   *                  22.
   *                  <li>Passing {22, 44} returns an array of arraySize elements
   *                  between 22 & 44.
   *                  <li>Passing {22, 44, 55} returns an array of arraySize
   *                  elements that has mixing numbers of 22 & 44 & 55.
   *                  </ul>
   * @return an array containing random numbers based on the given options and
   *         parameters.
   */
  public long[] create(int arraySize, RandomStyle randStyle, long... values) {
    if (values == null || values.length <= 0) {
      _LOG.error("can't create random array because values array is empty");
      return null;
    }

    long[] result = new long[arraySize];
    switch (values.length) {
      // no numbers to randomize (no range), just return an array of one element.
      case 1:

        result = null;
        break;

      case 2:

        final long difference = Math.abs(values[1] - values[0]);
        if (difference == 0) {
          _LOG.error("can't create random array because the first 2 values are identical");
          return null;
        }

        // sorting incase 2 numbers only.
        if (values[0] > values[1]) {
          long temp = values[0];
          values[0] = values[1];
          values[1] = temp;
        }

        final long min = values[0];
        if (arraySize <= 1) {
          arraySize = 1;
          randStyle = RandomStyle.Arbitrary;
        }

        double value = 0; // difference / arrSize;
        double diff = 0;
        for (int i = 0; i < arraySize; i++) {
          switch (randStyle) {
            case Fixed_Pace:
              if (i > 0) {
                value = (1.0 / arraySize) * (i + 1);
              }
              break;

            case Arbitrary:
              value = Math.random();
              diff = 1 - value;
              break;

            default:
              _LOG.error("random style enum not implemented");
              break;
          }
          result[i] = (long) (min + (value * (difference + diff)));
        }
        break;

      default:

        OfInt randIdx = new Random().ints(arraySize, 0, values.length).iterator();
        for (int i = 0; i < arraySize && randIdx.hasNext(); i++) {
          result[i] = values[randIdx.nextInt()];
        }
        break;
    }

    return result;
  }

  /**
   * Generates random numbers according to array size and random style between min
   * and max, or create an array of random numbers provided.
   *
   * @param arraySize Random numbers pool size.
   * @param randStyle Random style based on the given options, works only with
   *                  values array of 2 elements.
   * @param values    Values to generate random numbers with. For example:
   *                  <ul>
   *                  <li>Passing {22} returns an array of 1 element only that is
   *                  22.
   *                  <li>Passing {22, 44} returns an array of arraySize elements
   *                  between 22 & 44.
   *                  <li>Passing {22, 44, 55} returns an array of arraySize
   *                  elements that has mixing numbers of 22 & 44 & 55.
   *                  </ul>
   * @return an array containing random numbers based on the given options and
   *         parameters.
   */
  public double[] create(int arraySize, RandomStyle randStyle, double... values) {
    if (values == null || values.length <= 0) {
      _LOG.error("can't create random array because values array is empty");
      return null;
    }

    double[] result = new double[arraySize];
    switch (values.length) {
      // no numbers to randomize (no range), just return an array of one element.
      case 1:

        result = null;
        break;

      case 2:

        final double difference = Math.abs(values[1] - values[0]);
        if (difference == 0) {
          _LOG.error("can't create random array because the first 2 values are identical");
          return null;
        }

        // sorting incase 2 numbers only.
        if (values[0] > values[1]) {
          double temp = values[0];
          values[0] = values[1];
          values[1] = temp;
        }

        final double min = values[0];
        if (arraySize <= 1) {
          arraySize = 1;
          randStyle = RandomStyle.Arbitrary;
        }

        double value = 0; // difference / arrSize;
        double diff = 0;
        for (int i = 0; i < arraySize; i++) {
          switch (randStyle) {
            case Fixed_Pace:
              if (i > 0) {
                value = (1.0 / arraySize) * (i + 1);
              }
              break;

            case Arbitrary:
              value = Math.random();
              diff = 1 - value;
              break;

            default:
              _LOG.error("random style enum not implemented");
              break;
          }
          result[i] = Precision.round(min + (value * (difference + diff)), 2);
        }
        break;

      default:

        OfInt randIdx = new Random().ints(arraySize, 0, values.length).iterator();
        for (int i = 0; i < arraySize && randIdx.hasNext(); i++) {
          result[i] = values[randIdx.nextInt()];
        }
        break;
    }

    return result;
  }
}