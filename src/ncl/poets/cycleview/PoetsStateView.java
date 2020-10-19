package ncl.poets.cycleview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.UIElement;

public class PoetsStateView extends UIElement {

	public static class Legend extends UIElement {
		public Legend(UIContainer parent) {
			super(parent);
			setSize(PoetsState.NodeState.values().length*100, 40);
		}
		@Override
		public void paint(GraphAssist g) {
			int x = 0;
			float pix = getPixelScale();
			int sw = (int)(20/pix);
			for(PoetsState.NodeState s : PoetsState.NodeState.values()) {
				if(s.label==null)
					continue;
				g.startPixelMode(this);
				g.setColor(s.color);
				g.graph.fillRect((int)(x/pix), sw/2, sw, sw);
				g.setColor(Color.BLACK);
				g.graph.drawRect((int)(x/pix), sw/2, sw-1, sw-1);
				g.finishPixelMode();
				g.drawString(s.label, x+28, getHeight()/2, GraphAssist.LEFT, GraphAssist.CENTER);
				x += 100;
			}
		}
	}
	
	public static class TimelineBar extends UIElement {
		public BufferedImage barImage = null;
		public TimelineBar(UIContainer parent) {
			super(parent);
			setSize(0, 16);
		}
		@Override
		public void paint(GraphAssist g) {
			Timeline timeline = PoetsCycleView.timeline;
			PoetsState current = PoetsCycleView.current;
			if(current==null)
				return;
			
			float pix = getPixelScale();
			int w = (int)(getWidth()/pix);
			int h = (int)(8/pix);
			if(barImage==null) {
				barImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = (Graphics2D)barImage.getGraphics();
				Color colorOn = new Color(0x555555);
				Color colorOff = new Color(0xdddddd);
				long prev = 0;
				for(int x=0; x<w; x++) {
					long c = (long)(timeline.lastCycles/(double)w*x);
					Long next = timeline.events.higherKey(prev);
					g2.setColor(next==null || next>c ? colorOff : colorOn);
					g2.fillRect(x, 0, 1, h);
					prev = c;
				}
			}
			
			g.startPixelMode(this, true);
			g.graph.drawImage(barImage, 0, 0, null);
			g.setColor(Color.BLACK);
			int x = (int)(w*current.cycles/(double)timeline.lastCycles);
			g.graph.fillPolygon(new int[] {x-h, x, x+h}, new int[] {h*2, h, h*2}, 3);
			g.finishPixelMode();
		}
		@Override
		public boolean onMouseDown(float x, float y, Button button, int mods) {
			PoetsState current = PoetsCycleView.current;
			if(current!=null && !PoetsCycleView.playing) {
				Timeline timeline = PoetsCycleView.timeline;
				long c = (long)(timeline.lastCycles/(double)getWidth()*x);
				current.progress(timeline, c);
				repaint();
			}
			return true;
		}
	}
	
	public int gridWidth;
	public int gridHeight;
	public int cellSize;
	
	private float offsx, offsy;
	
	public PoetsStateView(UIContainer parent) {
		super(parent);
	}

	@Override
	public void layout() {
		PoetsState state = PoetsCycleView.current;
		if(state!=null) {
			gridWidth = (int)Math.sqrt(state.nodeCount);
			gridHeight = (int)Math.ceil(state.nodeCount/(float)gridWidth);
			float pix = getPixelScale();
			cellSize = Math.min((int)(getWidth()/pix/gridWidth), (int)(getHeight()/pix/gridHeight));
			if(cellSize<1)
				cellSize = 1;
			offsx = (getWidth()/pix-gridWidth*cellSize)/2;
			offsy = (getHeight()/pix-gridHeight*cellSize)/2;
		}
	}
	
	@Override
	public void paint(GraphAssist g) {
		PoetsState state = PoetsCycleView.current;
		if(state==null)
			return;
		
		g.startPixelMode(this);
		g.pushTx();
		g.translate(offsx, offsy);
		for(int x=0; x<gridWidth; x++)
			for(int y=0; y<gridHeight; y++) {
				int node = y*gridWidth+x;
				if(node<state.nodeCount) {
					g.setColor(state.nodes[node].color);
					g.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
				}
			}
		g.setColor(Color.BLACK);
		g.graph.drawRect(0, 0, gridWidth*cellSize-1, gridHeight*cellSize-1);
		g.popTx();
		g.finishPixelMode();
	}

}
