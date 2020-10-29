package ncl.poets.cycleview;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GridFileInfoParser extends BaseFileInfoParser<GridFileInfoParser.GridInfo> {

	public static final Pattern gridFilenamePattern = Pattern.compile("([AS])([HW])_grid2d(\\d)s(\\d+)\\.txt");
	
	public static class GridInfo extends BaseFileInfoParser.Info {
		public int fanout;
		public int gridSize;
	}
	
	public GridFileInfoParser(String rootPath) {
		super(rootPath);
	}

	@Override
	protected GridInfo createInfo() {
		return new GridInfo();
	}
	
	protected boolean fillInfo(GridInfo info, String fileName) {
		if(!super.fillInfo(info, fileName))
			return false;
		Matcher m = gridFilenamePattern.matcher(fileName);
		if(!m.matches())
			return false;
		info.fanout = Integer.parseInt(m.group(3));
		info.gridSize = Integer.parseInt(m.group(4));
		return true;
	}
	
	protected void buildInfoString(StringBuilder sb, GridInfo info) {
		super.buildInfoString(sb, info);
		sb.append(info.fanout);
		sb.append("-connected ");
		sb.append(info.gridSize);
		sb.append("x");
		sb.append(info.gridSize);
		sb.append(" grid");
	}

	@Override
	public boolean accept(File dir, String name) {
		return gridFilenamePattern.matcher(name).matches();
	}

}
