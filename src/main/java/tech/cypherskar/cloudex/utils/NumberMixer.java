package tech.cypherskar.cloudex.utils;

import org.apache.commons.math3.util.Precision;

/** Helps populating random numbers */
public final class NumberMixer
{
	//  *****************************
    //  Enums
    //  *****************************

	/**The random number generation pattern style. */
	public enum RandomStyle
	{
		/**
		 * To generate random numbers with fixed pace between min&max.
		 * <p>
		 * <li>example: min=100, max=500, arraySize=4 then:
		 * <li>(i = 0) => min => 100
		 * <li>(i = 1) => ((500-100)/4)+(i-1) => 200
		 * <li>(i = 2) => ((500-100)/4)+(i-1) => 300
		 * <li>(i = 3) => ((500-100)/4)+(i-1) => 400
		*/
		Fixed_Pace,

		/**To generate random numbers arbitrarily between min&max.*/
		Arbitrary
	}

	//  *****************************
    //  Public Methods
    //  *****************************

	/**
	 * Generates random numbers according to array size and random style
	 * between min and max.
	 * @param arraySize the random numbers pool size.
	 * @param randStyle the random style based on the given options.
	 * @param minValue the lower bound for randomness (inclusive if Fixed_Pace).
	 * @param maxValue the upper bound for randmoness (exclusive if Fixed_Pace).
	 * @return an array containing random numbers based on the given options.
	 */
    public int[] Create(int arraySize, RandomStyle randStyle, int minValue, int maxValue)
    {
        final int difference = Math.abs(maxValue - minValue);

        if (difference == 0 || maxValue < minValue) return new int[]{minValue};
		if (arraySize <= 1)
		{
			arraySize = 1;
			randStyle = RandomStyle.Arbitrary;
		}
		
        
        int[] result = new int[arraySize];
        
        double value = 0;//difference / arrSize;
        double diff = 0;
        for(int i = 0; i < arraySize; i++)
        {
            switch (randStyle)
            {
				case Fixed_Pace:
				if (i > 0) value = (1.0 / arraySize) * (i+1);
				break;

				case Arbitrary:
				value = Math.random();
				diff = 1 - value;
				break;
            }
            result[i] = (int)(minValue + (value * (difference + diff)));
        }

        return result;
    }

    /**
	 * Generates random numbers according to array size and random style
	 * between min and max.
	 * @param arraySize the random numbers pool size.
	 * @param randStyle the random style based on the given options.
	 * @param minValue the lower bound for randomness (inclusive if Fixed_Pace).
	 * @param maxValue the upper bound for randmoness (exclusive if Fixed_Pace).
	 * @return an array containing random numbers based on the given options.
	 */
    public long[] Create(int arraySize, RandomStyle randStyle, long minValue, long maxValue)
    {
        final long difference = Math.abs(maxValue - minValue);

        if (difference == 0 || maxValue < minValue) return new long[]{minValue};
        if (arraySize <= 1)
		{
			arraySize = 1;
			randStyle = RandomStyle.Arbitrary;
		}
		
        
        long[] result = new long[arraySize];
        
        double value = 0;//difference / arrSize;
        double diff = 0;
        for(int i = 0; i < arraySize; i++)
        {
			switch (randStyle)
			{
				case Fixed_Pace:
				if (i > 0) value = (1.0 / arraySize) * (i+1);
				break;
				
				case Arbitrary:
				value = Math.random();
				diff = 1 - value;
				break;
			}
            result[i] = (long)(minValue + (value * (difference + diff)));
        }

        return result;
    }

	/**
	 * Generates random numbers according to array size and random style
	 * between min and max.
	 * @param arraySize the random numbers pool size.
	 * @param randStyle the random style based on the given options.
	 * @param minValue the lower bound for randomness (inclusive if Fixed_Pace).
	 * @param maxValue the upper bound for randmoness (exclusive if Fixed_Pace).
	 * @return an array containing random numbers based on the given options.
	 */
    public double[] Create(int arraySize, RandomStyle randStyle, double minValue, double maxValue)
    {
        final double difference = Math.abs(maxValue - minValue);

        if (difference == 0 || maxValue < minValue) return new double[]{minValue};
        if (arraySize <= 1)
		{
			arraySize = 1;
			randStyle = RandomStyle.Arbitrary;
		}
		
        
        double[] result = new double[arraySize];
        
        double value = 0;//difference / arrSize;
        double diff = 0;
        for(int i = 0; i < arraySize; i++)
        {
			switch (randStyle)
			{
				case Fixed_Pace:
				if (i > 0) value = (1.0 / arraySize) * (i+1);
				break;
				
				case Arbitrary:
				value = Math.random();
				diff = 1 - value;
				break;
			}
            result[i] = Precision.round(minValue + (value * (difference + diff)), 2);
        }

        return result;
    }
}