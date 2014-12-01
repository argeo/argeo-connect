package org.argeo.photo.manager;

import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;

/** Service interface for managing pictures. */
public interface PictureManager {
	public InputStream getPictureAsStream(String relativePath);

	public List<Node> getChildren(String relativePath);

	public Boolean hasChildren(String relativePath);
}
