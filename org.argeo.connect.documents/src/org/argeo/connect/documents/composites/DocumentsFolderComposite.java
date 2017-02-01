package org.argeo.connect.documents.composites;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.argeo.cms.ui.fs.FsStyles;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.documents.DocumentsException;
import org.argeo.connect.documents.DocumentsService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.ColumnDefinition;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.fs.FileIconNameLabelProvider;
import org.argeo.eclipse.ui.fs.FsTableViewer;
import org.argeo.eclipse.ui.fs.FsUiConstants;
import org.argeo.eclipse.ui.fs.FsUiUtils;
import org.argeo.eclipse.ui.fs.NioFileLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 * Default Documents folder composite: a sashForm layout with a simple table in
 * the middle and an overview at right hand side.
 */
public class DocumentsFolderComposite extends Composite {
	// private final static Log log = LogFactory.getLog(CmsFsBrowser.class);
	private static final long serialVersionUID = -40347919096946585L;

	private final Node currentBaseContext;

	private final DocumentsService documentService;

	// UI Parts for the browser
	private Composite filterCmp;
	private Text filterTxt;
	private FsTableViewer directoryDisplayViewer;
	private Composite rightPannelCmp;

	private DocumentsContextMenu contextMenu;

	// Local context (this composite is statefull)
	private Path initialPath;
	private Path currDisplayedFolder;

	public DocumentsFolderComposite(Composite parent, int style, Node context, DocumentsService documentService) {
		super(parent, style);
		this.documentService = documentService;
		this.currentBaseContext = context;

		this.setLayout(EclipseUiUtils.noSpaceGridLayout());

		SashForm form = new SashForm(this, SWT.HORIZONTAL);

		Composite centerCmp = new Composite(form, SWT.BORDER | SWT.NO_FOCUS);
		createDisplay(centerCmp);

		rightPannelCmp = new Composite(form, SWT.NO_FOCUS);

		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		form.setWeights(new int[] { 55, 20 });
	}

	public void populate(Path path) {
		initialPath = path;
		setInput(path);
	}

	void refresh() {
		modifyFilter(false);
	}

