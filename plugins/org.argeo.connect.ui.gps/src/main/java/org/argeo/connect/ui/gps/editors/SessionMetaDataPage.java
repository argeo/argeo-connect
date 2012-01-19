package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.commons.ModifiedFieldListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class SessionMetaDataPage extends AbstractCleanDataEditorPage {
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);

	// local variables
	public final static String ID = "cleanDataEditor.metaDataPage";

	// Current page widgets
	private Text paramSetLabel;
	private Text paramSetComments;
	private Text defaultSensorName;

	// parameter table
	// private TableViewer paramsTableViewer;
	// private List<Node> paramNodeList;

	public SessionMetaDataPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		TableWrapLayout twt = new TableWrapLayout();
		TableWrapData twd = new TableWrapData(SWT.FILL);
		twd.grabHorizontal = true;
		form.getBody().setLayout(twt);
		form.getBody().setLayoutData(twd);
		createFields(form.getBody());
		// ((CleanDataEditor) getEditor()).refreshReadOnlyState();
	}

	private Section createFields(Composite parent) {
		FormToolkit tk = getManagedForm().getToolkit();
		GridData gd;

		// clean session metadata
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectUiGpsPlugin
				.getGPSMessage(METADATA_SECTION_TITLE));
		Composite body = tk.createComposite(section, SWT.WRAP);
		section.setClient(body);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		body.setLayout(layout);

		// Name and comments
		Label label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(PARAM_SET_LABEL_LBL));
		paramSetLabel = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetLabel.setLayoutData(gd);
		if (getJcrStringValue(Property.JCR_TITLE) == null)
			try {
				paramSetLabel.setText(getEditor().getCurrentSessionNode()
						.getName());
			} catch (RepositoryException re) {// Silent
			}
		else
			paramSetLabel.setText(getJcrStringValue(Property.JCR_TITLE));

		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(PARAM_SET_COMMENTS_LBL));
		paramSetComments = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetComments.setLayoutData(gd);
		if (getJcrStringValue(Property.JCR_DESCRIPTION) != null)
			paramSetComments
					.setText(getJcrStringValue(Property.JCR_DESCRIPTION));

		// Default Sensor name
		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(DEFAULT_SENSOR_NAME_LBL));
		defaultSensorName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		defaultSensorName.setLayoutData(gd);
		if (getJcrStringValue(CONNECT_DEFAULT_SENSOR) != null)
			defaultSensorName
					.setText(getJcrStringValue(CONNECT_DEFAULT_SENSOR));
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
								paramSetLabel.getText());
						currentSessionNode.setProperty(
								Property.JCR_DESCRIPTION,
								paramSetComments.getText());
						currentSessionNode.setProperty(CONNECT_DEFAULT_SENSOR,
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

		paramSetLabel.addModifyListener(new ModifiedFieldListener(part));
		paramSetComments.addModifyListener(new ModifiedFieldListener(part));
		defaultSensorName.addModifyListener(new ModifiedFieldListener(part));

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
}
