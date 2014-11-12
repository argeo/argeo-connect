package org.argeo.cms;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Label;

/** Read and write access to images. */
public interface CmsImageManager {
	public Boolean load(Node node, Label lbl, Point size)
			throws RepositoryException;

	public Point getSize(Node node) throws RepositoryException;

	/** The related <img tag, with src, width and height set. */
	public String getImageTag(Node node) throws RepositoryException;

	/**
	 * The related <img tag, with url, width and height set. Caller must close
	 * the tag (or add additional attributes).
	 */
	public StringBuilder getImageTagBuilder(Node node)
			throws RepositoryException;

	/**
	 * Returns the remotely accessible URL of the image (registering it if
	 * needed)
	 */
	public String getImageUrl(Node node) throws RepositoryException;

	/** @return URL */
	public String uploadImage(Node parentNode, String fileName, InputStream in)
			throws RepositoryException;
}
