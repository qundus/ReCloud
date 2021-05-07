package com.upm.researcher.templates.setups;

import com.upm.researcher.utils.NumberMixer.RandomStyle;

import org.cloudbus.cloudsim.UtilizationModel;

/**A class to store the task's/cloudlet's data */
public class ACloudlet
{
    /**Length of the task in MI*/
    public long minLength, maxLength;
    /**Number of processing units*/
    public int minPes, maxPes;
    /**Length of the file size*/
    public long minFileSize, maxFileSize;
    /**Length of the file size*/
    public long minOutputSize, maxOutputSize;
    /**The random number generation style if any*/
    public RandomStyle randStyle;

    /**Length array created during runtime.*/
    public long[] lengths;
    /**Pes array created during runtime.*/
    public int[] pes;
    /**Filesize array created during runtime.*/
    public long[] fileSizes;
    /**Outputsize array created during runtime.*/
    public long[] outputSizes;


    public Class<? extends UtilizationModel> utilizationModelCpu;
    public Class<? extends UtilizationModel> utilizationModelRam;
    public Class<? extends UtilizationModel> utilizationModelBw;

    /**External file rating.*/
    public int workLoadRating;
    /**External file path.*/
    public String workloadFile;
}