package com.tum.ident;

public class Util {

	public static double[] normalize(double[] data){
		if(data!=null){
			if(data.length > 0){
				double avg = 0;
				for(int i =0; i < data.length; i++){
					avg = avg+data[i];	
				}
				avg = avg/data.length;
				double v = 0;
				for(int i =0; i < data.length; i++){
					double diff = data[i]-avg;
					v += (diff) * (diff);
				}
				v = v / data.length;
				double sd = (float)Math.sqrt(v);
			    if(sd>0){
					for(int i =0; i < data.length; i++){
						
						data[i] = (data[i]-avg)/sd;
					}
			    }
			}
		}
		return data;
	}
	
	public static double deg2rad(double deg) {
   	 return (deg * Math.PI / 180.0);
   }
   	
	public static double rad2deg(double rad) {
   	return (rad * 180 / Math.PI);
   }
}
