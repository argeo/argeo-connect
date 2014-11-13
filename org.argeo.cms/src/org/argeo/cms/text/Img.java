package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsImageManager;
import org.argeo.cms.CmsSession;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.internal.JcrFileUploadReceiver;
import org.argeo.cms.viewers.Section;
import org.argeo.cms.viewers.SectionPart;
import org.argeo.cms.widgets.EditableImage;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.addons.fileupload.FileUploadReceiver;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** An image within the Argeo Text framework */
public class Img extends EditableImage implements SectionPart {
	private static final long serialVersionUID = 6233572783968188476L;

	// private final static Log log = LogFactory.getLog(Img.class);

	private final TextSection section;

	private final CmsImageManager imageManager;
	private FileUploadListener fileUploadListener;

	public Img(Composite parent, int swtStyle, Node imgNode)
			throws RepositoryException {
		this((TextSection) TextSection.findSection(parent), parent, swtStyle,
				imgNode);
	}

	Img(TextSection section, Composite parent, int swtStyle, Node imgNode)
			throws RepositoryException {
		super(parent, swtStyle, imgNode, false);
		this.section = section;
		imageManager = CmsSession.current.get().getImageManager();
	}

	@Override
	protected Control createControl(Composite box, String style,
			Integer preferredHeight) {
		if (isEditing()) {
			try {
				return createImageChooser(box, style);
			} catch (RepositoryException e) {
				throw new CmsException("Cannot create image chooser", e);
			}
		} else {
			return createLabel(box, style);
		}
	}

	@Override
	public synchronized void stopEditing() {
		super.stopEditing();
		fileUploadListener = null;
	}

	@Override
	protected String createImgTag() throws RepositoryException {
		Node imgNode = getNode();

		String imgTag;
		if (imgNode.isNodeType(CmsTypes.CMS_IMAGE)) {
			imgTag = imageManager.getImageTag(imgNode);
		} else {
			imgTag = CmsUtils.noImg(NO_IMAGE_SIZE);
		}

		return imgTag;
	}

	@Override
	protected synchronized Boolean load(Control lbl) {
		try {
			Node imgNode = getNode();
			boolean loaded = imageManager
					.load(imgNode, lbl, getPreferredSize());
			getParent().layout();
			return loaded;
		} catch (RepositoryException e) {
			throw new CmsException("Cannot load " + getNodeId()
					+ " from image manager", e);
		}
	}

	protected Control createImageChooser(Composite box, String style)
			throws RepositoryException {
		// createLabel(box, style);

		JcrFileUploadReceiver receiver = new JcrFileUploadReceiver(getNode()
				.getParent(), getNode().getName());
		final FileUploadHandler uploadHandler = prepareUpload(receiver);
		final ServerPushSession pushSession = new ServerPushSession();
		final FileUpload fileUpload = new FileUpload(box, SWT.NONE);
		// fileUpload.moveAbove(null);
		CmsUtils.style(fileUpload, style);
		CmsUtils.markup(fileUpload);
		// fileUpload.setText("...");
		fileUpload.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				true));
		fileUpload.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -9158471843941668562L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				pushSession.start();
				fileUpload.submit(uploadHandler.getUploadUrl());
			}
		});
		return fileUpload;
	}

	protected FileUploadHandler prepareUpload(FileUploadReceiver receiver) {
		final FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
		if (fileUploadListener != null)
			uploadHandler.addUploadListener(fileUploadListener);
		return uploadHandler;
	}

	@Override
	public Section getSection() {
		return section;
	}

	public void setFileUploadListener(FileUploadListener fileUploadListener) {
		this.fileUploadListener = fileUploadListener;
	}

}
