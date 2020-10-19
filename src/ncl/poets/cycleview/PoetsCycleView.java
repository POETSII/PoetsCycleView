package ncl.poets.cycleview;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import com.xrbpowered.zoomui.GraphAssist;
import com.xrbpowered.zoomui.UIContainer;
import com.xrbpowered.zoomui.std.UIButton;
import com.xrbpowered.zoomui.std.UIListBox;
import com.xrbpowered.zoomui.std.UIOptionBox;
import com.xrbpowered.zoomui.swing.SwingFrame;
import com.xrbpowered.zoomui.swing.SwingWindowFactory;

public class PoetsCycleView extends UIContainer {

	public static final String rootPath = "out_res";
	
	public static Timeline timeline = null;
	public static PoetsState current = null;
	private static PoetsStateView.TimelineBar timelineBar;
	
	public static final FrameControl frameControl = new FrameControl();
	public static boolean playing = false;
	
	public static final Pattern gridFilenamePattern = Pattern.compile("([AS])([HW])_grid2d(\\d)s(\\d+)\\.txt");
	public static String filenameInfo = null;
	public static boolean alignLast = false;
	
	private static PoetsCycleView ui;
	
	private final PoetsStateView stateView;
	private final UIContainer controlPane;
	private final PoetsStateView.Legend legend;
	private final UIButton resetButton, prevButton, nextButton, skipButton, playButton;
	private final UIOptionBox<Integer> frameRateOptions;
	private final UIListBox outputList;
	private final UIOptionBox<Boolean> alignOptions;
	
