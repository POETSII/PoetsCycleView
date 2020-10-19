package ncl.poets.cycleview;

public class FrameControl {

	public static final Integer[] frameRatePoints = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
	public static final Integer defaultFrameRate = frameRatePoints[9];

	public int frameRate = defaultFrameRate;
	
}
