package ncl.poets.cycleview;

import java.io.File;
import java.util.Scanner;

public class TimelineData {

	public static class Entry {
		public int node;
		public long start, firstUpdate, lastUpdate, finish;
	}
	
	public final String inputName; 
	public final int nodeCount;
	public final Entry[] entries;

	public long lastCycles = 0;

	public TimelineData(String inputName, int nodeCount) {
		this.inputName = inputName;
		this.nodeCount = nodeCount;
		entries = new Entry[nodeCount];
	}

	public static TimelineData load(String path) {
		try {
			Scanner in = new Scanner(new File(path));
			String inputName = in.nextLine();
			int nodeCount = in.nextInt();
			
			TimelineData d = new TimelineData(inputName, nodeCount);
			
			while(in.hasNext()) {
				Entry e = new Entry();
				e.node = in.nextInt();
				in.nextInt(); // hops
				in.nextInt(); // dist
				e.start = in.nextLong();
				e.firstUpdate = in.nextLong();
				e.lastUpdate = in.nextLong();
				e.finish = in.nextLong();
				if(e.finish>d.lastCycles)
					d.lastCycles = e.finish;
				d.entries[e.node] = e;
			}
			
			in.close();
			return d;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
