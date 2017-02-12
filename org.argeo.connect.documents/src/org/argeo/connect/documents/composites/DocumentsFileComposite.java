package org.argeo.connect.documents.composites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.documents.DocumentsException;
import org.argeo.connect.documents.DocumentsService;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.fs.FsUiUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Default Documents file composite: a sashForm with a browser in the middle and
 * meta data at right hand side.
 */
public class DocumentsFileComposite extends Composite {
	private static final long serialVersionUID = -7567632342889241793L;

	private final static Log log = LogFactory.getLog(DocumentsFileComposite.class);

	private final Node currentBaseContext;

	// UI Parts for the browser
	private Composite rightPannelCmp;

	public DocumentsFileComposite(Composite parent, int style, Node context, DocumentsService documentsService,
			FileSystemProvider fsp) {
		super(parent, style);
		this.currentBaseContext = context;
		this.setLayout(EclipseUiUtils.noSpaceGridLayout());
		SashForm form = new SashForm(this, SWT.HORIZONTAL);

		Composite centerCmp = new Composite(form, SWT.BORDER | SWT.NO_FOCUS);
		createDisplay(centerCmp);

		rightPannelCmp = new Composite(form, SWT.NO_FOCUS);

		Path path = documentsService.getPath(fsp, ConnectJcrUtils.getPath(context));
		setOverviewInput(path);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		form.setWeights(new int[] { 55, 20 });
	}

	private void createDisplay(final Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		Browser browser = new Browser(parent, SWT.NONE);
		// browser.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
		// true));
		browser.setLayoutData(EclipseUiUtils.fillAll());
		try {
			String url = CmsUtils.getDataUrl(currentBaseContext, RWT.getRequest());
			// FIXME issue with the redirection to https
			if (url.startsWith("http://") && !url.startsWith("http://localhost"))
				url = "https://" + url.substring("http://".length(), url.length());
			if (log.isTraceEnabled())
				log.debug("Trying to display " + url);
			browser.setUrl(url);
			browser.layout(true, true);
		} catch (RepositoryException re) {
			throw new DocumentsException("Cannot open file at " + currentBaseContext, re);
		}
	}

	/**
	 * Recreates the content of the box that displays information about the
	 * current selected Path.
	 */
	private void setOverviewInput(Path path) {
		try {
			EclipseUiUtils.clear(rightPannelCmp);
			rightPannelCmp.setLayout(new GridLayout());
			if (path != null) {
				// if (isImg(context)) {
				// EditableImage image = new Img(parent, RIGHT, context,
				// imageWidth);
				// image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				// true, false,
				// 2, 1));
				// }

				Label contextL = new Label(rightPannelCmp, SWT.NONE);
				contextL.setText(path.getFileName().toString());
				contextL.setFont(EclipseUiUtils.getBoldFont(rightPannelCmp));
				addProperty(rightPannelCmp, "Last modified", Files.getLastModifiedTime(path).toString());
				// addProperty(rightPannelCmp, "Owner",
				// Files.getOwner(path).getName());
				if (Files.isDirectory(path)) {
					addProperty(rightPannelCmp, "Type", "Folder");
				} else {
					String mimeType = Files.probeContentType(path);
					if (EclipseUiUtils.isEmpty(mimeType))
						mimeType = "<i>Unknown</i>";
					addProperty(rightPannelCmp, "Type", mimeType);
					addProperty(rightPannelCmp, "Size", FsUiUtils.humanReadableByteCount(Files.size(path), false));
				}
			}
			rightPannelCmp.layout(true, true);
		} catch (IOException e) {
			throw new DocumentsException("Cannot display details for " + path.toString(), e);
		}
	}

	// Simplify UI implementation
	private void addProperty(Composite parent, String propName, String value) {
		Label propLbl = new Label(parent, SWT.NONE);
		propLbl.setText(ConnectUiUtils.replaceAmpersand(propName + ": " + value));
		CmsUtils.markup(propLbl);
	}
}
