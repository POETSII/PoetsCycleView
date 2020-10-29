package ncl.poets.cycleview;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseFileInfoParser<I extends BaseFileInfoParser.Info> implements FilenameFilter {

	public static final Pattern filenamePattern = Pattern.compile("([AS])([HW])_(.*)\\.txt");

	public static class Info {
		public String inputName;
		public String async;
		public String weights;
		
		public void print() {
			System.out.print(inputName);
			System.out.print("\t");
			System.out.print(async);
			System.out.print("\t");
			System.out.print(weights);
			System.out.print("\t");
		}
		
		public static void printHeader() {
			System.out.print("file\tasync\tweights\t");
		}
	}
	
	public final String rootPath;
	
	public BaseFileInfoParser(String rootPath) {
		this.rootPath = rootPath;
	}
	
	public String[] listFiles() {
		File[] files = new File(rootPath).listFiles(this);
		String[] fileNames = new String[files.length];
		for(int i=0; i<files.length; i++)
			fileNames[i] = files[i].getName();
		return fileNames;
	}
	
	public String withPath(String fileName) {
		return rootPath+"/"+fileName;
	}
	
	protected abstract I createInfo();
	
	protected boolean fillInfo(I info, String fileName) {
		Matcher m = filenamePattern.matcher(fileName);
		if(!m.matches())
			return false;
		info.async = m.group(1);
		info.weights = m.group(2);
		info.inputName = m.group(3);
		return true;
	}
	
	public I getInfo(String path) {
		I info = createInfo();
		String fileName = new File(path).getName();
		if(!fillInfo(info, fileName))
			return null;
		return info;
	}

	protected void buildInfoString(StringBuilder sb, I info) {
		if(info.async.equals("S"))
			sb.append("Sync ");
		else if(info.async.equals("A"))
			sb.append("Aync ");
		if(info.weights.equals("H"))
			sb.append("Unweighted ");
		else if(info.weights.equals("W"))
			sb.append("Weighted ");
	}
	
	public String getInfoString(I info) {
		if(info==null)
			return null;
		
		StringBuilder sb = new StringBuilder();
		buildInfoString(sb, info);
		return sb.toString();
	}

	public String getInfoString(String path) {
		return getInfoString(getInfo(path));
	}
	
}
