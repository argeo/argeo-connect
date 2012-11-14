package org.argeo.connect.demo.gr.ui.providers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.specific.FileProvider;

public class TmpFileProvider implements FileProvider {
	private final static Log log = LogFactory.getLog(TmpFileProvider.class);

	public byte[] getByteArrayFileFromId(String path) {
		try {
			log.debug("From Byte array ");
			return FileUtils.readFileToByteArray(new File(path));
		} catch (IOException e) {
			throw new ArgeoException("Cannot read file at path: " + path, e);
		}
	}

	public InputStream getInputStreamFromFileId(String path) {
		try {

			log.debug("From Input Stream");
			return FileUtils.openInputStream(new File(path));
		} catch (IOException e) {
			throw new ArgeoException("Cannot read file at path: " + path, e);
		}
	}
}