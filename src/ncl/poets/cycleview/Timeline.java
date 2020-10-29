package ncl.poets.cycleview;

import java.util.ArrayList;
import java.util.TreeMap;

import ncl.poets.cycleview.PoetsState.NodeState;

public class Timeline {

	public static class Event {
		public long cycles;
		public int node;
		public NodeState state;
	}

	public final String inputName; 
	public final int nodeCount;
	public final boolean alignLast;
	
	public final TreeMap<Long, ArrayList<Event>> events = new TreeMap<>();
	public long lastCycles = 0L;
	
	private void putEvent(long cycles, int node, NodeState state) {
		Event e = new Event();
		e.cycles = cycles;
		e.node = node;
		e.state = state;
		ArrayList<Event> list = events.get(cycles);
		if(list==null) {
			list = new ArrayList<>();
			events.put(cycles, list);
			lastCycles = events.lastKey();
		}
		list.add(e);
	}
	
	public Timeline(String inputName, int nodeCount, boolean alignLast) {
		this.inputName = inputName;
		this.nodeCount = nodeCount;
		this.alignLast = alignLast;
	}
	
	public static Timeline fromData(TimelineData d, boolean alignLast) {
		if(d==null)
			return null;
		Timeline t = new Timeline(d.inputName, d.nodeCount, alignLast);
		
		for(int i=0; i<d.nodeCount; i++) {
			TimelineData.Entry e = d.entries[i];
			if(e.firstUpdate==0 || e.lastUpdate==0)
				continue;
			long offs = alignLast ? (d.lastCycles-e.finish) : 0;
			t.putEvent(offs+e.start, e.node, NodeState.start);
			t.putEvent(offs+e.firstUpdate, e.node, NodeState.firstUpdate);
			t.putEvent(offs+e.lastUpdate, e.node, NodeState.lastUpdate);
			t.putEvent(offs+e.finish, e.node, NodeState.finish);
		}
		
		return t;
	}
	
	public static String alignLastLabel(boolean align) {
		return align ? "Align to last finish" : "Show as reported";
	}
}
