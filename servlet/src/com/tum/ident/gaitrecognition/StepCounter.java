package com.tum.ident.gaitrecognition;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class StepCounter implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public int[] stepWeights = new int[7];
	public long[] firstStepTime = new long[7];
	public long[] lastStepTime  = new long[7];
	public double[] steps = new double[7];
	public double firstStepTimeAvg;
	public double lastStepTimeAvg;
	public long lastStepTimeCache  = 0;
	public double avgsteps;
	public int stepDayOffset = 0;
	public int currentDayOfYear = -1;
	public int currentDay = -1;
	public int currentSteps = 0;


	private void updateAverage(){
		double avg = 0;
		double weight = 0;
		double firstAvg = 0;
		double lastAvg = 0;
		for(int i = 0;i<7;i++){
			avg = avg+steps[i]*stepWeights[i];
			weight = weight+stepWeights[i];
			firstAvg = firstAvg+firstStepTime[i]*stepWeights[i];
			lastAvg  = lastAvg+lastStepTime[i]*stepWeights[i];
		}
		if(weight > 0){
			avgsteps = avg/weight;
			firstStepTimeAvg = firstAvg/weight;
			lastStepTimeAvg  = lastAvg/weight;
		}
		else{
			avgsteps = 0;
		}
	}
	
	public void newDay(){
		if(currentDay!=-1){
			int day = currentDay;
			lastStepTime[day] = lastStepTimeCache;
			steps[day] = ((double)steps[day]*stepWeights[day]+currentSteps)/((double) stepWeights[day]+1);
			stepWeights[day]++;	
			currentSteps = 0;
			updateAverage();
		}
		Calendar calendar = Calendar.getInstance();
		currentDay = calendar.get(Calendar.DAY_OF_WEEK)-1;
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		firstStepTime[currentDay] = hours*3600+minutes*60+seconds;
	}
	
	public void setLastStepTime(){
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		lastStepTimeCache = hours*3600+minutes*60+seconds;
	}
	
	public int setSteps(int s){
		int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if(dayOfYear!=currentDayOfYear){
			newDay();
			stepDayOffset = s;
		}
		currentDayOfYear = dayOfYear;
		currentSteps = s-stepDayOffset;
		setLastStepTime();
		return currentSteps;
	}
	
	public int addStep(){
		int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if(dayOfYear!=currentDayOfYear){
			newDay();
			stepDayOffset = 0;
		}
		else{
			currentSteps = currentSteps+1;
		}
		currentDayOfYear = dayOfYear;
		setLastStepTime();
		return currentSteps;

	}
	

}
