package org.argeo.cms.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsImageManager;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Label;

/** Manages only public images so far. */
public class SimpleImageManager implements CmsImageManager, CmsNames {
	private final static Log log = LogFactory.getLog(SimpleImageManager.class);
	private MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

	public Boolean load(Node node, Label lbl, Point preferredSize)
			throws RepositoryException {
		Point imageSize = getSize(node);
		Point size;
		Boolean loaded;
		if (preferredSize == null)
			size = imageSize;
		else
			size = preferredSize;
		if (size == null)
			size = CmsConstants.NO_IMAGE_SIZE;

		String imgTag;
		try {
			imgTag = getImageTag(node);
		} catch (Exception e) {
			// throw new CmsException("Cannot retrieve image", e);
			log.error("Cannot retrieve image", e);
			imgTag = CmsUtils.noImg(size);
			loaded = false;
		}

		if (imgTag == null) {
			loaded = false;
			imgTag = CmsUtils.noImg(size);
		} else
			loaded = true;
		if (lbl != null) {
			lbl.setText(imgTag);
			lbl.setSize(size);
		} else
			loaded = false;
		return loaded;
	}

	public Point getSize(Node node) throws RepositoryException {
		if (!node.isNodeType(CmsTypes.CMS_IMAGE))
			return null;
		return new Point((int) node.getProperty(CmsNames.CMS_IMAGE_WIDTH)
				.getLong(), (int) node.getProperty(CmsNames.CMS_IMAGE_HEIGHT)
				.getLong());

	}

	@Override
	public String getImageTag(Node node) throws RepositoryException {
		return getImageTagBuilder(node).append("/>").toString();
	}

	@Override
	public StringBuilder getImageTagBuilder(Node node)
			throws RepositoryException {
		return CmsUtils.imgBuilder(getImageUrl(node),
				node.getProperty(CMS_IMAGE_WIDTH).getString(), node
						.getProperty(CMS_IMAGE_HEIGHT).getString());
	}

	@Override
	public String getImageUrl(Node node) throws RepositoryException {
		String name = getResourceName(node);
		ResourceManager resourceManager = RWT.getResourceManager();
		if (!resourceManager.isRegistered(name)) {
			InputStream inputStream = null;
			Binary binary = null;
			try {
				if (node.isNodeType(NodeType.NT_FILE))
					binary = node.getNode(Node.JCR_CONTENT)
							.getProperty(Property.JCR_DATA).getBinary();
				else if (node.isNodeType(CmsTypes.CMS_STYLED)) {
					binary = node.getProperty(CMS_DATA).getBinary();
				} else {
					throw new CmsException("Unsupported node " + node);
				}
				inputStream = binary.getStream();
				resourceManager.register(name, inputStream);
			} finally {
				IOUtils.closeQuietly(inputStream);
				JcrUtils.closeQuietly(binary);
			}
		}
		return resourceManager.getLocation(name);
	}

	protected String getResourceName(Node node) throws RepositoryException {
		String workspace = node.getSession().getWorkspace().getName();
		return workspace + '_' + node.getIdentifier();
	}

	@Override
	public String uploadImage(Node parentNode, String fileName, InputStream in)
			throws RepositoryException {
		InputStream inputStream = null;
		try {
			byte[] arr = IOUtils.toByteArray(in);
			Node fileNode = JcrUtils.copyBytesAsFile(parentNode, fileName, arr);
			fileNode.addMixin(CmsTypes.CMS_IMAGE);

			inputStream = new ByteArrayInputStream(arr);
			ImageData id = new ImageData(inputStream);
			fileNode.setProperty(CMS_IMAGE_WIDTH, id.width);
			fileNode.setProperty(CMS_IMAGE_HEIGHT, id.height);
			if (!fileNode.hasProperty(Property.JCR_MIMETYPE))
				fileNode.setProperty(Property.JCR_MIMETYPE,
						fileTypeMap.getContentType(fileName));
			fileNode.getSession().save();
			return getImageUrl(fileNode);
		} catch (IOException e) {
			throw new CmsException("Cannot upload image " + fileName + " in "
					+ parentNode, e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
}
