package org.argeo.connect.people.rap.monitoring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.editors.util.AbstractPeopleBasicEditor;
import org.argeo.connect.people.rap.providers.SimpleLazyContentProvider;
import org.argeo.connect.people.rap.util.Refreshable;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.argeo.jcr.JcrMonitor;
import org.argeo.security.ui.PrivilegedJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Quick and dirty editor to monitor the number of instances for each known node
 * type
 * 
 * TODO Work on this to enrich the generic monitoring perspective.
 */
public class NodeTypeList extends AbstractPeopleBasicEditor implements
		Refreshable {

	/* DEPENDENCY INJECTION */
	private PeopleService peopleService;

	// Configuration
	protected NumberFormat numberFormat;
	protected DateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy 'at' HH:mm");
	protected DateFormat isoFormat = new SimpleDateFormat("yyyy-dd-MM_HH-mm");
	protected final String SEPARATOR = ",";
	protected final String DELIMITER = "\"";
	protected final String CR = "\n";
	private final static String NODE_DEF_PARENT_PATH = "/jcr:system/jcr:nodeTypes";

	private final String relPath = "/nodeInstances";
	private final String[] headers = { "Name", "Nb of occurences" };
	private final String prefix = "instanceNb";

	// This page widget
	private TableViewer tableViewer;
	private SimpleLazyContentProvider lazyCp;
	private NameLP nameLP = new NameLP();
	private CountLP countLP = new CountLP();

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		setPartName("Node types");
		setTitleToolTip("Display the list of used node types and corresponding occurence number");

		numberFormat = DecimalFormat.getInstance();
		((DecimalFormat) numberFormat).applyPattern("#,##0");
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		// The table itself
		Composite tableCmp = new Composite(parent, SWT.NO_FOCUS);
		tableViewer = createTableViewer(tableCmp, SWT.READ_ONLY);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());
		// tableViewer.addDoubleClickListener(new PeopleJcrViewerDClickListener(
		// tagInstanceType, peopleWorkbenchService));
	}

	private TableViewer createTableViewer(final Composite parent, int tableStyle) {
		parent.setLayout(new GridLayout());

		int swtStyle = tableStyle | SWT.VIRTUAL;
		Table table = new Table(parent, swtStyle);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = new TableViewer(table);
		TableViewerColumn column;
		column = ViewerUtils.createTableViewerColumn(viewer, "Name", SWT.NONE,
				300);
		column.setLabelProvider(new SimpleLP(0));
		column = ViewerUtils.createTableViewerColumn(viewer, "Number",
				SWT.RIGHT, 150);
		column.setLabelProvider(new SimpleLP(1));

		lazyCp = new SimpleLazyContentProvider(viewer);
		viewer.setContentProvider(lazyCp);
		return viewer;
	}

	@Override
	public void forceRefresh(Object object) {
		new QueryJob().schedule();
	}

	/** Privileged job that performs the query asynchronously */
	private class QueryJob extends PrivilegedJob {
		private Table table;

		public QueryJob() {
			super("AsynchronousQuery");
			tableViewer = getTableViewer();
			table = getTableViewer().getTable();
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
			if (monitor != null && !monitor.isCanceled())
				monitor.beginTask("Retrieving defined node types", -1);

			Session session = getSession();
			OutputStream outputStream = null;
			try {
				if (session.nodeExists(NODE_DEF_PARENT_PATH)) {
					Node parent = session.getNode(NODE_DEF_PARENT_PATH);
					NodeIterator nit = parent.getNodes();

					if (nit.hasNext()) {
						// Enable progress bar
						if (monitor != null && !monitor.isCanceled())
							monitor.beginTask("Computing instances number for",
									(int) nit.getSize());

						// Create log file
						outputStream = new FileOutputStream(createLogFile(
								relPath, prefix));
						writeLine(outputStream, headers);
					}

					final List<String[]> infos = new ArrayList<String[]>();
					while (nit.hasNext()) {
						Node currDef = nit.nextNode();
						if (currDef.isNodeType(NodeType.NT_NODE_TYPE)) {
							String name = nameLP.getText(currDef);
							// Enable progress bar
							if (monitor != null && !monitor.isCanceled())
								monitor.subTask(name);

							String[] vals = new String[2];
							vals[0] = name;
							vals[1] = countLP.getText(currDef);
							infos.add(vals);
							writeLine(outputStream, vals);
							monitor.worked(1);
						}
					}

					outputStream.flush();
					outputStream.close();

					if (table.isDisposed())
						return Status.OK_STATUS;

					table.getDisplay().asyncExec(new Runnable() {
						public void run() {
							Object[] input = null;
							if (!infos.isEmpty())
								input = infos.toArray(new String[1][2]);
							lazyCp.setElements(input);
						}
					});
				}
			} catch (RepositoryException e) {
				throw new PeopleException(
						"Unable to refresh node type list table", e);
			} catch (IOException e) {
				throw new PeopleException(
						"Unable write to log file with prefix " + prefix, e);
			} finally {
				IOUtils.closeQuietly(outputStream);
			}
			return Status.OK_STATUS;
		}
	}

	// Exposes
	protected PeopleService getPeopleService() {
		return peopleService;
	}

	// PROVIDERS
	private class SimpleLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4896540744722267118L;
		private final int colIndex;

		public SimpleLP(int colIndex) {
			this.colIndex = colIndex;
		}

		@Override
		public String getText(Object element) {
			return ((String[]) element)[colIndex];
		}
	}

	protected class NameLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -8179051587196328000L;

		@Override
		public String getText(Object element) {
			try {
				return ((Node) element).getName();
			} catch (RepositoryException e) {
				throw new PeopleException("Unable to retrieve name " + "for "
						+ element, e);
			}
		}
	}

	protected class CountLP extends ColumnLabelProvider {
		private static final long serialVersionUID = -4022534826825314784L;

		@Override
		public String getText(Object element) {
			try {
				String currType = ((Node) element).getName();
				Query query = getSession()
						.getWorkspace()
						.getQueryManager()
						.createQuery(
								"select * from [" + currType + "] as instances",
								Query.JCR_SQL2);
				NodeIterator nit = query.execute().getNodes();
				// return numberFormat.format(nit.getSize());
				return "" + nit.getSize();
			} catch (RepositoryException re) {
				throw new PeopleException("Unable to retrieve number of "
						+ "instances for type " + element, re);
			}
		}
	}

	protected TableViewer getTableViewer() {
		return tableViewer;
	}

	protected void writeLine(OutputStream output, String[] values)
			throws IOException {
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			builder.append(DELIMITER + cleanValue(value) + DELIMITER).append(
					SEPARATOR);
		}
		String header = builder.toString().substring(0, builder.length() - 1);
		header += CR;
		output.write(header.getBytes());
	}

	protected String cleanValue(String value) {
		return value.replace("\"", "\\\"");
	}

	protected void writeValue(OutputStream output, String value)
			throws IOException {
		byte[] contentInBytes = cleanValue(value).getBytes();
		output.write(contentInBytes);
	}

	protected File createLogFile(String relPath, String filePrefix)
			throws IOException {
		File file = null;
		String path = getPeopleService().getMaintenanceService()
				.getMonitoringLogFolderPath() + relPath;
		File parentFolder = new File(path);
		FileUtils.forceMkdir(parentFolder);
		String nowIso = isoFormat.format(new GregorianCalendar().getTime());
		file = new File(path + "/" + filePrefix + "_" + nowIso + ".csv");
		file.createNewFile();
		return file;
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

}