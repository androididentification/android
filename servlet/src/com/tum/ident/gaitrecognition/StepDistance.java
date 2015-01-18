package com.tum.ident.gaitrecognition;

import java.io.Serializable;

import com.tum.ident.fastdtw.dtw.FastDTW;
import com.tum.ident.fastdtw.dtw.TimeWarpInfo;
import com.tum.ident.fastdtw.timeseries.TimeSeries;
import com.tum.ident.fastdtw.util.DistanceFunction;
import com.tum.ident.fastdtw.util.DistanceFunctionFactory;
public class StepDistance implements Serializable{

	private static final long serialVersionUID = 1L;
	public StepItem step1;
	public StepItem step2;
	public double dwtDistance;
	public StepDistance(StepItem step1,StepItem step2,double dwtDistance){
		this.step1 = step1;
		this.step2 = step2;
		this.dwtDistance = dwtDistance;
	}
	
	public static double dwtDistance(StepItem stepItem1,StepItem stepItem2, int radius){
		double result = -1;
		if(stepItem1.stepData!=null && stepItem2.stepData != null){
			final TimeSeries tsI = new TimeSeries(stepItem1.stepData);
			final TimeSeries tsJ = new TimeSeries(stepItem2.stepData);
			final DistanceFunction distFn;
			distFn = DistanceFunctionFactory.getDistFnByName("ManhattanDistance"); 
			final TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsI, tsJ, radius, distFn);
			result = info.getDistance();
		}
		return result;
	}
}
