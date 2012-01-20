package org.argeo.connect.ui.gps.editors;

import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class LocalRepoViewerPage extends FormPage {
	// private final static Log log =
	// LogFactory.getLog(LocalRepoViewerPage.class);
	public final static String ID = "localRepoEditor.localRepoViewerPage";

	private MapControlCreator mapControlCreator;
	private MapViewer mapViewer;

	public LocalRepoViewerPage(FormEditor editor, String title,
			MapControlCreator mapControlCreator) {
		super(editor, ID, title);
		this.mapControlCreator = mapControlCreator;

	}

	public LocalRepoEditor getEditor() {
		return (LocalRepoEditor) super.getEditor();
	}

	private String getReferential() {
		return getEditor().getEditorInput().getName();
	}

	protected void createFormContent(IManagedForm managedForm) {
		// Initialize current form
		ScrolledForm form = managedForm.getForm();
		Composite body = form.getBody();
		body.setLayout(new GridLayout(1, true));

		createMapPart(body);
		try {
			addCleanDataLayer(getReferential());
		} catch (Exception e) {
			ErrorFeedback.show("Cannot load data layer", e);
		}
	}

	protected void createMapPart(Composite parent) {
		Composite mapArea = getManagedForm().getToolkit().createComposite(
				parent, SWT.BORDER);
		mapArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FillLayout layout = new FillLayout();
		mapArea.setLayout(layout);
		mapViewer = mapControlCreator.createMapControl(getEditor()
				.getCurrentRepoNode(), mapArea);
		getEditor().addBaseLayers(mapViewer);
	}

	/*
	 * GIS
	 */
	protected void addCleanDataLayer(String referential) {
		// TODO implement this method
	}
}
