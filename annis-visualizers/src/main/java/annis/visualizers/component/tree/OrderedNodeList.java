/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.visualizers.component.tree;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A class for finding optimal horizontal positions for node labels given
 * all intervening vertical edges.
 */
public class OrderedNodeList {
	private final double minDistance;
	private final List<NodeStructureData> nodes = new ArrayList<NodeStructureData>();
	private final List<Double> points = new ArrayList<Double>();
	
	public OrderedNodeList(double minDistance_) {
		minDistance = minDistance_;
	}
	
	public void addVerticalEdgePosition(NodeStructureData structData, Point2D pos) {
		int idx = findInsertionIndex(pos.getX());
		nodes.add(idx, structData);
		points.add(idx, pos.getX());
	}

	private int findInsertionIndex(double pos) {
		int idx = Collections.binarySearch(points, pos);
		if (idx < 0) {
			idx = -(idx + 1);
		}
		return idx;
	}

	public double findBestPosition(NodeStructureData nodeStructureData,
			double minX, double maxX) {
		double optimalPos = (minX + maxX) / 2;

		if (nodeStructureData.isContinuous()) {
			return optimalPos;
		} else if (hasConflict(nodeStructureData, optimalPos)) {
			double lastPos = minX;
			double bestPos = minX + minDistance / 2;
			double bestDist = Integer.MAX_VALUE;
			
			for (double x: findConflicts(nodeStructureData, minX, maxX)) {
				double space = Math.abs(x - lastPos);
                if (space > 2 * minDistance) {
                	double regionOptimalPos = nearest(optimalPos, lastPos + minDistance, x - minDistance);
                	double dist = Math.abs(regionOptimalPos - optimalPos);
                	if (dist < bestDist) {
                		bestPos = regionOptimalPos;
                		bestDist = dist;
                	}
                }
                lastPos = x;
			}
            return bestPos;
			
		} else {
			return optimalPos;
		}
	}

	private double nearest(double optimalPos, double min, double max) {
		return Math.max(min, Math.min(max, optimalPos));
	}

	private Collection<Double> findConflicts(NodeStructureData nodeStructureData,
			double minX, double maxX) {
		List<Double> result = new ArrayList<Double>();
		for (int pos: findInRegion(minX, maxX)) {
			if (nodeStructureData.hasVerticalEdgeConflict(nodes.get(pos))) {
				result.add(points.get(pos));
			}
		}
		result.add(maxX);
		return result;
	}

	private boolean hasConflict(NodeStructureData nodeStructureData,
			double atPos) {
        for (int lower: findInRegion(atPos - minDistance, atPos + minDistance)) {
        	if (nodeStructureData.hasVerticalEdgeConflict(nodes.get(lower))) {
        		return true;
        	}
        }
        return false;
	}

	private Collection<Integer> findInRegion(double low, double high) {
		int start = findInsertionIndex(low);
		int end = findInsertionIndex(high);
		List<Integer> l = new ArrayList<Integer>();
		for (int i = start; i < end; i++) {
			l.add(i);
		}
		return l;
	}
}

