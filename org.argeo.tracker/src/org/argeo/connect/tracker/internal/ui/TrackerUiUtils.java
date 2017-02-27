package org.argeo.connect.tracker.internal.ui;

import java.util.List;

import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/** Centralise useful methods to ease implementation of the UI */
public class TrackerUiUtils {
	
	public static TableViewer createTableViewer(final Composite parent, int tableStyle,
			List<ColumnDefinition> columnDefs) {

		int style = tableStyle | SWT.H_SCROLL | SWT.V_SCROLL;
		Table table = new Table(parent, style);
		TableColumnLayout layout = new TableColumnLayout();

		// TODO the table layout does not works with the scrolled form
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = new TableViewer(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		for (ColumnDefinition colDef : columnDefs)
			createTableColumn(viewer, layout, colDef);

		viewer.setContentProvider(new IStructuredContentProvider() {
			private static final long serialVersionUID = -3133493667354601923L;

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				viewer.refresh();
			}

			@Override
			public void dispose() {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return (Object[]) inputElement;
			}
		});
		return viewer;
	}

	/** Default creation of a column for a survey answer table */
	public static TableViewerColumn createTableColumn(TableViewer tableViewer, TableColumnLayout layout,
			ColumnDefinition columnDef) {

		boolean resizable = true;
		TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn column = tvc.getColumn();

		column.setText(columnDef.getLabel());
		column.setWidth(columnDef.getMinWidth());
		column.setResizable(resizable);

		ColumnLabelProvider lp = columnDef.getLabelProvider();
		tvc.setLabelProvider(lp);
		layout.setColumnData(column, new ColumnWeightData(columnDef.getWeight(), columnDef.getMinWidth(), resizable));
		return tvc;
	}

	public static Label createFormBoldLabel(FormToolkit toolkit, Composite parent, String value) {
		// We add a blank space before to workaround the cropping of the word
		// first letter in some OS/Browsers (typically MAC/Firefox 31 )
		Label label = toolkit.createLabel(parent, " " + value, SWT.END);
		label.setFont(EclipseUiUtils.getBoldFont(parent));
		label.setLayoutData(new TableWrapData(TableWrapData.RIGHT));
		return label;
	}

	
	/** Appends a section with a title in a table wrap layout */
	public static Section addFormSection(FormToolkit tk, Composite parent, String title) {
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setText(title);
		Composite body = tk.createComposite(section, SWT.NO_FOCUS);
		section.setClient(body);
		return section;
	}

	/** Appends an expendable section with a title */
	public static Section addFormSection(final IManagedForm form, FormToolkit tk, Composite parent, String title,
			boolean isExpended) {
		int style = Section.TITLE_BAR | Section.TWISTIE;
		if (isExpended)
			style |= Section.EXPANDED;
		Section section = tk.createSection(parent, style);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		section.setText(title);
		Composite body = tk.createComposite(section, SWT.NO_FOCUS);
		section.setClient(body);
		return section;
	}

	public static ToolBarManager addMenu(Section section) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		section.setTextClient(toolbar);
		final Cursor handCursor = new Cursor(section.getDisplay(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		toolbar.addDisposeListener(new DisposeListener() {
			private static final long serialVersionUID = 6093858538389954404L;

			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});
		return toolBarManager;
	}

	// EASILY CREATE WIDGETS
	/** Creates a simple label / value pair with no content for the value */
	public static Label createLL(FormToolkit toolkit, Composite body, String label) {
		return createLL(toolkit, body, label, "", 1);
	}

	/** Creates a simple label / value pair. */
	public static Label createLL(FormToolkit toolkit, Composite body, String label, String value) {
		return createLL(toolkit, body, label, value, 1);
	}

	/** Creates a simple label / value pair. */
	public static Label createLL(FormToolkit toolkit, Composite body, String label, String value, int colSpan) {
		Label lbl = toolkit.createLabel(body, label);
		lbl.setFont(EclipseUiUtils.getBoldFont(body));
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		Label valuelbl = toolkit.createLabel(body, value, SWT.WRAP);
		valuelbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, colSpan, 1));
		return valuelbl;
	}

	/** Creates label and multiline text. */
	public static Text createLMT(FormToolkit toolkit, Composite body, String label, String value) {
		Label lbl = toolkit.createLabel(body, label);
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text text = toolkit.createText(body, value, SWT.BORDER | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		return text;
	}

	/** Creates label and text. */
	public static Text createLT(FormToolkit toolkit, Composite body, String label, String value) {
		Label lbl = toolkit.createLabel(body, label);
		lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Text text = toolkit.createText(body, value, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return text;
	}
}