package org.argeo.photo.manager;

import java.io.File;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Manager {

	public static Set<PicFile> listPicFiles(File dir, MessageFormat format,
			Long srcId) {
		SortedSet<PicFile> set = new TreeSet<PicFile>(
				new Comparator<PicFile>() {

					public int compare(PicFile o1, PicFile o2) {
						return o1.getDesc().getPhotoId().intValue()
								- o2.getDesc().getPhotoId().intValue();
					}

				});

		for (File file : dir.listFiles()) {
			try {
				if (!file.isDirectory()) {
					PicFile picFile = new PicFile(file, format);
					picFile.getDesc().setSrcId(srcId);
					set.add(picFile);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return set;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File("C:/test/argeophoto/0612-0026-ManifSannine/JPEG");
		Long srcId = new Long(26);
		MessageFormat format = new MessageFormat(
				"{3,date,yyMMdd}-{2}-{1,number,0000}");

		Set<PicFile> set = listPicFiles(dir, format, srcId);
		String pattern = "{0,number,0000}-{1,number,000}-{2}-{3,date,yyMM}";
		for (PicFile picFile : set) {
			PhotoDesc desc = picFile.getDesc();
			System.out.println(MessageFormat.format(pattern, desc.getValues()));
		}
	}

}
