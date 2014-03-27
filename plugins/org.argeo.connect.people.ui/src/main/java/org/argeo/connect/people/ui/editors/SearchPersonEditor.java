package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.composites.PeopleVirtualTableViewer;
import org.argeo.connect.people.ui.editors.utils.SearchEntityEditorInput;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.extracts.PeopleColumnDefinition;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrRowLabelProvider;
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
public class SearchPersonEditor extends EditorPart implements PeopleNames{
	// implements ITableProvider 
	
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".searchPersonEditor";

	/* DEPENDENCY INJECTION */
	private Session session;
	private String openEntityEditorCmdId = OpenEntityEditor.ID;

	// This page widgets
	private ITableProvider currTableProvider;
	private TableViewer tableViewer;

	// Business Objects
	private String entityType;

	private List<PeopleColumnDefinition> colDefs = new ArrayList<PeopleColumnDefinition>();
	{
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				Property.JCR_TITLE, PropertyType.STRING, "Display Name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						Property.JCR_TITLE), 300));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PEOPLE_LAST_NAME, PropertyType.STRING, "Last name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						PEOPLE_LAST_NAME), 120));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PEOPLE_FIRST_NAME, PropertyType.STRING, "First name",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						PEOPLE_FIRST_NAME), 120));
		colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_PERSON,
				PEOPLE_TAGS, PropertyType.STRING, "Tags",
				new SimpleJcrRowLabelProvider(PeopleTypes.PEOPLE_PERSON,
						PEOPLE_TAGS), 200));
	};

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		SearchEntityEditorInput sei = (SearchEntityEditorInput) getEditorInput();
		entityType = sei.getName();
	}

	/**
	 * Overwrite to provide a plugin specific open editor command and thus be
	 * able to open plugin specific editors
	 */
	protected String getOpenEntityEditorCmdId() {
		return openEntityEditorCmdId;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		PeopleVirtualTableViewer tableCmp = new PeopleVirtualTableViewer(
				parent, SWT.MULTI, colDefs);
		tableViewer = tableCmp.getTableViewer();
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
//				entityType, getPeop));

		listFilteredElements(null);
	}

	/**
	 * Build repository request : caller might overwrite in order to display a
	 * subset
	 */
	protected void listFilteredElements(String filter) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector source = factory.selector(PeopleTypes.PEOPLE_PERSON,
					PeopleTypes.PEOPLE_PERSON);

			Constraint defaultC = null;
			// Build constraints based the textArea filter content
			if (filter != null && !"".equals(filter.trim())) {
				// Parse the String
				String[] strs = filter.trim().split(" ");
				for (String token : strs) {
					StaticOperand so = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}

			Ordering order = factory.ascending(factory.propertyValue(
					source.getSelectorName(), Property.JCR_TITLE));
			Ordering[] orderings = { order };

			QueryObjectModel query = factory.createQuery(source, defaultC,
					orderings, null);

			QueryResult result = query.execute();
			RowIterator rit = result.getRows();

			List<Row> rows = new ArrayList<Row>();
			while (rit.hasNext()) {
				rows.add(rit.nextRow());
			}

			tableViewer.setInput(rows.toArray(new Row[rows.size()]));

			// we must explicitly set the items count
			tableViewer.setItemCount(rows.size());

		} catch (RepositoryException re) {
			throw new PeopleException("Unable to list rows for filter "
					+ filter, re);
		}
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

	// // Configure calc extractsgetOpenEntityEditorCmdId()
	// @Override
	// public RowIterator getRowIterator(String extractId) {
	// if (currTableProvider != null)
	// return currTableProvider.getRowIterator(extractId);
	//
	// return null;
	// }
	//
	// @Override
	// public void dispose() {
	// JcrUtils.logoutQuietly(session);
	// super.dispose();
	// }
	//
	// @Override
	// public List<ColumnDefinition> getColumnDefinition(String extractId) {
	// if (currTableProvider != null)
	// return currTableProvider.getColumnDefinition(extractId);
	// return null;
	// }

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

	public void setOpenEntityEditorCmdId(String openEntityEditorCmdId) {
		this.openEntityEditorCmdId = openEntityEditorCmdId;
	}
}