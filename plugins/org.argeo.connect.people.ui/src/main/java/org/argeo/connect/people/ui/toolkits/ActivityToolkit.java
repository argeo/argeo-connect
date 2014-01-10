package org.argeo.connect.people.ui.toolkits;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.composites.ActivityTableComposite;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of commonly used people activity controls (typically
 * Text and composite widget) to be used in various forms.
 */
public class ActivityToolkit {
	private final static Log log = LogFactory.getLog(ActivityToolkit.class);

	private final FormToolkit toolkit;
	@SuppressWarnings("unused")
	private final IManagedForm form;
	@SuppressWarnings("unused")
	private final PeopleService peopleService;

	public ActivityToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
	}

	public void populateActivityLogPanel(final Composite parent,
			final Node entity, final String openEditorCmdId) {
		parent.setLayout(new GridLayout()); // .gridLayoutNoBorder());
		try {

			final Button addBtn = toolkit.createButton(parent, "Add activity",
					SWT.PUSH);
			// The Table
			Composite table = null;
			ActivityTableComposite tmpCmp = new ActivityTableComposite(parent,
					SWT.MULTI, entity.getSession());
			// TableViewer viewer = tmpCmp.getTableViewer();
			table = tmpCmp;
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			configureAddActivityButton(addBtn, entity, "Add a new activity",
					entity);
		} catch (RepositoryException re) {
			throw new PeopleException("unable to create activity log", re);
		}
	}

	// ///////////////////////
	// HELPERS
	private Node createActivity(Session session, Node relatedEntity) {
		try {
			String currentUser = session.getUserID();
			Node userProfile = getUserById(session, currentUser);
			Calendar currentTime = GregorianCalendar.getInstance();
			String path = PeopleConstants.PEOPLE_ACTIVITIES_BASE_PATH + "/"
					+ JcrUtils.dateAsPath(currentTime, true) + currentUser;
			Node parent = JcrUtils.mkdirs(session, path);
			Node activity = parent.addNode(PeopleTypes.PEOPLE_NOTE,
					PeopleTypes.PEOPLE_ACTIVITY);
			activity.addMixin(PeopleTypes.PEOPLE_NOTE);
			log.debug("Created activity: " + activity.getPath());

			// updateCreated(activity);
			activity.setProperty(PeopleNames.PEOPLE_MANAGER, userProfile);

			// related to
			ValueFactory vFactory = session.getValueFactory();
			Value val = vFactory.createValue(relatedEntity.getIdentifier(),
					PropertyType.REFERENCE);
			Value[] related = new Value[1];
			related[0] = val;
			activity.setProperty(PeopleNames.PEOPLE_RELATED_TO, related);

			// Content
			activity.setProperty(Property.JCR_TITLE, "A title");
			activity.setProperty(Property.JCR_DESCRIPTION,
					"A quite short dummy description");
			JcrUtils.updateLastModified(activity);
			activity.getSession().save();
			return activity;
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create activity node", e);
		}
	}

	private Node getUserById(Session session, String userId)
			throws RepositoryException {
		String currentUser = session.getUserID();

		String path = "/argeo:system/argeo:people/"
				+ JcrUtils.firstCharsToPath(currentUser, 2) + "/" + currentUser
				+ "/argeo:profile";
		log.debug(path);
		Node userProfile = null;
		userProfile = session.getNode(path);
		return userProfile;
	}

	private void configureAddActivityButton(Button button,
			final Node targetNode, String tooltip, final Node entity) {
		button.setToolTipText(tooltip);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

		button.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					createActivity(entity.getSession(), entity);
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Unable to create activity for entity " + entity,
							re);
				}
			}
		});
	}
}