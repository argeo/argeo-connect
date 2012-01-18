package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

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

public class MetaDataPage extends AbstractCleanDataEditorPage {
	// private final static Log log = LogFactory
	// .getLog(DefineParamsAndReviewPage.class);

	// local variables
	public final static String ID = "cleanDataEditor.metaDataPage";

	// Current page widgets
	private Text paramSetLabel;
	private Text paramSetComments;
	private Text defaultSensorName;

	// parameter table
	// private TableViewer paramsTableViewer;
	// private List<Node> paramNodeList;

	public MetaDataPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		try {
			ScrolledForm form = managedForm.getForm();

			TableWrapLayout twt = new TableWrapLayout();
			TableWrapData twd = new TableWrapData(SWT.FILL);
			twd.grabHorizontal = true;
			form.getBody().setLayout(twt);
			form.getBody().setLayoutData(twd);

			createFields(form.getBody());
			// createParamTable(form.getBody());

		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (getJcrStringValue(CONNECT_NAME) == null)
			try {
				paramSetLabel.setText(getEditor().getCurrentSessionNode()
						.getName());
			} catch (RepositoryException re) {// Silent
			}
		else
			paramSetLabel.setText(getJcrStringValue(CONNECT_NAME));

		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(PARAM_SET_COMMENTS_LBL));
		paramSetComments = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetComments.setLayoutData(gd);
		if (getJcrStringValue(CONNECT_COMMENTS) != null)
			paramSetComments.setText(getJcrStringValue(CONNECT_COMMENTS));

		// Default Sensor name
		label = new Label(body, SWT.NONE);
		label.setText(ConnectUiGpsPlugin.getGPSMessage(DEFAULT_SENSOR_NAME_LBL));
		defaultSensorName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		defaultSensorName.setLayoutData(gd);
		if (getJcrStringValue(CONNECT_COMMENTS) != null)
			defaultSensorName
					.setText(getJcrStringValue(CONNECT_DEFAULT_SENSOR));

		AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				// implements here what must be done while committing and saving
				// (if onSave = true)

				Node currentSessionNode = getEditor().getCurrentSessionNode();
				try {
					currentSessionNode.setProperty(CONNECT_NAME,
							paramSetLabel.getText());
					currentSessionNode.setProperty(CONNECT_COMMENTS,
							paramSetComments.getText());
					currentSessionNode.setProperty(CONNECT_DEFAULT_SENSOR,
							defaultSensorName.getText());
					super.commit(onSave);

					// Only to get record of the pattern:
					// // We inform param page that the model has changed.
					// IManagedForm imf = getEditor().findPage(
					// DefineParamsAndReviewPage.ID).getManagedForm();
					// if (imf != null)
					// ((AbstractFormPart) imf.getParts()[0]).markStale();
					// // refresh() method of the corresponding abstract form
					// // part must then be implemented to have the required
					// // behaviour.

				} catch (RepositoryException re) {
					throw new ArgeoException(
							"Error while trying to persist Meta Data for Session",
							re);
				}

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