	public PoetsCycleView(UIContainer parent) {
		super(parent);
		ui = this;
		
		new Thread() {
			@Override
			public void run() {
				try {
					for(;;) {
						if(current!=null && playing) {
							current.progress(timeline, Math.min(current.cycles+frameControl.frameRate, timeline.lastCycles));
							if(current.cycles>=timeline.lastCycles)
								playing = false;
							repaint();
						}
						Thread.sleep(10);
					}
				}
				catch(InterruptedException e) {
				}
			}
		}.start();
		
		stateView = new PoetsStateView(this);
		legend = new PoetsStateView.Legend(this);
		controlPane = new UIContainer(this) {
			@Override
			protected void paintSelf(GraphAssist g) {
				g.setFont(UIButton.font);
				g.setColor(Color.BLACK);
				if(current!=null)
					g.drawString(String.format("Cycle: %d / %d", current.cycles, timeline.lastCycles), 0, 8, GraphAssist.LEFT, GraphAssist.TOP);
				if(filenameInfo!=null)
					g.drawString(filenameInfo, 0, getHeight()-12, GraphAssist.LEFT, GraphAssist.BOTTOM);
			}
		};
		
		float x = 0;
		float y = 56;
		resetButton = new UIButton(controlPane, "Reset") {
			@Override
			public void onAction() {
				if(current!=null) {
					playing = false;
					current.reset(timeline);
					repaint();
				}
			}
		};
		resetButton.setLocation(x, y);
		x += resetButton.getWidth()+4;
		prevButton = new UIButton(controlPane, "<") {
			@Override
			public void onAction() {
				if(current!=null && !playing) {
					current.progress(timeline, Math.max(current.cycles-frameControl.frameRate, 0));
					repaint();
				}
			}
		};
		prevButton.setLocation(x, y);
		x += prevButton.getWidth()+4;
		nextButton = new UIButton(controlPane, ">") {
			@Override
			public void onAction() {
				if(current!=null && !playing) {
					current.progress(timeline, Math.min(current.cycles+frameControl.frameRate, timeline.lastCycles));
					repaint();
				}
			}
		};
		nextButton.setLocation(x, y);
		x += nextButton.getWidth()+4;
		skipButton = new UIButton(controlPane, "Skip") {
			@Override
			public void onAction() {
				if(current!=null) {
					Long c = timeline.events.higherKey(current.cycles);
					if(c!=null) {
						current.progress(timeline, c);
						repaint();
					}
				}
			}
		};
		skipButton.setLocation(x, y);
		x += skipButton.getWidth()+4;
		playButton = new UIButton(controlPane, "Play") {
			@Override
			public void onAction() {
				playing = !playing;
			}
			@Override
			public void paint(GraphAssist g) {
				label = playing ? "Pause" : "Play";
				super.paint(g);
			}
		};
		playButton.setLocation(x, y);
		x += playButton.getWidth()+4;
		frameRateOptions = new UIOptionBox<Integer>(controlPane, FrameControl.frameRatePoints) {
			@Override
			protected void onOptionSelected(Integer value) {
				frameControl.frameRate = value;
			}
		};
		frameRateOptions.setSize(playButton.getWidth(), playButton.getHeight());
		frameRateOptions.selectOption(FrameControl.defaultFrameRate);
		frameRateOptions.setLocation(x, y);
		x += frameRateOptions.getWidth()+4;

		controlPane.setSize(x-4, 110);
		
		timelineBar = new PoetsStateView.TimelineBar(controlPane);
		timelineBar.setSize(controlPane.getWidth(), timelineBar.getHeight());
		timelineBar.setLocation(0, 24);

		File[] files = new File(rootPath).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return gridFilenamePattern.matcher(name).matches();
			}
		});
		String[] fileNames = new String[files.length];
		for(int i=0; i<files.length; i++)
			fileNames[i] = files[i].getName();
		Arrays.sort(fileNames);
		outputList = new UIListBox(this, fileNames) {
			@Override
			public void onClickSelected() {
				loadTimeline(rootPath+"/"+(String)(getSelectedItem().object));
			}
		};
		
		alignOptions = new UIOptionBox<Boolean>(this, new Boolean[] {false, true}) {
			@Override
			protected String formatOption(Boolean value) {
				return Timeline.alignLastLabel(value);
			}
			@Override
			protected void onOptionSelected(Boolean value) {
				alignLast = value;
			}
		};
	}
	
	@Override
	public void layout() {
		outputList.setSize(getWidth()*0.25f, getHeight()-alignOptions.getHeight());
		outputList.setLocation(getWidth()-outputList.getWidth(), 0);
		alignOptions.setSize(outputList.getWidth(), alignOptions.getHeight());
		alignOptions.setLocation(outputList.getX(), outputList.getHeight());
		controlPane.setLocation((outputList.getX()-controlPane.getWidth())/2, getHeight()-controlPane.getHeight());
		legend.setLocation((outputList.getX()-legend.getWidth())/2, controlPane.getY()-legend.getHeight());
		stateView.setSize(outputList.getX(), legend.getY());
		stateView.setLocation(0, 0);
		super.layout();
	}
	
	@Override
	protected void paintSelf(GraphAssist g) {
		g.fill(this, Color.WHITE);
	}
	
	public static void loadTimeline(String path) {
		playing = false;
		current = null;
		timeline = null;
		filenameInfo = "Loading...";
		if(timelineBar!=null)
			timelineBar.barImage = null;
		if(ui!=null)
			ui.repaint();
		
		new Thread() {
			@Override
			public void run() {
				timeline = Timeline.fromData(TimelineData.load(path), alignLast);
				if(timeline!=null) {
					current = new PoetsState(timeline.nodeCount);
					current.reset(timeline);
					
					Matcher m = gridFilenamePattern.matcher(new File(path).getName());
					if(m.matches()) {
						StringBuilder sb = new StringBuilder();
						if(m.group(1).equals("S"))
							sb.append("Sync ");
						else if(m.group(1).equals("A"))
							sb.append("Aync ");
						if(m.group(2).equals("H"))
							sb.append("Unweighted ");
						else if(m.group(2).equals("W"))
							sb.append("Weighted ");
						sb.append(m.group(3));
						sb.append("-connected ");
						sb.append(m.group(4));
						sb.append("x");
						sb.append(m.group(4));
						sb.append(" grid / ");
						sb.append(Timeline.alignLastLabel(timeline.alignLast));
						filenameInfo = sb.toString();
					}
					else
						filenameInfo = null;
				}
				else {
					current = null;
					filenameInfo = null;
				}
				if(ui!=null) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							ui.invalidateLayout();
							ui.repaint();
						}
					});
				}
			}
		}.start();
	}
	
	public static void main(String[] args) {
		SwingFrame frame = new SwingFrame(SwingWindowFactory.use(), "POETS cycle viewer", 1000, 640, true, false) {};
		new PoetsCycleView(frame.getContainer());
		frame.show();
	}
}
