package org.argeo.cms.widgets;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsConstants;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsImageManager;
import org.argeo.cms.CmsSession;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.file.JcrFileUploadReceiver;
import org.argeo.cms.text.TextStyles;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class EditableImage extends StyledControl {
	private static final long serialVersionUID = -5689145523114022890L;
	private final static Log log = LogFactory.getLog(EditableImage.class);

	private final Node parentNode;
	private String name;

	public EditableImage(Composite parent, int swtStyle, Node parentNode,
			String name) {
		super(parent, swtStyle);
		this.parentNode = parentNode;
		this.name = name;
		setStyle(TextStyles.TEXT_IMAGE);
	}

	public EditableImage(Composite parent, int swtStyle, Node parentNode) {
		this(parent, swtStyle, parentNode, null);
	}

	@Override
	protected Control createControl(Composite box, String style) {
		if (isEditing()) {
			return createImageChooser(box, style);
		} else {
			return createLabel(box, style);
		}
	}

	protected Label createLabel(Composite box, String style) {
		Label lbl = new Label(box, getStyle() | SWT.WRAP);
		lbl.setLayoutData(CmsUtils.fillWidth());
		lbl.setData(CmsConstants.MARKUP, true);
		lbl.setData(STYLE, style);
		String imgTag;
		Node fileNode = null;
		try {
			if (parentNode.hasNode(name)) {
				fileNode = parentNode.getNode(name);
				CmsImageManager imageManager = CmsSession.current.get()
						.getImageManager();
				imgTag = imageManager.getImageTag(fileNode);
			} else {
				imgTag = CmsUtils.noImg(NO_IMAGE_SIZE);
			}
		} catch (Exception e) {
			log.error("Cannot retrieve image " + fileNode, e);
			imgTag = CmsUtils.noImg(NO_IMAGE_SIZE);
			// throw new CmsException("Cannot retrieve image", e);
		}
		lbl.setText(imgTag);
		getParent().layout();
		if (mouseListener != null)
			lbl.addMouseListener(mouseListener);
		return lbl;
	}

	public void displayNoImage(Point size) {
		// getDisplay().asyncExec(new Runnable() {

		// @Override
		// public void run() {
		Label lbl = (Label) getControl();
		Control[] arr = { lbl };
		// getParent().layout(arr);
		// Point size = new Point(width, 0);
		// if (landscape)
		// size.y = NO_IMAGE_SIZE.y * width / NO_IMAGE_SIZE.x;
		// else
		// size.y = NO_IMAGE_SIZE.x * width / NO_IMAGE_SIZE.y;
		setSize(size);
		String imgTag = CmsUtils.noImg(size);
		// ResourceManager rm = RWT.getResourceManager();
		// String imgTag = CmsUtils.img(rm.getLocation(NO_IMAGE), "100%",
		// "100%");
		lbl.setText(imgTag);
		getParent().layout(arr);
		// }
		// });
	}

	protected Control createImageChooser(Composite box, String style) {
		final FileUploadHandler uploadHandler = prepareUpload(parentNode, name);
		final ServerPushSession pushSession = new ServerPushSession();
		final FileUpload fileUpload = new FileUpload(box, SWT.NONE);
		CmsUtils.style(fileUpload, style);
		fileUpload.setText("Upload...");
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

	protected static FileUploadHandler prepareUpload(Node parentNode,
			String name) {
		JcrFileUploadReceiver receiver = new JcrFileUploadReceiver(parentNode,
				name);
		final FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
		uploadHandler.addUploadListener(new FileUploadListener() {

			public void uploadProgress(FileUploadEvent event) {
				// handle upload progress
			}

			public void uploadFailed(FileUploadEvent event) {
				throw new CmsException("Upload failed " + event, event
						.getException());
			}

			public void uploadFinished(FileUploadEvent event) {
				for (FileDetails file : event.getFileDetails()) {
					if (log.isDebugEnabled())
						log.debug("Received: " + file.getFileName());
				}
				uploadHandler.dispose();
			}
		});
		return uploadHandler;
	}

}
