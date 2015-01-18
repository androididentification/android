package com.tum.ident.spectrum;

import java.io.Serializable;
import java.util.ArrayList;

import com.tum.ident.IdentificationConfiguration;


public class SpectrumItemList implements Serializable {
	private static final long serialVersionUID = 1L;

	private ArrayList<SpectrumItem> list = new ArrayList<SpectrumItem>();


	public void add(SpectrumItem spectrumItem) {
		
		boolean spectrumFound = false;
		for (SpectrumItem sItem : list) {
			if(SpectrumItem.distance(spectrumItem, sItem)<IdentificationConfiguration.spectrumThreshold){
				sItem.merge(spectrumItem);
				spectrumFound = true;
				break;
			}
		}
		
		if (spectrumFound) {
			
		} else {
			list.add(spectrumItem);
		}
	}

	public ArrayList<SpectrumItem> getList(){
		return list;
	}
	
	
}
