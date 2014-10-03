package org.argeo.connect.people.web.parts;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.CmsUiProvider;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.argeo.connect.people.web.PeopleWebUtils;
import org.argeo.connect.people.web.providers.PersonOverviewLP;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class PersonHeaderUiProvider implements CmsUiProvider {

	/* dependency injection */
	private PeopleService peopleService;
	private TagLikeValuesUiProvider tagsPart;
	private TagLikeValuesUiProvider mailingListsPart;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		Composite body = new Composite(parent, SWT.NO_FOCUS);

		InputStream is = null;
		Image itemPicture = null;
		// Initialize image
		try {
			if (context.hasNode(PeopleNames.PEOPLE_PICTURE)) {
				Node imageNode = context.getNode(PeopleNames.PEOPLE_PICTURE)
						.getNode(Node.JCR_CONTENT);
				is = imageNode.getProperty(Property.JCR_DATA).getBinary()
						.getStream();
				itemPicture = new Image(body.getShell().getDisplay(), is);
			} else
				itemPicture = null;
		} catch (Exception e) {
		} finally {
			IOUtils.closeQuietly(is);
		}

		if (itemPicture != null) {
			body.setLayout(new GridLayout(2, false));
			Composite imgCmp = new Composite(body, SWT.NO_FOCUS | SWT.NO_SCROLL
					| SWT.NO_TRIM);
			imgCmp.setLayout(PeopleWebUtils.noSpaceGridLayout());
			imgCmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			new ImageLabel(imgCmp, SWT.NO_FOCUS, itemPicture);

			Composite rightCmp = new Composite(body, SWT.NO_FOCUS);
			rightCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			body = rightCmp;
		}
		body.setLayout(new GridLayout());

		final Label readOnlyInfoLbl = new Label(body, SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		ILabelProvider personLP = new PersonOverviewLP(
				PeopleWebConstants.OVERVIEW_TYPE_HEADER, peopleService);
		readOnlyInfoLbl.setText(personLP.getText(context));

		Composite tagsCmp = new Composite(body, SWT.NO_FOCUS);
		tagsCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		tagsCmp.setLayout(PeopleWebUtils.noSpaceGridLayout());
		tagsPart.createUi(tagsCmp, context);

		Composite mlCmp = new Composite(body, SWT.NO_FOCUS);
		mlCmp.setLayoutData(PeopleWebUtils.horizontalFillData());
		mlCmp.setLayout(PeopleWebUtils.noSpaceGridLayout());
		mailingListsPart.createUi(mlCmp, context);

		return body;
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

	public void setTagsPart(TagLikeValuesUiProvider tagsPart) {
		this.tagsPart = tagsPart;
	}

	public void setMailingListsPart(TagLikeValuesUiProvider mailingListsPart) {
		this.mailingListsPart = mailingListsPart;
	}

}