	private void createDisplay(final Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// top filter
		filterCmp = new Composite(parent, SWT.NO_FOCUS);
		filterCmp.setLayoutData(EclipseUiUtils.fillWidth());
		addFilterPanel(filterCmp);

		// Main display
		directoryDisplayViewer = new FsTableViewer(parent, SWT.MULTI);
		List<ColumnDefinition> colDefs = new ArrayList<>();
		colDefs.add(new ColumnDefinition(new FileIconNameLabelProvider(), " Name", 250));
		colDefs.add(new ColumnDefinition(new NioFileLabelProvider(FsUiConstants.PROPERTY_SIZE), "Size", 100));
		colDefs.add(new ColumnDefinition(new NioFileLabelProvider(FsUiConstants.PROPERTY_TYPE), "Type", 150));
		colDefs.add(new ColumnDefinition(new NioFileLabelProvider(FsUiConstants.PROPERTY_LAST_MODIFIED),
				"Last modified", 400));
		final Table table = directoryDisplayViewer.configureDefaultTable(colDefs);
		table.setLayoutData(EclipseUiUtils.fillAll());

		directoryDisplayViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) directoryDisplayViewer.getSelection();
				Path selected = null;
				if (selection.isEmpty())
					setSelected(null);
				else
					selected = ((Path) selection.getFirstElement());
				if (selected != null) {
					// TODO manage multiple selection
					setSelected(selected);
				}
			}
		});

		directoryDisplayViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) directoryDisplayViewer.getSelection();
				Path selected = null;
				if (!selection.isEmpty())
					selected = ((Path) selection.getFirstElement());
				if (selected != null) {
					if (Files.isDirectory(selected))
						setInput(selected);
					else
						externalNavigateTo(selected);
				}
			}
		});

		// The context menu
		contextMenu = new DocumentsContextMenu(this, documentService,
				ConnectJcrUtils.getRepository(currentBaseContext));

		table.addMouseListener(new MouseAdapter() {
			private static final long serialVersionUID = 6737579410648595940L;

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					// contextMenu.setCurrFolderPath(currDisplayedFolder);
					contextMenu.show(table, new Point(e.x, e.y), currDisplayedFolder);
				}
			}
		});
	}

	/**
	 * Overwrite to enable single soucing between workbench and CMS navigation
	 */
	protected void externalNavigateTo(Path path) {

	}

	private void addPathElementBtn(Path path) {
		Button elemBtn = new Button(filterCmp, SWT.PUSH);
		String nameStr;
		if (path.toString().equals("/"))
			nameStr = "[jcr:root]";
		else
			nameStr = path.getFileName().toString();
		elemBtn.setText(nameStr + " >> ");
		CmsUtils.style(elemBtn, FsStyles.BREAD_CRUMB_BTN);
		elemBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -4103695476023480651L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				setInput(path);
			}
		});
	}

	public void setInput(Path path) {
		if (path.equals(currDisplayedFolder))
			return;
		currDisplayedFolder = path;

		Path diff = initialPath.relativize(currDisplayedFolder);

		for (Control child : filterCmp.getChildren())
			if (!child.equals(filterTxt))
				child.dispose();

		addPathElementBtn(initialPath);
		Path currTarget = initialPath;
		if (!diff.toString().equals(""))
			for (Path pathElem : diff) {
				currTarget = currTarget.resolve(pathElem);
				addPathElementBtn(currTarget);
			}

		filterTxt.setText("");
		filterTxt.moveBelow(null);
		setSelected(null);
		filterCmp.getParent().layout(true, true);
	}

	private void setSelected(Path path) {
		if (path == null)
			setOverviewInput(currDisplayedFolder);
		else
			setOverviewInput(path);
	}

	public Viewer getViewer() {
		return directoryDisplayViewer;
	}

	/**
	 * Recreates the content of the box that displays information about the
	 * current selected Path.
	 */
	private void setOverviewInput(Path path) {
		try {
			EclipseUiUtils.clear(rightPannelCmp);
			rightPannelCmp.setLayout(new GridLayout());
			if (path != null) {
				// if (isImg(context)) {
				// EditableImage image = new Img(parent, RIGHT, context,
				// imageWidth);
				// image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				// true, false,
				// 2, 1));
				// }

				Label contextL = new Label(rightPannelCmp, SWT.NONE);
				contextL.setText(path.getFileName().toString());
				contextL.setFont(EclipseUiUtils.getBoldFont(rightPannelCmp));
				addProperty(rightPannelCmp, "Last modified", Files.getLastModifiedTime(path).toString());
				// addProperty(rightPannelCmp, "Owner",
				// Files.getOwner(path).getName());
				if (Files.isDirectory(path)) {
					addProperty(rightPannelCmp, "Type", "Folder");
				} else {
					String mimeType = Files.probeContentType(path);
					if (EclipseUiUtils.isEmpty(mimeType))
						mimeType = "<i>Unknown</i>";
					addProperty(rightPannelCmp, "Type", mimeType);
					addProperty(rightPannelCmp, "Size", FsUiUtils.humanReadableByteCount(Files.size(path), false));
				}
			}
			rightPannelCmp.layout(true, true);
		} catch (IOException e) {
			throw new DocumentsException("Cannot display details for " + path.toString(), e);
		}
	}

	private void addFilterPanel(Composite parent) {
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.center = true;
		parent.setLayout(rl);
		// parent.setLayout(EclipseUiUtils.noSpaceGridLayout(new GridLayout(2,
		// false)));

		filterTxt = new Text(parent, SWT.SEARCH | SWT.ICON_CANCEL);
		filterTxt.setMessage("Search current folder");
		filterTxt.setLayoutData(new RowData(250, SWT.DEFAULT));
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
				// boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
				// // boolean altPressed = (e.stateMask & SWT.ALT) != 0;
				// FilterEntitiesVirtualTable currTable = null;
				// if (currEdited != null) {
				// FilterEntitiesVirtualTable table =
				// browserCols.get(currEdited);
				// if (table != null && !table.isDisposed())
				// currTable = table;
				// }
				//
				// if (e.keyCode == SWT.ARROW_DOWN)
				// currTable.setFocus();
				// else if (e.keyCode == SWT.BS) {
				// if (filterTxt.getText().equals("")
				// && !(currEdited.getNameCount() == 1 ||
				// currEdited.equals(initialPath))) {
				// Path oldEdited = currEdited;
				// Path parentPath = currEdited.getParent();
				// setEdited(parentPath);
				// if (browserCols.containsKey(parentPath))
				// browserCols.get(parentPath).setSelected(oldEdited);
				// filterTxt.setFocus();
				// e.doit = false;
				// }
				// } else if (e.keyCode == SWT.TAB && !shiftPressed) {
				// Path uniqueChild = getOnlyChild(currEdited,
				// filterTxt.getText());
				// if (uniqueChild != null) {
				// // Highlight the unique chosen child
				// currTable.setSelected(uniqueChild);
				// setEdited(uniqueChild);
				// }
				// filterTxt.setFocus();
				// e.doit = false;
				// }
			}
		});
	}

	// private Path getOnlyChild(Path parent, String filter) {
	// try (DirectoryStream<Path> stream =
	// Files.newDirectoryStream(currDisplayedFolder, filter + "*")) {
	// Path uniqueChild = null;
	// boolean moreThanOne = false;
	// loop: for (Path entry : stream) {
	// if (uniqueChild == null) {
	// uniqueChild = entry;
	// } else {
	// moreThanOne = true;
	// break loop;
	// }
	// }
	// if (!moreThanOne)
	// return uniqueChild;
	// return null;
	// } catch (IOException ioe) {
	// throw new DocumentsException(
	// "Unable to determine unique child existence and get it under " + parent +
	// " with filter " + filter,
	// ioe);
	// }
	// }

	private void modifyFilter(boolean fromOutside) {
		if (!fromOutside)
			if (currDisplayedFolder != null) {
				String filter = filterTxt.getText() + "*";
				directoryDisplayViewer.setInput(currDisplayedFolder, filter);
			}
	}

	// Simplify UI implementation
	private void addProperty(Composite parent, String propName, String value) {
		Label contextL = new Label(parent, SWT.NONE);
		contextL.setText(propName + ": " + value);
	}
}
