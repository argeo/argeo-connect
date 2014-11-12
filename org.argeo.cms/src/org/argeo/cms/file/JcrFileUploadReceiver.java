package org.argeo.cms.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.jcr.JcrUtils;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;
import org.eclipse.swt.graphics.ImageData;

public class JcrFileUploadReceiver extends FileUploadReceiver implements
		CmsNames {
	private Node parentNode;
	private String nodeName;

	/** Uses uploaded file name */
	public JcrFileUploadReceiver(Node parentNode) {
		this(parentNode, null);
	}

	public JcrFileUploadReceiver(Node parentNode, String nodeName) {
		super();
		this.parentNode = parentNode;
		this.nodeName = nodeName;
	}

	@Override
	public void receive(InputStream stream, FileDetails details)
			throws IOException {
		try {
			byte[] arr = IOUtils.toByteArray(stream);
			String fileName = nodeName != null ? nodeName : details
					.getFileName();
			Node fileNode = JcrUtils.copyBytesAsFile(parentNode, fileName, arr);
			String contentType = details.getContentType();
			if (contentType != null) {
				fileNode.addMixin(NodeType.MIX_MIMETYPE);
				fileNode.setProperty(Property.JCR_MIMETYPE, contentType);
			}

			String ext = FilenameUtils.getExtension(details.getFileName());
			// image

			if (ext != null
					&& (ext.equals("png") || ext.equalsIgnoreCase("jpg"))) {
				InputStream inputStream = new ByteArrayInputStream(arr);
				ImageData id = new ImageData(inputStream);
				fileNode.addMixin(CmsTypes.CMS_IMAGE);
				fileNode.setProperty(CMS_IMAGE_WIDTH, id.width);
				fileNode.setProperty(CMS_IMAGE_HEIGHT, id.height);
				processNewFile(fileNode);
				fileNode.getSession().save();
			}
		} catch (RepositoryException e) {
			throw new CmsException("cannot receive " + details, e);
		}
	}

	protected void processNewFile(Node node) {

	}

}
