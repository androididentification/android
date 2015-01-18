package com.tum.ident.gaitrecognition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import android.util.Log;

import com.tum.ident.IdentificationConfiguration;
import com.tum.ident.sensors.SensorData;

public class StepClusterList implements Serializable {
	private static final long serialVersionUID = 1L;

	private transient  static final String TAG = "StepData";

	private ArrayList<StepCluster> list = new ArrayList<StepCluster>();

	private long clusterIndex = 0;

	private transient  StepCluster lastCluster = null;

	private transient  int currentClusterCounter = 0;

	public ArrayList<StepCluster> getList(){
		return list;
	}
	
	public void startRecording() {
		Log.v(TAG, "startRecording");
		clean(2);
		currentClusterCounter = 0;
	}

	public void stopRecording() {
		Log.v(TAG, "stopRecording");
		clean(0);
		clean(2);
		if (currentClusterCounter >= 2) {
			int counter = 0;
			sort(list);
			for (StepCluster sItem : list) {
				if (sItem.clusterFound() && sItem.newCluster() == false) {
					counter++;
					sItem.clusterFound(false);
				}
			}
			Iterator<StepCluster> i = list.iterator();
			while (i.hasNext()) {
				StepCluster sItem = i.next();
				if (sItem.clusterFound() && sItem.newCluster()) {
					counter++;
					if (counter > 2) {
						i.remove();
					} else {
						sItem.newCluster(false);
						sItem.clusterFound(false);
					}
				}
			}
		}
		removeEmbodiedClusters();
		currentClusterCounter = 0;
	}

	public void removeEmbodiedClusters() {
		int size = list.size();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				StepCluster sItem1 = list.get(j);
				StepCluster sItem2 = list.get(i);
				if (sItem1 != sItem2) {
					if (sItem2.getNumSteps() > 10) {
						if (sItem1.partOf(sItem2)) {
							list.remove(j);
							if (i >= j) {
								i--;
							}
							j--;
							size--;
						}
					}
				}
			}
		}
	}

	public void prepare(boolean mode) {
		if (list.size() > 0) {
			for (StepCluster sItem : list) {
				sItem.prepare(mode);
			}
		}
	}

	public void calculateAverage() {
		if (list.size() > 0) {
			for (StepCluster sItem : list) {
				sItem.calculateAverage();
			}
		}

	}

	public String getSummary() {
		String summary = "";
		int counter = 0;
		for (StepCluster sItem : list) {
			summary = summary + "cluster (" + counter + ") size: "
					+ sItem.size() + "\n";
			counter++;
		}
		return summary;

	}

	public void add(StepItem stepItem, SensorData sensorData) {
		clean(1);
		Log.v(TAG, "add step!");
		long mergeCounter = 0;
		StepCluster mergeCluster = null;
		for (StepCluster sItem : list) {
			if (sItem.dwtAvgDistance(stepItem) < IdentificationConfiguration.dwtAcceptThreshold) {
				sItem.merge(true);
				if (mergeCluster == null) {
					mergeCluster = sItem;
				}
				mergeCounter++;
			} else {
				sItem.merge(false);
			}
		}
		if (mergeCounter == 0) {
			currentClusterCounter++;
			StepCluster stepCluster = new StepCluster(clusterIndex);
			stepCluster.add(stepItem);
			list.add(stepCluster);
			stepCluster.clusterFound(true);
			stepCluster.newCluster(true);
			lastCluster = stepCluster;

			clusterIndex++;
		} else if (mergeCounter == 1) {
			mergeCluster.add(stepItem);
			mergeCluster.merge(false);
			mergeCluster.clusterFound(true);
			mergeCluster.newCluster(false);
		} else if (mergeCounter > 1) {
			mergeCluster.add(stepItem);
			mergeCluster.merge(false);
			mergeCluster.clusterFound(true);
			mergeCluster.newCluster(false);
			Iterator<StepCluster> i = list.iterator();
			while (i.hasNext()) {
				StepCluster sItem = i.next();
				if (sItem.merge() == true) {
					sItem.merge(false);
					mergeCluster.merge(sItem);
					i.remove();
				}
			}

		}
		sort(list);
		if (mergeCounter > 0) {

			// STEP FOUND!
		}
	}

	public void sort(ArrayList<StepCluster> sortList) {
		// Sorting
		Collections.sort(sortList, new Comparator<StepCluster>() {
			@Override
			public int compare(StepCluster item1, StepCluster item2) {
				int sgn = (int) Math.signum(item2.size() - item1.size());
				return sgn;
			}
		});
	}

	public void clean(int mode) {
		if (mode == 0) {
			// Log.v(TAG,"clean clusters!");
			Iterator<StepCluster> i = list.iterator();
			while (i.hasNext()) {
				StepCluster sItem = i.next();
				if (sItem.getNumSteps() > 0) {
					if (System.currentTimeMillis() - sItem.getLastUpdateTime() > 300000) {
						if (sItem.size() <= 2) {
							i.remove();
						}
					} else {
						if (sItem.size() == 0) {
							// Log.v(TAG,"remove ("+counter+")");

							i.remove();
						}
					}
				} else {
					// Log.v(TAG,"remove ("+counter+")");

					i.remove();
				}
			}
		} else if (mode == 1 || mode == 2) {
			Iterator<StepCluster> i = list.iterator();
			while (i.hasNext()) {
				StepCluster sItem = i.next();
				if (currentClusterCounter > 3 || mode == 2) {
					if (sItem != lastCluster || mode == 2) {
						if (sItem.getNumSteps() > 0) {
							if (System.currentTimeMillis()
									- sItem.getLastUpdateTime() > 3000
									|| mode == 2) {
								if (sItem.size() <= 1) {
									i.remove();
									currentClusterCounter--;
								}
							}
						} else {

							i.remove();
							currentClusterCounter--;
						}
					}
				}
			}
			if (currentClusterCounter < 0) {
				currentClusterCounter = 0;
			}
		}
		Iterator<StepCluster> i = list.iterator();
		while (list.size() > 20) {
			long min = -1;
			StepCluster removeItem = null;
			while (i.hasNext()) {
				StepCluster sItem = i.next();
				if (sItem.getNumSteps() > 0) {
					if (sItem.size() < min || min == -1) {
						min = sItem.size();
						removeItem = sItem;
					}
				} else {
					removeItem = sItem;
					break;
				}
			}
			if (removeItem != null) {
				list.remove(removeItem);
			} else {
				break;
			}
		}

	}
}
