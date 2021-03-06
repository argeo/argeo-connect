package org.argeo.people.web.parts;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.PeopleJcrUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Manages button for a people:contact node */
public class ContactButtonsPart implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new RowLayout(SWT.HORIZONTAL));

		createCategoryButton(parent, context);

		configurePrimaryButton(createPrimaryButton(parent, context));

		// Deletion if needed
		// configureDeleteButton(createDeleteButton(buttCmp));

		return parent;
	}

	private Button createCategoryButton(Composite parent, Node context) throws RepositoryException {
		Button btn = new Button(parent, SWT.NONE);
		btn.setLayoutData(CmsUiUtils.rowData16px());
		btn.setData(RWT.CUSTOM_VARIANT, getCssStyle(context));
		return btn;
	}

	private String getCssStyle(Node entity) throws RepositoryException {

		Node contactable = entity.getParent().getParent();
		boolean isPro = ConnectJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_CONTACT_REF);
		// EMAIL
		if (entity.isNodeType(PeopleTypes.PEOPLE_MAIL)) {
			return "people_icon_email";
		}
		// PHONE
		else if (entity.isNodeType(PeopleTypes.PEOPLE_PHONE))
			return "people_icon_phone";
		else if (entity.isNodeType(PeopleTypes.PEOPLE_MOBILE))
			return "people_icon_mobile";
		else if (entity.isNodeType(PeopleTypes.PEOPLE_FAX))
			return "people_icon_fax";		
		// ADDRESS
		else if (entity.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS)) {
			if (contactable.isNodeType(PeopleTypes.PEOPLE_PERSON )&& isPro)
					return "people_icon_work";
			return "people_icon_address";
		}
		// URL
		else if (entity.isNodeType(PeopleTypes.PEOPLE_URL)) {
			// return ContactImages.PRIVATE_HOME_PAGE;
			return "people_icon_url";
		}
		// SOCIAL MEDIA
		else if (entity.isNodeType(PeopleTypes.PEOPLE_SOCIAL_MEDIA)) {
			String category = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			if (ContactValueCatalogs.CONTACT_CAT_GOOGLEPLUS.equals(category))
				return "people_icon_google_plus";
			else if (ContactValueCatalogs.CONTACT_CAT_FACEBOOK.equals(category))
				return "people_icon_facebook";
			else if (ContactValueCatalogs.CONTACT_CAT_LINKEDIN.equals(category))
				return "people_icon_linkedin";
			else if (ContactValueCatalogs.CONTACT_CAT_TWITTER.equals(category))
				return "people_icon_twitter";
			else if (ContactValueCatalogs.CONTACT_CAT_XING.equals(category))
				return "people_icon_xing";
			return "people_icon_social_media";
		}
		// // IMPP
		else if (entity.isNodeType(PeopleTypes.PEOPLE_IMPP)) {
			return "people_icon_impp";
		}
		return null;
	}

	@SuppressWarnings("unused")
	private Button createDeleteButton(Composite parent, Node context) {
		Button btn = new Button(parent, SWT.NONE);
		btn.setLayoutData(CmsUiUtils.rowData16px());
		btn.setData(RWT.CUSTOM_VARIANT, "cms_icon_delete");
		return btn;
	}

	private Button createPrimaryButton(Composite parent, Node context) throws RepositoryException {
		Button btn = new Button(parent, SWT.NONE);
		btn.setLayoutData(CmsUiUtils.rowData16px());
		// update image
		boolean isPrimary = PeopleJcrUtils.isPrimary(context.getParent().getParent(), context);

		if (isPrimary)
			btn.setData(RWT.CUSTOM_VARIANT, "people_icon_primary");
		else
			btn.setData(RWT.CUSTOM_VARIANT, "people_icon_not_primary");
		return btn;
	}

	@SuppressWarnings("unused")
	private void configureDeleteButton(final Button btn) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				// TODO implement this
				// try {
				// // update primary cache
				// if (PeopleJcrUtils.isPrimary(parentVersionableNode,
				// contactNode))
				// PeopleJcrUtils.updatePrimaryCache(peopleService,
				// parentVersionableNode, contactNode, false);
				//
				// contactNode.remove();
				// } catch (RepositoryException e) {
				// throw new PeopleException("unable to initialise deletion",
				// e);
				// }
			}
		});
	}

	private void configurePrimaryButton(final Button btn) {
		btn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				// TODO implement this
				// boolean hasChanged = PeopleJcrUtils.markAsPrimary(
				// peopleService, parentVersionableNode, contactNode);

			}
		});
	}
}
