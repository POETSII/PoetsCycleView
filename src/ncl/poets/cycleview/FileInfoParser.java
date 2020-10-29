package ncl.poets.cycleview;

import java.io.File;

public class FileInfoParser extends BaseFileInfoParser<BaseFileInfoParser.Info> {

	public FileInfoParser(String rootPath) {
		super(rootPath);
	}

	@Override
	protected Info createInfo() {
		return new Info();
	}

	@Override
	public boolean accept(File dir, String name) {
		return filenamePattern.matcher(name).matches();
	}

	public static void main(String[] args) {
		FileInfoParser files = new FileInfoParser("out_res");
		Info.printHeader();
		System.out.print("minf\tmaxf\tmaxl\t");
		System.out.println();
		
		for(String fileName : files.listFiles()) {
			Info info = files.getInfo(fileName);
			info.print();
			
			TimelineData d = TimelineData.load(files.withPath(fileName));
			long minf = 0;
			long maxf = 0;
			long maxl = 0;
			boolean first = true;
			for(int i=0; i<d.nodeCount; i++) {
				TimelineData.Entry e = d.entries[i];
				if(e.firstUpdate==0 || e.lastUpdate==0)
					continue;
				long offs = d.lastCycles-e.finish;
				long f = offs+e.firstUpdate;
				long l = offs+e.lastUpdate;
				if(first || f<minf)
					minf = f;
				if(first || f>maxf)
					maxf = f;
				if(first || l>maxl)
					maxl = l;
				first = false;
			}
			System.out.print(minf);
			System.out.print("\t");
			System.out.print(maxf);
			System.out.print("\t");
			System.out.print(maxl);
			System.out.print("\t");
			
			System.out.println();
		}
	}

}
