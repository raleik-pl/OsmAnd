package com.osmand.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.osmand.osm.MapUtils;
import com.osmand.osm.Relation;
import com.osmand.osm.Way;

public class TransportRoute extends MapObject {
	private List<Way> ways;
	private List<TransportStop> forwardStops = new ArrayList<TransportStop>();
	private List<TransportStop> backwardStops = new ArrayList<TransportStop>();
	private String ref;
	private String operator;
	private String type;
	
	public TransportRoute(Relation r, String ref){
		super(r);
		this.ref = ref;
	}
	
	public List<TransportStop> getForwardStops() {
		return forwardStops;
	}
	
	public List<TransportStop> getBackwardStops() {
		return backwardStops;
	}
	
	public List<Way> getWays() {
		if(ways == null){
			return Collections.emptyList();
		}
		return ways;
	}
	
	public void addWay(Way w){
		if(ways == null){
			ways = new ArrayList<Way>();
		}
		ways.add(w);
	}
	
	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getAvgBothDistance(){
		int d = 0;
		for(int i=1; i< backwardStops.size(); i++){
			d += MapUtils.getDistance(backwardStops.get(i-1).getLocation(), backwardStops.get(i).getLocation());
		}
		for(int i=1; i< forwardStops.size(); i++){
			d += MapUtils.getDistance(forwardStops.get(i-1).getLocation(), forwardStops.get(i).getLocation());
		}
		return d;
	}
	
	
}
