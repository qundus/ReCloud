package com.upm.researcher.utils;

public class ArrayUtils
{
    

    public static int[][] Clone(int[][] source)
    {
        int[][] dest = new int[source.length][source[0].length];

        for(int i = 0; i < source.length; i++)
        {
            for(int j = 0; j < source[i].length; j++)
            {
                dest[i][j] = source[i][j];
            }
        }

        return dest;
    }

    public static int[][] Clone(double[][] source)
    {
        int[][] dest = new int[source.length][source[0].length];

        for(int i = 0; i < source.length; i++)
        {
            for(int j = 0; j < source[i].length; j++)
            {
                dest[i][j] = (int)source[i][j];
            }
        }

        return dest;
    }

    public static double[] Clone(double[] source)
    {
        double[] dest = new double[source.length];

        for(int i = 0; i < source.length; i++)
        {
            dest[i] = source[i];
        }

        return dest;
    }

    public static int[] Clone(int[] source)
    {
        int[] dest = new int[source.length];

        for(int i = 0; i < source.length; i++)
        {
            dest[i] = source[i];
        }

        return dest;
    }
}