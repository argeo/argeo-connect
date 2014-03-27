package org.argeo.connect.people.ui.editors;

import java.util.List;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.query.RowIterator;

import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.composites.EntityTableComposite;
import org.argeo.connect.people.ui.composites.PersonTableComposite;
import org.argeo.connect.people.ui.editors.utils.SearchEntityEditorInput;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.listeners.PeopleJcrViewerDClickListener;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Search repository with a given entity type
 */
public class StaticSearchEntityEditor extends EditorPart implements
		ITableProvider {

	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".staticSearchEntityEditor";

	/* DEPENDENCY INJECTION */
	private Session session;
	private PeopleUiService peopleUiService;

	// This page widgets
	private ITableProvider currTableProvider;

	// Business Objects
	private String entityType;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		SearchEntityEditorInput sei = (SearchEntityEditorInput) getEditorInput();
		entityType = sei.getName();
	}

	@Override
	public void dispose() {
		JcrUtils.logoutQuietly(session);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		// parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		parent.setLayout(new GridLayout());

		Composite table = null;
		TableViewer viewer = null;

		if (PeopleTypes.PEOPLE_PERSON.equals(entityType)) {
			PersonTableComposite tmpCmp = new PersonTableComposite(parent,
					SWT.MULTI, session);
			currTableProvider = tmpCmp;
			viewer = tmpCmp.getTableViewer();
			table = tmpCmp;
		} else {
			EntityTableComposite tmpCmp = new EntityTableComposite(parent,
					SWT.MULTI, session, entityType, null, true, false);
			viewer = tmpCmp.getTableViewer();
			table = tmpCmp;
		}
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
				entityType, peopleUiService));
	}

	/**
	 * Overwrite to set the correct row height
	 * 
	 */
	protected int getCurrRowHeight() {
		return 20;
	}

	@Override
	public void setFocus() {
	}

	// Exposes to children classes
	protected String getCurrNodeType() {
		return entityType;
	}

	// Configure calc extracts
	@Override
	public RowIterator getRowIterator(String extractId) {
		if (currTableProvider != null)
			return currTableProvider.getRowIterator(extractId);

		return null;
	}

	@Override
	public List<ColumnDefinition> getColumnDefinition(String extractId) {
		if (currTableProvider != null)
			return currTableProvider.getColumnDefinition(extractId);
		return null;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		session = CommonsJcrUtils.login(repository);
	}

	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}

}