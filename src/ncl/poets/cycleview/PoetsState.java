package ncl.poets.cycleview;

import java.awt.Color;
import java.util.ArrayList;

public class PoetsState {

	public enum NodeState {
		reset(Color.BLACK, "Reset"),
		start(new Color(0x003399), "Init event"),
		firstUpdate(new Color(0x0099ff), "First update"),
		lastUpdate(new Color(0xffdd00), "Last update"),
		finish(new Color(0xffffee), "Finish event");
		
		public final String label;
		public final Color color;
		
		private NodeState(Color color, String label) {
			this.label = label;
			this.color = color;
		}
	}
	
	public final int nodeCount;
	public final NodeState[] nodes;
	public long cycles;
	
	public PoetsState(int nodeCount) {
		this.nodeCount = nodeCount;
		this.nodes = new NodeState[nodeCount];
		reset();
	}
	
	public void reset() {
		for(int i=0; i<nodeCount; i++)
			nodes[i] = NodeState.reset;
		cycles = 0;
	}
	
	private int applyEvents(Timeline t, long c) {
		ArrayList<Timeline.Event> list = t.events.get(c);
		if(list==null)
			return 0;
		for(Timeline.Event e : list) {
			nodes[e.node] = e.state;
			cycles = e.cycles;
		}
		return 1;
	}
	
	public int reset(Timeline t) {
		reset();
		return applyEvents(t, 0);
	}
	
	public int progress(Timeline t, long targetCycles) {
		int count = 0;
		if(targetCycles<cycles)
			count += reset(t);
		for(;;) {
			Long c = t.events.higherKey(cycles);
			if(c==null || c>targetCycles) {
				cycles = targetCycles;
				return count;
			}
			count += applyEvents(t, c);
		}
	}
	
}
