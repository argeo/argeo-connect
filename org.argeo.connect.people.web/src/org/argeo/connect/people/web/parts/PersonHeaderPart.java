package org.argeo.connect.people.web.parts;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.argeo.connect.people.web.providers.PersonOverviewLP;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Creates header for people:person nodes */
public class PersonHeaderPart implements CmsUiProvider {

	/* dependency injection */
	private PeopleService peopleService;
	private TagLikeValuesPart tagsPart;
	private TagLikeValuesPart mailingListsPart;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {

		InputStream is = null;
		Image itemPicture = null;
		// Initialize image
		try {
			if (context.hasNode(PeopleNames.PEOPLE_PICTURE)) {
				Node imageNode = context.getNode(PeopleNames.PEOPLE_PICTURE)
						.getNode(Node.JCR_CONTENT);
				is = imageNode.getProperty(Property.JCR_DATA).getBinary()
						.getStream();
				itemPicture = new Image(parent.getShell().getDisplay(), is);
			} else
				itemPicture = null;
		} catch (Exception e) {
		} finally {
			IOUtils.closeQuietly(is);
		}

		if (itemPicture != null) {
			parent.setLayout(new GridLayout(2, false));
			Composite imgCmp = new Composite(parent, SWT.NO_FOCUS
					| SWT.NO_SCROLL | SWT.NO_TRIM);
			imgCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			imgCmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			new ImageLabel(imgCmp, SWT.NO_FOCUS, itemPicture);

			Composite rightCmp = new Composite(parent, SWT.NO_FOCUS);
			rightCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			parent = rightCmp;
		}
		parent.setLayout(new GridLayout());

		final Label readOnlyInfoLbl = new Label(parent, SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		ILabelProvider personLP = new PersonOverviewLP(
				PeopleWebConstants.OVERVIEW_TYPE_HEADER, peopleService);
		readOnlyInfoLbl.setText(personLP.getText(context));

		Composite tagsCmp = new Composite(parent, SWT.NO_FOCUS);
		tagsCmp.setLayoutData(EclipseUiUtils.fillWidth());
		tagsCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		tagsPart.createUi(tagsCmp, context);

		Composite mlCmp = new Composite(parent, SWT.NO_FOCUS);
		mlCmp.setLayoutData(EclipseUiUtils.fillWidth());
		mlCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		mailingListsPart.createUi(mlCmp, context);

		return parent;
	}

	/** Will dispose the image on dispose */
	private class ImageLabel extends Label {
		private static final long serialVersionUID = 1L;

		private final Image image;

		private ImageLabel(Composite parent, int style, Image image) {
			super(parent, style);
			this.image = image;
			this.setBackground(parent.getBackground());
			this.setImage(image);
			// gd.horizontalIndent = 5;
			// gd.verticalIndent = 5;
			this.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		}

		@Override
		public void dispose() {
			if (image != null)
				image.dispose();
			super.dispose();
		}

	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	public void setTagsPart(TagLikeValuesPart tagsPart) {
		this.tagsPart = tagsPart;
	}

	public void setMailingListsPart(TagLikeValuesPart mailingListsPart) {
		this.mailingListsPart = mailingListsPart;
	}

}
