package org.argeo.connect.people.web.pages;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.argeo.connect.people.web.providers.OrgOverviewLP;
import org.argeo.connect.resources.ResourceService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Shows all information we have about a given organization. */
public class OrganizationPage implements CmsUiProvider {

	/* DEPENDENCY INJECTION */
	private ResourceService resourceService;
	private PeopleService peopleService;

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {

		Composite body = new Composite(parent, SWT.NO_FOCUS);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		body.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// header
		Composite headerCmp = new Composite(body, SWT.NO_FOCUS);
		headerCmp.setLayoutData(EclipseUiUtils.fillWidth());
		createHeader(headerCmp, context);

		// mailing lists
		Composite mlCmp = new Composite(body, SWT.NO_FOCUS);
		mlCmp.setLayoutData(EclipseUiUtils.fillWidth());
		createMailingListPanel(mlCmp, context);

		// contacts
		Composite contactCmp = new Composite(body, SWT.NO_FOCUS);
		contactCmp.setLayoutData(EclipseUiUtils.fillWidth());
		createContactPanel(contactCmp, context);

		// activities
		Composite activityCmp = new Composite(body, SWT.NO_FOCUS);
		activityCmp.setLayoutData(EclipseUiUtils.fillWidth());
		createActivityPanel(activityCmp, context);

		parent.layout();
		return body;
	}

	private void createHeader(Composite parent, Node context) {

		InputStream is = null;
		Image itemPicture = null;
		// Initialize image
		try {
			if (context.hasNode(PeopleNames.PEOPLE_PICTURE)) {
				Node imageNode = context.getNode(PeopleNames.PEOPLE_PICTURE).getNode(Node.JCR_CONTENT);
				is = imageNode.getProperty(Property.JCR_DATA).getBinary().getStream();
				itemPicture = new Image(parent.getShell().getDisplay(), is);
			} else
				itemPicture = null;
		} catch (Exception e) {
		} finally {
			IOUtils.closeQuietly(is);
		}

		if (itemPicture != null) {
			parent.setLayout(new GridLayout(2, false));
			Composite imgCmp = new Composite(parent, SWT.NO_FOCUS | SWT.NO_SCROLL | SWT.NO_TRIM);
			imgCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
			imgCmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			new ImageLabel(imgCmp, SWT.NO_FOCUS, itemPicture);

			Composite rightCmp = new Composite(parent, SWT.NO_FOCUS);
			rightCmp.setLayoutData(EclipseUiUtils.fillWidth());
			parent = rightCmp;

		}
		parent.setLayout(new GridLayout());

		final Label readOnlyInfoLbl = new Label(parent, SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		ILabelProvider labelProvider = new OrgOverviewLP(resourceService, peopleService,
				PeopleWebConstants.OVERVIEW_TYPE_HEADER);
		readOnlyInfoLbl.setText(labelProvider.getText(context));
	}

	private void createMailingListPanel(Composite parent, final Node context) throws RepositoryException {
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.spacing = 8;
		parent.setLayout(rl);
		if (context.hasProperty(PeopleNames.PEOPLE_MAILING_LISTS)) {

			Value[] values = context.getProperty(PeopleNames.PEOPLE_MAILING_LISTS).getValues();
			for (Value value : values) {
				final String valueStr = value.getString();
				new Label(parent, SWT.NONE).setText(valueStr);

				Button icon = new Button(parent, SWT.NONE);
				icon.setLayoutData(CmsUtils.rowData16px());
				icon.setData(RWT.CUSTOM_VARIANT, "cms_icon_delete");
				icon.addSelectionListener(new SelectionAdapter() {
					private static final long serialVersionUID = 1L;

					@Override
					public void widgetSelected(SelectionEvent e) {
						ConnectJcrUtils.removeStringFromMultiValuedProp(context, PeopleNames.PEOPLE_MAILING_LISTS,
								valueStr);
						// FIXME won't work: node is checked in
						// TODO refresh this part or the whole body
					}
				});
			}
		}
	}

	private void createContactPanel(Composite parent, Node context) {
	}

	private void createActivityPanel(Composite parent, Node context) {
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
	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
