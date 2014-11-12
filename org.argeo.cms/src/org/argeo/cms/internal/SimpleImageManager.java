package org.argeo.cms.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsImageManager;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.graphics.ImageData;

/** Manages only public images so far. */
public class SimpleImageManager implements CmsImageManager, CmsNames {
	private MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

	@Override
	public String getImageTag(Node node) throws RepositoryException {
		return getImageTagBuilder(node).append("/>").toString();
	}

	@Override
	public StringBuilder getImageTagBuilder(Node node)
			throws RepositoryException {
		// StringBuilder buf = new StringBuilder(64);
		// buf.append("<img url='");
		// buf.append(getImageUrl(node));
		// buf.append("' width='");
		// buf.append(node.getProperty(CMS_IMAGE_WIDTH).getString());
		// buf.append("' height=' ");
		// buf.append(node.getProperty(CMS_IMAGE_HEIGHT).getString());
		// buf.append('\'');
		// return buf;
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
				binary = node.getNode(Node.JCR_CONTENT)
						.getProperty(Property.JCR_DATA).getBinary();
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
