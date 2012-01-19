package org.argeo.connect.ui.gps.editors;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.commons.ModifiedFieldListener;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/** Manages all metadata corresponding to the current clean data session */
public class SessionMetaDataPage extends AbstractCleanDataEditorPage {
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);

	// local variables
	public final static String ID = "cleanDataEditor.metaDataPage";

	private HashMap<String, String> repos;
	// Current page widgets
	private Text sessionDisplayName;
	private Text sessionDescription;
	private Text defaultSensorName;
	private Combo localRepoCombo;

	private FormToolkit tk;

	// parameter table
	// private TableViewer paramsTableViewer;
	// private List<Node> paramNodeList;

	public SessionMetaDataPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		tk = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.getBody().setLayout(new TableWrapLayout());

		createFields(form.getBody());
	}

	private Section createFields(Composite parent) {
		GridData gd;

		// clean session metadata
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectUiGpsPlugin
				.getGPSMessage(ConnectGpsLabels.METADATA_SECTION_TITLE));
		Composite body = tk.createComposite(section, SWT.WRAP);
		section.setClient(body);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 2;
		body.setLayout(layout);

		// Session display name
		Label label = tk.createLabel(body, ConnectUiGpsPlugin
				.getGPSMessage(ConnectGpsLabels.PARAM_SET_LABEL_LBL));
		sessionDisplayName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.grabExcessHorizontalSpace = true;
		sessionDisplayName.setLayoutData(gd);
		if (getJcrStringValue(Property.JCR_TITLE) != null)
			sessionDisplayName.setText(getJcrStringValue(Property.JCR_TITLE));
		else
			try {
				sessionDisplayName.setText(getEditor().getCurrentSessionNode()
						.getName());
			} catch (RepositoryException re) {
			} // Silent

		// Session description
		label = tk.createLabel(body, ConnectUiGpsPlugin
				.getGPSMessage(ConnectGpsLabels.PARAM_SET_COMMENTS_LBL));
		sessionDescription = new Text(body, SWT.FILL | SWT.BORDER
				| SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 60;
		sessionDescription.setLayoutData(gd);
		if (getJcrStringValue(Property.JCR_DESCRIPTION) != null)
			sessionDescription
					.setText(getJcrStringValue(Property.JCR_DESCRIPTION));

		// Default Sensor name
		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin
				.getGPSMessage(ConnectGpsLabels.DEFAULT_SENSOR_NAME_LBL));
		defaultSensorName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		defaultSensorName.setLayoutData(gd);
		if (getJcrStringValue(ConnectNames.CONNECT_DEFAULT_SENSOR) != null)
			defaultSensorName
					.setText(getJcrStringValue(ConnectNames.CONNECT_DEFAULT_SENSOR));
		else
			try {
				defaultSensorName.setText(getEditor().getCurrentSessionNode()
						.getSession().getUserID());
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Unexpected error while getting user name", re);
			}

		// Local repository name
		label = new Label(body, SWT.NONE);
		label.setText("Choose a local repository.");
		localRepoCombo = new Combo(body, SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		localRepoCombo.setLayoutData(gd);

		populateRepoCombo(localRepoCombo);

		if (getJcrStringValue(ConnectNames.CONNECT_DEFAULT_SENSOR) != null)
			defaultSensorName
					.setText(getJcrStringValue(ConnectNames.CONNECT_DEFAULT_SENSOR));
		else
			try {
				defaultSensorName.setText(getEditor().getCurrentSessionNode()
						.getSession().getUserID());
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Unexpected error while getting user name", re);
			}

		AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				if (onSave)
					try {
						Node currentSessionNode = getEditor()
								.getCurrentSessionNode();
						currentSessionNode.setProperty(Property.JCR_TITLE,
								sessionDisplayName.getText());
						currentSessionNode.setProperty(
								Property.JCR_DESCRIPTION,
								sessionDescription.getText());
						currentSessionNode.setProperty(
								ConnectNames.CONNECT_DEFAULT_SENSOR,
								defaultSensorName.getText());
						// TODO enhance that. works because repo list is usually
						// small
						String tmpStr = localRepoCombo.getItem(localRepoCombo
								.getSelectionIndex());
						if (tmpStr != null)
							for (String key : repos.keySet()) {
								if (tmpStr.equals(repos.get(key))) {
									currentSessionNode
											.setProperty(
													ConnectNames.CONNECT_LOCAL_REPO_NAME,
													key);
									break;
								}
							}
						currentSessionNode.setProperty(
								ConnectNames.CONNECT_DEFAULT_SENSOR,
								defaultSensorName.getText());
						super.commit(onSave);
					} catch (RepositoryException re) {
						throw new ArgeoException(
								"Error while trying to persist Meta Data for Session",
								re);
					}
				else if (log.isDebugEnabled())
					log.debug("commit(false)");
			}
		};

		sessionDisplayName.addModifyListener(new ModifiedFieldListener(part));
		sessionDescription.addModifyListener(new ModifiedFieldListener(part));
		defaultSensorName.addModifyListener(new ModifiedFieldListener(part));
		localRepoCombo.addSelectionListener(new ComboListener(part));

		getManagedForm().addPart(part);
		return section;
	}

	// Fill with existing values :
	private String getJcrStringValue(String jcrPropertyName) {
		String value = null;
		try {
			Node curNode = getEditor().getCurrentSessionNode();
			if (curNode.hasProperty(jcrPropertyName))
				value = curNode.getProperty(jcrPropertyName).getString();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting persisted value for property"
							+ jcrPropertyName, re);
		}
		return value;
	}

	// fill the combo
	private void populateRepoCombo(Combo combo) {
		repos = new HashMap<String, String>();
		try {
			Node parNode = getEditor()
					.getTrackDao()
					.getLocalRepositoriesParentNode(getEditor().getJcrSession());
			NodeIterator ni = parNode.getNodes();
			while (ni.hasNext()) {
				Node curNode = ni.nextNode();
				if (curNode.isNodeType(ConnectTypes.CONNECT_LOCAL_REPOSITORY)) {
					repos.put(curNode.getName(),
							curNode.getProperty(Property.JCR_TITLE).getString());
				}
			}
			combo.removeAll();
			for (String key : repos.keySet()) {
				combo.add(repos.get(key));
			}

			// Initialize to persisted repo name
			Node sessionNode = getEditor().getCurrentSessionNode();
			if (sessionNode.hasProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)) {

				String tmp = sessionNode
						.getProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
						.getString().trim();

				if (repos.containsKey(tmp)) {
					combo.select(combo.indexOf(repos.get(tmp)));
				} else
					ErrorFeedback
							.show("Session is linked to a absent local repository");
			}
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error wile populating comboo list", re);
		}
	}

	/**
	 * returns the default sensor name or null if none or an empty string has
	 * been entered
	 */
	public String getDefaultSensorName() {
		String name = defaultSensorName.getText();

		if (name == null || "".equals(name))
			return null;
		else
			return name;
	}

	// Inner classes
	private class ComboListener implements SelectionListener {
		private AbstractFormPart part;

		public ComboListener(AbstractFormPart part) {
			this.part = part;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			part.markDirty();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Do nothing
		}

	}
}
