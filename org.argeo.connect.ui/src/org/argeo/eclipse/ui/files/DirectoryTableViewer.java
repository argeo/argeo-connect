package org.argeo.eclipse.ui.files;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.argeo.cms.maintenance.MaintenanceStyles;
import org.argeo.cms.util.CmsUtils;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Canonical implementation of a JFace table viewer to display the content of a
 * repository
 */
public class DirectoryTableViewer extends TableViewer {
	private static final long serialVersionUID = -5632407542678477234L;

	public DirectoryTableViewer(Composite parent, int style) {
		super(parent, style | SWT.VIRTUAL);
	}

	public Table configureDefaultSingleColumnTable(int tableWidthHint) {
		Table table = this.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		CmsUtils.markup(table);
		CmsUtils.style(table, MaintenanceStyles.BROWSER_COLUMN);

		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		TableColumn tcol = column.getColumn();
		tcol.setWidth(tableWidthHint);
		column.setLabelProvider(new SimpleNameLP());

		this.setContentProvider(new MyLazyCP());
		return table;
	}

	public Table configureDefaultTable(List<ColumnDefinition> columns) {
		this.setContentProvider(new MyLazyCP());
		Table table = this.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		CmsUtils.markup(table);
		CmsUtils.style(table, MaintenanceStyles.BROWSER_COLUMN);

		for (ColumnDefinition colDef : columns) {
			TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
			column.setLabelProvider(colDef.getLabelProvider());
			TableColumn tcol = column.getColumn();
			tcol.setResizable(true);
			tcol.setText(colDef.getLabel());
			tcol.setWidth(colDef.getMinWidth());
		}

		return table;
	}

	public void setInput(Path dir, String filter) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
			// TODO make this lazy
			List<Path> paths = new ArrayList<>();
			for (Path entry : stream) {
				paths.add(entry);
			}
			Object[] rows = paths.toArray(new Object[0]);
			this.setInput(rows);
			this.setItemCount(rows.length);
			this.refresh();
		} catch (IOException | DirectoryIteratorException e) {
			throw new FilesException("Unable to filter " + dir + " children with filter " + filter, e);
		}
	}

	/** Directly displays bookmarks **/
	public void setPathsInput(Path... paths) {
		this.setInput((Object[]) paths);
		this.setItemCount(paths.length);
		this.refresh();
	}

	private class MyLazyCP implements ILazyContentProvider {
		private static final long serialVersionUID = 9096550041395433128L;
		private Object[] elements;

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// IMPORTANT: don't forget this: an exception will be thrown if
			// a selected object is not part of the results anymore.
			viewer.setSelection(null);
			this.elements = (Object[]) newInput;
		}

		public void updateElement(int index) {
			DirectoryTableViewer.this.replace(elements[index], index);
		}
	}

	protected static class SimpleNameLP extends ColumnLabelProvider {
		private static final long serialVersionUID = 8187902187946523148L;

		@Override
		public String getText(Object element) {
			if (element instanceof Path) {
				Path curr = ((Path) element);
				Path name = curr.getFileName();
				if (name == null)
					return "<i> No name </i>";
				else
					return name.toString();
			}
			return null;
		}

		@Override
		public Image getImage(Object element) {
			if (element instanceof Path) {
				Path curr = ((Path) element);
				if (Files.isDirectory(curr))
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
				else
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			} else
				return super.getImage(element);
		}
	}

	protected static ColumnLabelProvider getSimpleNameLp() {
		return new SimpleNameLP();
	}
}
