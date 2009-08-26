package de.deutschdiachrondigital.dddquery.helper;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestStaircaseJoin {

	class Node {
		String name;
		int pre;
		int post;
		
		Node (String name, int pre, int post) {
			this.name = name;
			this.pre = pre;
			this.post = post;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	final Node a = new Node("a", 0, 9);
	final Node b = new Node("b", 1, 1);
	final Node c = new Node("c", 2, 0);
	final Node d = new Node("d", 3, 2);
	final Node e = new Node("e", 4, 8);
	final Node f = new Node("f", 5, 5);
	final Node g = new Node("g", 6, 3);
	final Node h = new Node("h", 7, 4);
	final Node i = new Node("i", 8, 7);
	final Node j = new Node("j", 9, 6);
	
	Node[] doc = { a, b, c, d, e, f, g, h, i, j	};

	public List<Node> staircaseDescending(Node[] contextNodes) {
		/* partition ??? */
		List<Node> result = new ArrayList<Node>();
		Node c_from = contextNodes[0];
		for (int i = 1; i < contextNodes.length; ++i) {
			Node c_to = contextNodes[i];
			if (c_to.post < c_from.post) {
				/* prune */
			} else {
				result.addAll(scanPartitionDescending(c_from.pre + 1, c_to.pre - 1, c_from.post));
				c_from = c_to;
			}	
		}
		Node c_to = j;
		result.addAll(scanPartitionDescending(c_from.pre + 1, c_to.pre, c_from.post));
		return result;
	}

	public List<Node> scanPartitionDescending(int pre_from, int pre_to, int post) {
		List<Node> result = new ArrayList<Node>();
		for (int i = pre_from; i <= pre_to; ++i) {
			Node n = doc[i];
			if (n.post < post) {
				result.add(n);
			} else
				break; /* skip */
		}
		return result;
	}
	
	public List<Node> staircaseAscending(Node[] contextNodes) {
		List<Node> result = new ArrayList<Node>();
		Node c_to = contextNodes[0];
		Node n = a;
		result.addAll(scanPartitionAscending(n.pre, c_to.pre, c_to.post));
		for (int i = 1; i < contextNodes.length; ++i) {
			Node c_from = c_to;
			c_to = contextNodes[i];
			result.addAll(scanPartitionAscending(c_from.pre + 1, c_to.pre, c_to.post));
		}
		return result;
	}
	
	public List<Node> scanPartitionAscending(int pre_from, int pre_to, int post) {
		List<Node> result = new ArrayList<Node>();
		for (int i = pre_from; i <= pre_to; ++i) {
			Node n = doc[i];
			if (n.post >= post) {
				result.add(n);
			} 
		}
		return result;
	}
	
	
	@Test
	public void example() {
		Node[] context = {c, g, j};
		System.out.println(staircaseAscending(context));
	}
	
}
