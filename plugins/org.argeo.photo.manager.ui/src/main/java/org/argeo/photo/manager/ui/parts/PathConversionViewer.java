package org.argeo.photo.manager.ui.parts;

import org.argeo.photo.manager.PathConverterResult;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class PathConversionViewer {
	private TableViewer viewer;

	private PathConverterResult result;

	private ScrolledComposite scroll;

	public PathConversionViewer(Composite parent) {
		// Scroll
		scroll = new ScrolledComposite(parent, SWT.BORDER);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinSize(600, 400);

		viewer = new TableViewer(scroll, SWT.MULTI | SWT.V_SCROLL);
		Table table = viewer.getTable();
		scroll.setContent(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn colFrom = new TableColumn(table, SWT.LEFT);
		colFrom.setText("From");
		colFrom.setWidth(300);
		TableColumn colTo = new TableColumn(table, SWT.LEFT);
		colTo.setText("To");
		colTo.setWidth(300);

		// updateFromUi();

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		// viewer.setInput(getViewSite());

	}
	
	public void setLayoutData(Object layoutData){
		scroll.setLayoutData(layoutData);
	}

	public void setPathConverterResult(PathConverterResult result) {
		viewer.setInput(result);
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			result = (PathConverterResult) newInput;
		}

		public void dispose() {
		}

		public Object[] getElements(Object inputElement) {

			if (result == null || result.getPaths().size() == 0)
				return new Object[] { "Nothing to display" };
			else
				return result.getPaths().toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			String fromPath = obj.toString();
			if (index == 0) {
				return fromPath;
			} else if (index == 1) {
				if (result == null)
					return null;

				String text = result.getMapping().get(fromPath);
				if (text == null)
					text = result.getConversionErrors().get(fromPath)
							.getMessage();
				return text;

			} else {
				return null;
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

	}

}
