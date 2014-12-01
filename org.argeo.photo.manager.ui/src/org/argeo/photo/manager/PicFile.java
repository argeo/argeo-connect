package org.argeo.photo.manager;

import java.io.File;
import java.text.MessageFormat;

public class PicFile {
	private final File file;
	private final MessageFormat format;
	private final PhotoDesc desc;

	public PicFile(File file, MessageFormat format) throws Exception {
		this.file = file;
		this.format = format;
		this.desc = new PhotoDesc(this.format.parse(file.getName()));
	}

	public PhotoDesc getDesc() {
		return desc;
	}

	public File getFile() {
		return file;
	}

	public MessageFormat getFormat() {
		return format;
	}

}
