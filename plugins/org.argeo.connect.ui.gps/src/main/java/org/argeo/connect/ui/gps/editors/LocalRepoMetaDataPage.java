package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
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
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class LocalRepoMetaDataPage extends FormPage {
	// private final static Log log = LogFactory
	// .getLog(DefineParamsAndReviewPage.class);

	// local variables
	public final static String ID = "localRepoEditor.localRepoMetaDataPage";

	// Current page widgets
	private Text displayName;

	public LocalRepoMetaDataPage(FormEditor editor, String title) {
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
	}

	private Section createFields(Composite parent) {
		FormToolkit tk = getManagedForm().getToolkit();
		GridData gd;

		// Local repo metadata
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText("General information about repository '"
				+ getEditor().getEditorInput().getName() + "'.");
		Composite body = tk.createComposite(section, SWT.WRAP);
		section.setClient(body);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		body.setLayout(layout);

		// Name and comments
		Label label = new Label(body, SWT.NONE);
		label.setText("Display name: ");
		displayName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		displayName.setLayoutData(gd);
		displayName.setText(getJcrStringValue(Property.JCR_TITLE));

		AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				if (onSave)
					try {
						Node currentSessionNode = getEditor()
								.getCurrentRepoNode();
						currentSessionNode.setProperty(Property.JCR_TITLE,
								displayName.getText());
						super.commit(onSave);
					} catch (RepositoryException re) {
						throw new ArgeoException(
								"Error while trying to persist Meta Data for local repo",
								re);
					}
			}
		};

		displayName.addModifyListener(new ModifiedFieldListener(part));
		getManagedForm().addPart(part);
		return section;
	}

	// Fill with existing values :
	private String getJcrStringValue(String jcrPropertyName) {
		String value = null;
		try {
			Node curNode = getEditor().getCurrentRepoNode();
			if (curNode.hasProperty(jcrPropertyName))
				value = curNode.getProperty(jcrPropertyName).getString();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting persisted value for property"
							+ jcrPropertyName, re);
		}
		return value;
	}

	public LocalRepoEditor getEditor() {
		return (LocalRepoEditor) super.getEditor();
	}
}
