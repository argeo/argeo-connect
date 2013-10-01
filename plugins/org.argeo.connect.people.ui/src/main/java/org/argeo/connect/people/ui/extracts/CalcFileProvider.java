package org.argeo.connect.people.ui.extracts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.eclipse.ui.specific.FileProvider;

public class CalcFileProvider implements FileProvider {

	@Override
	public byte[] getByteArrayFileFromId(String path) {
		try {
			return FileUtils.readFileToByteArray(new File(path));
		} catch (IOException e) {
			throw new PeopleException("Cannot read file at path: " + path, e);
		}
	}

	@Override
	public InputStream getInputStreamFromFileId(String path) {
		try {
			return FileUtils.openInputStream(new File(path));
		} catch (IOException e) {
			throw new PeopleException("Cannot read file at path: " + path, e);
		}
	}

}
