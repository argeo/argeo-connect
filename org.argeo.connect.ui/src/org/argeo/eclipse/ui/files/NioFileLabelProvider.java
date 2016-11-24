package org.argeo.eclipse.ui.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/** Expect a {@link Path} as input element */
public class NioFileLabelProvider extends ColumnLabelProvider {
	private static final long serialVersionUID = 2160026425187796930L;
	public final static String SIZE = "size";
	public final static String LAST_MODIFIED = "last-modified";
	public final static String TYPE = "type";

	private final String propName;

	public NioFileLabelProvider(String propName) {
		this.propName = propName;
	}

	@Override
	public String getText(Object element) {
		Path path = (Path) element;
		try {
			switch (propName) {
			case SIZE:
				return FilesUiUtils.humanReadableByteCount(Files.size(path), false);
			case LAST_MODIFIED:
				return Files.getLastModifiedTime(path).toString();
			case TYPE:
				if (Files.isDirectory(path))
					return "Folder";
				else {
					String mimeType = Files.probeContentType(path);
					if (EclipseUiUtils.isEmpty(mimeType))
						return "Unknown";
					else
						return mimeType;
				}
			default:
				throw new IllegalArgumentException("Unsupported property " + propName);
			}
		} catch (IOException ioe) {
			throw new FilesException("Cannot get property " + propName + " on " + path.toString());
		}
	}
}
