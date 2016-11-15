package org.argeo.eclipse.ui.files;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.argeo.cms.maintenance.MaintenanceStyles;
import org.argeo.cms.util.CmsUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class SimpleFsBrowser {

	// Some local constants to experiment. should be cleaned
	private final static int THUMBNAIL_WIDTH = 400;
	private final static int COLUMN_WIDTH = 160;

	// Keep a cache of the opened directories
	// Key is the path
	private LinkedHashMap<Path, FilterEntitiesVirtualTable> browserCols = new LinkedHashMap<>();
	private Composite pathDisplayParent;
	private Composite colViewer;
	private ScrolledComposite scrolledCmp;
	private Text parentPathTxt;
	private Text filterTxt;
	private Path currEdited;

	private Path initialPath;

	public Control createUi(Composite parent, Path context) {
		if (context == null)
			// return null;
			throw new IllegalArgumentException("Context cannot be null");
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		// TODO use a sash
		// Left
		Composite leftCmp = new Composite(parent, SWT.NO_FOCUS);
		leftCmp.setLayoutData(EclipseUiUtils.fillAll());
		createBrowserPart(leftCmp, context);

		// Right
		pathDisplayParent = new Composite(parent, SWT.NO_FOCUS | SWT.BORDER);
		GridData gd = new GridData(SWT.RIGHT, SWT.FILL, false, true);
		gd.widthHint = THUMBNAIL_WIDTH;
		pathDisplayParent.setLayoutData(gd);
		createPathView(pathDisplayParent, context);

		// INIT
		setEdited(context);
		initialPath = context;

		// Workaround we don't yet manage the delete to display parent of the
		// initial context node

		return null;
	}

	private void createBrowserPart(Composite parent, Path context) {
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		parent.setLayout(layout);
		Composite filterCmp = new Composite(parent, SWT.NO_FOCUS);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());

		// top filter
		addFilterPanel(filterCmp);

		// scrolled composite
		scrolledCmp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.BORDER | SWT.NO_FOCUS);
		scrolledCmp.setLayoutData(EclipseUiUtils.fillAll());
		scrolledCmp.setExpandVertical(true);
		scrolledCmp.setExpandHorizontal(true);
		scrolledCmp.setShowFocusedControl(true);

		colViewer = new Composite(scrolledCmp, SWT.NO_FOCUS);
		scrolledCmp.setContent(colViewer);
		scrolledCmp.addControlListener(new ControlAdapter() {
			private static final long serialVersionUID = 45632468266199163L;

			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolledCmp.getClientArea();
				scrolledCmp.setMinSize(colViewer.computeSize(SWT.DEFAULT, r.height));
			}
		});
		initExplorer(colViewer, context);
	}

	private Control initExplorer(Composite parent, Path context) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		createBrowserColumn(parent, context);
		return null;
	}

	private Control createBrowserColumn(Composite parent, Path context) {
		// TODO style is not correctly managed.
		FilterEntitiesVirtualTable table = new FilterEntitiesVirtualTable(parent, SWT.BORDER | SWT.NO_FOCUS, context);
		// CmsUtils.style(table, ArgeoOrgStyle.browserColumn.style());
		table.filterList("*");
		table.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		browserCols.put(context, table);
		return null;
	}

	public void addFilterPanel(Composite parent) {

		parent.setLayout(EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false)));

		// Text Area for the filter
		parentPathTxt = new Text(parent, SWT.NO_FOCUS);
		parentPathTxt.setEditable(false);
		filterTxt = new Text(parent, SWT.SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage("Filter current list");
		filterTxt.setLayoutData(EclipseUiUtils.fillWidth());
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1L;

			public void modifyText(ModifyEvent event) {
				modifyFilter(false);
			}
		});

		filterTxt.addKeyListener(new KeyListener() {
			private static final long serialVersionUID = 2533535233583035527L;

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
				// boolean altPressed = (e.stateMask & SWT.ALT) != 0;
				FilterEntitiesVirtualTable currTable = null;
				if (currEdited != null) {
					FilterEntitiesVirtualTable table = browserCols.get(currEdited);
					if (table != null && !table.isDisposed())
						currTable = table;
				}

				// try {
				if (e.keyCode == SWT.ARROW_DOWN)
					currTable.setFocus();
				else if (e.keyCode == SWT.BS) {
					if (filterTxt.getText().equals("")
							&& !(currEdited.getNameCount() == 1 || currEdited.equals(initialPath))) {
						setEdited(currEdited.getParent());
						e.doit = false;
						filterTxt.setFocus();
					}
				} else if (e.keyCode == SWT.TAB && !shiftPressed) {
					Path uniqueChild = getOnlyChild(currEdited, filterTxt.getText());
					if (uniqueChild != null)
						setEdited(uniqueChild);
					filterTxt.setFocus();
					e.doit = false;
				}
				// } catch (RepositoryException e1) {
				// throw new CmsException("Unexpected error in key management
				// for " + currEdited + "with filter "
				// + filterTxt.getText(), e1);
				// }

			}
		});
	}

	private Path getOnlyChild(Path parent, String filter) {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(currEdited, filter + "*")) {
			Path uniqueChild = null;
			boolean moreThanOne = false;
			loop: for (Path entry : stream) {
				if (uniqueChild == null) {
					uniqueChild = entry;
				} else {
					moreThanOne = true;
					break loop;
				}
			}
			if (!moreThanOne)
				return uniqueChild;
			return null;
		} catch (IOException ioe) {
			throw new FilesException(
					"Unable to determine unique child existence and get it under " + parent + " with filter " + filter,
					ioe);
		}
	}

	private void setEdited(Path path) {
		currEdited = path;
		CmsUtils.clear(pathDisplayParent);
		createPathView(pathDisplayParent, currEdited);
		pathDisplayParent.layout();
		refreshFilters(path);
		refreshBrowser(path);
	}

	private void refreshFilters(Path path) {
		parentPathTxt.setText(path.toUri().toString());
		filterTxt.setText("");
		filterTxt.getParent().layout();
	}

	private void refreshBrowser(Path currPath) {

		// Retrieve
		Path currParPath = currPath.getParent();

		Object[][] colMatrix = new Object[browserCols.size()][2];

		int i = 0, j = -1, k = -1;
		for (Path path : browserCols.keySet()) {
			colMatrix[i][0] = path;
			colMatrix[i][1] = browserCols.get(path);
			if (j >= 0 && k < 0 && currParPath != null) {
				boolean leaveOpened = path.startsWith(currPath);

				// // workaround for same name siblings
				// // fix me weird side effect when we go left or click on anb
				// // already selected, unfocused node
				// if (leaveOpened && (path.lastIndexOf("/") == 0 &&
				// currNodePath.lastIndexOf("/") == 0
				// ||
				// JcrUtils.parentPath(path).equals(JcrUtils.parentPath(currNodePath))))
				// leaveOpened =
				// JcrUtils.lastPathElement(path).equals(JcrUtils.lastPathElement(currNodePath));

				if (!leaveOpened)
					k = i;
			}
			if (currParPath.equals(path))
				j = i;
			i++;
		}

		if (j >= 0 && k >= 0)
			// remove useless cols
			for (int l = i - 1; l >= k; l--) {
			browserCols.remove(colMatrix[l][0]);
			((FilterEntitiesVirtualTable) colMatrix[l][1]).dispose();
			}

		// Remove disposed columns
		// TODO investigate and fix the mechanism that leave them there after
		// disposal
		if (browserCols.containsKey(currPath)) {
			FilterEntitiesVirtualTable currCol = browserCols.get(currPath);
			if (currCol.isDisposed())
				browserCols.remove(currPath);
		}

		if (!browserCols.containsKey(currPath))
			createBrowserColumn(colViewer, currPath);

		colViewer.setLayout(EclipseUiUtils.noSpaceGridLayout(new GridLayout(browserCols.size(), false)));
		// colViewer.pack();
		colViewer.layout();
		// also resize the scrolled composite
		scrolledCmp.layout();
		scrolledCmp.getShowFocusedControl();
		// colViewer.getParent().layout();
		// if (JcrUtils.parentPath(currNodePath).equals(currBrowserKey)) {
		// } else {
		// }
	}

	private void modifyFilter(boolean fromOutside) {
		if (!fromOutside)
			if (currEdited != null) {
				String filter = filterTxt.getText() + "*";
				FilterEntitiesVirtualTable table = browserCols.get(currEdited);
				if (table != null && !table.isDisposed())
					table.filterList(filter);
			}
	}

	// private String getPath(Node node) {
	// try {
	// return node.getPath();
	// } catch (RepositoryException e) {
	// throw new CmsException("Unable to get path for node " + node, e);
	// }
	// }

	private Point imageWidth = new Point(250, 0);

	/**
	 * Recreates the content of the box that displays information about the
	 * current selected node.
	 */
	private Control createPathView(Composite parent, Path context) {

		parent.setLayout(new GridLayout(2, false));

		// if (isImg(context)) {
		// EditableImage image = new Img(parent, RIGHT, context, imageWidth);
		// image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false,
		// 2, 1));
		// }

		// Name and primary type
		Label contextL = new Label(parent, SWT.NONE);
		CmsUtils.markup(contextL);
		contextL.setText("<b>" + context.getFileName() + "</b>");
		// new Label(parent,
		// SWT.NONE).setText(context.getPrimaryNodeType().getName());

		// Children
		// for (NodeIterator nIt = context.getNodes(); nIt.hasNext();) {
		// Node child = nIt.nextNode();
		// new CmsLink(child.getName(), BROWSE_PREFIX +
		// child.getPath()).createUi(parent, context);
		// new Label(parent,
		// SWT.NONE).setText(child.getPrimaryNodeType().getName());
		// }

		// Properties
		// for (PropertyIterator pIt = context.getProperties(); pIt.hasNext();)
		// {
		// Property property = pIt.nextProperty();
		// Label label = new Label(parent, SWT.NONE);
		// label.setText(property.getName());
		// label.setToolTipText(JcrUtils.getPropertyDefinitionAsString(property));
		// new Label(parent, SWT.NONE).setText(getPropAsString(property));
		// }

		return null;
	}

	// private boolean isImg(Node node) throws RepositoryException {
	// return node.hasNode(JCR_CONTENT) && node.isNodeType(CmsTypes.CMS_IMAGE);
	// }

	// private String getPropAsString(Property property) throws
	// RepositoryException {
	// String result = "";
	// if (property.isMultiple()) {
	// result = getMultiAsString(property, ", ");
	// } else {
	// Value value = property.getValue();
	// if (value.getType() == PropertyType.BINARY)
	// result = "<binary>";
	// else if (value.getType() == PropertyType.DATE)
	// result = timeFormatter.format(value.getDate().getTime());
	// else
	// result = value.getString();
	// }
	// return result;
	// }
	//
	// private String getMultiAsString(Property property, String separator)
	// throws RepositoryException {
	// if (separator == null)
	// separator = "; ";
	// Value[] values = property.getValues();
	// StringBuilder builder = new StringBuilder();
	// for (Value val : values) {
	// String currStr = val.getString();
	// if (!"".equals(currStr.trim()))
	// builder.append(currStr).append(separator);
	// }
	// if (builder.lastIndexOf(separator) >= 0)
	// return builder.substring(0, builder.length() - separator.length());
	// else
	// return builder.toString();
	// }

	/** Almost canonical implementation of a table that display entities */
	private class FilterEntitiesVirtualTable extends Composite {
		private static final long serialVersionUID = 2223410043691844875L;

		// Context
		private Path context;

		// UI Objects
		private TableViewer entityViewer;

		// enable management of multiple columns
		// Path getPath() {
		// return context;
		// }

		@Override
		public boolean setFocus() {
			if (entityViewer.getTable().isDisposed())
				return false;
			if (entityViewer.getSelection().isEmpty()) {
				Object first = entityViewer.getElementAt(0);
				if (first != null) {
					entityViewer.setSelection(new StructuredSelection(first), true);
				}
			}
			return entityViewer.getTable().setFocus();
		}

		void filterList(String filter) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(context)) {
				refreshFilteredList(stream);
			} catch (IOException | DirectoryIteratorException e) {
				throw new FilesException("Unable to filter " + context + " children with filter " + filter, e);
			}

			// try {
			// FIXME list children path
			// NodeIterator nit = context.getNodes(filter);
			// } catch (RepositoryException e) {
			// throw new CmsException("Unable to filter " + getNode() + "
			// children with filter " + filter, e);
			// }

		}

		public FilterEntitiesVirtualTable(Composite parent, int style, Path context) {
			super(parent, SWT.NO_FOCUS);
			this.context = context;
			populate();
		}

		protected void populate() {
			Composite parent = this;
			GridLayout layout = EclipseUiUtils.noSpaceGridLayout();

			this.setLayout(layout);
			createTableViewer(parent);
		}

		private void createTableViewer(final Composite parent) {
			// the list
			// We must limit the size of the table otherwise the full list is
			// loaded
			// before the layout happens
			Composite listCmp = new Composite(parent, SWT.NO_FOCUS);
			GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, true);
			gd.widthHint = COLUMN_WIDTH;
			listCmp.setLayoutData(gd);
			listCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());

			entityViewer = new TableViewer(listCmp, SWT.VIRTUAL | SWT.SINGLE);
			Table table = entityViewer.getTable();

			table.setLayoutData(EclipseUiUtils.fillAll());
			table.setLinesVisible(true);
			table.setHeaderVisible(false);
			CmsUtils.markup(table);
			CmsUtils.style(table, MaintenanceStyles.BROWSER_COLUMN);

			// first column
			TableViewerColumn column = new TableViewerColumn(entityViewer, SWT.NONE);
			TableColumn tcol = column.getColumn();
			tcol.setWidth(COLUMN_WIDTH);
			tcol.setResizable(true);
			column.setLabelProvider(new SimpleNameLP());

			entityViewer.setContentProvider(new MyLazyCP(entityViewer));
			entityViewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) entityViewer.getSelection();
					if (selection.isEmpty())
						return;
					else
						setEdited((Path) selection.getFirstElement());

				}
			});

			table.addKeyListener(new KeyListener() {
				private static final long serialVersionUID = -8083424284436715709L;

				@Override
				public void keyReleased(KeyEvent e) {
				}

				@Override
				public void keyPressed(KeyEvent e) {

					IStructuredSelection selection = (IStructuredSelection) entityViewer.getSelection();
					Path selected = null;
					if (!selection.isEmpty())
						selected = ((Path) selection.getFirstElement());
					// try {
					if (e.keyCode == SWT.ARROW_RIGHT) {
						if (selected != null) {
							setEdited(selected);
							browserCols.get(selected).setFocus();
						}
					} else if (e.keyCode == SWT.ARROW_LEFT) {
						Path parent = selected.getParent();
						;
						if (parent == null)
							return;

						selected = parent;

						setEdited(selected);
						if (browserCols.containsKey(selected))
							browserCols.get(selected).setFocus();
					}
					// } catch (RepositoryException ie) {
					// throw new CmsException("Error while managing arrow " +
					// "events in the browser for " + selected,
					// ie);
					// }
				}
			});
		}

		private class MyLazyCP implements ILazyContentProvider {
			private static final long serialVersionUID = 9096550041395433128L;
			private TableViewer viewer;
			private Object[] elements;

			public MyLazyCP(TableViewer viewer) {
				this.viewer = viewer;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// IMPORTANT: don't forget this: an exception will be thrown if
				// a selected object is not part of the results anymore.
				viewer.setSelection(null);
				this.elements = (Object[]) newInput;
			}

			public void updateElement(int index) {
				viewer.replace(elements[index], index);
			}
		}

		protected void refreshFilteredList(DirectoryStream<Path> children) throws IOException {
			// TODO make this lazy
			List<Path> paths = new ArrayList<>();
			for (Path entry : children) {
				paths.add(entry);
			}
			Object[] rows = paths.toArray(new Object[0]);
			entityViewer.setInput(rows);
			entityViewer.setItemCount(rows.length);
			entityViewer.refresh();
		}

		public class SimpleNameLP extends ColumnLabelProvider {
			private static final long serialVersionUID = 8187902187946523148L;

			@Override
			public String getText(Object element) {
				if (element instanceof Path) {
					Path curr = ((Path) element);
					return curr.getFileName().toString();
				}
				return super.getText(element);
			}
		}
	}
}
