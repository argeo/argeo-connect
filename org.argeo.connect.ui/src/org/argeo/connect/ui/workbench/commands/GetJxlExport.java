package org.argeo.connect.ui.workbench.commands;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.query.Row;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.ui.workbench.util.PrivilegedJob;
import org.argeo.connect.ConnectException;
import org.argeo.connect.exports.jxl.NodesToCalcWriter;
import org.argeo.connect.exports.jxl.RowsToCalcWriter;
import org.argeo.connect.ui.ConnectColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;
import org.argeo.connect.ui.IJcrTableViewer;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.ConnectUiPlugin;
import org.argeo.eclipse.ui.EclipseJcrMonitor;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.specific.OpenFile;
import org.argeo.eclipse.ui.utils.SingleSourcingConstants;
import org.argeo.jcr.JcrMonitor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Launch the generation of a spreadsheet displaying nodes or rows values for
 * the current active view or editor. This current active {@link IWorkbenchPart}
 * must implement the {@link IJcrTableViewer} interface.
 */
public class GetJxlExport extends AbstractHandler {
	public final static String ID = AppWorkbenchService.CONNECT_WORKBENCH_ID_PREFIX + ".getJxlExport";
	public final static String PARAM_EXPORT_ID = "param.exportId";

	private final static DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	private final static String URI_FILE_PREFIX = SingleSourcingConstants.FILE_SCHEME
			+ SingleSourcingConstants.SCHEME_HOST_SEPARATOR;

	/* DEPENDENCY INJECTION */
	private AppWorkbenchService appWorkbenchService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String exportId = event.getParameter(PARAM_EXPORT_ID);
		if (EclipseUiUtils.isEmpty(exportId))
			exportId = ConnectUiConstants.DEFAULT_JXL_EXPORT;

		try {
			IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();
			if (iwp instanceof IJcrTableViewer) {
				IJcrTableViewer provider = (IJcrTableViewer) iwp;
				if (provider.getColumnDefinition(exportId) == null)
					return null;
				else {
					Object[] elements = provider.getElements(exportId);
					List<ConnectColumnDefinition> cols = provider.getColumnDefinition(exportId);
					new GenerateExtract(HandlerUtil.getActivePart(event).getSite().getShell().getDisplay(), elements,
							cols, exportId, getFileName(provider)).schedule();
				}
			} else
				throw new ConnectException(iwp.toString() + " is not an instance of "
						+ "ICalcExtractProvider interface." + " Command " + ID + " can be call only when active part "
						+ "implements ICalcExtractProvider interface.");
		} catch (Exception e) {
			throw new ConnectException("Cannot create and populate spreadsheet", e);
		}
		return null;
	}

	/** Override to provide other naming strategy */
	protected String getFileName(IJcrTableViewer provider) {
		String prefix = "PeopleExport-";
		String dateVal = df.format(new GregorianCalendar().getTime());
		return prefix + dateVal + ".xls";
	}

	/** Real call to spreadsheet generator. */
	protected synchronized void callJxlEngine(Object[] elements, List<ConnectColumnDefinition> cols, String exportId,
			File file) throws Exception {
		if (elements instanceof Row[]) {
			RowsToCalcWriter writer = new RowsToCalcWriter();
			writer.writeTableFromRows(file, (Row[]) elements, cols);
		} else if (elements instanceof Node[]) {
			NodesToCalcWriter writer = new NodesToCalcWriter();
			writer.writeTableFromNodes(file, (Node[]) elements, cols);
		}
	}

	/** Privileged job that performs the extract asynchronously */
	private class GenerateExtract extends PrivilegedJob {

		private Display display;
		private Object[] elements;
		private List<ConnectColumnDefinition> cols;

		private String exportId;
		private String targetFileName;

		public GenerateExtract(Display display, Object[] elements, List<ConnectColumnDefinition> cols, String exportId,
				String targetFileName) {
			super("Generating the export");
			this.display = display;
			this.elements = elements;
			this.cols = cols;
			this.exportId = exportId;
			this.targetFileName = targetFileName;
		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			try {
				JcrMonitor monitor = new EclipseJcrMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Getting objects", -1);
					// session = repository.login();
					final File tmpFile = File.createTempFile("people-extract", ".xls");
					tmpFile.deleteOnExit();

					callJxlEngine(elements, cols, exportId, tmpFile);

					// Call the open file command from the UI thread
					// TODO An error will be thrown if the end user click on
					// "run in background" and then close the corresponding
					// editor
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							Map<String, String> params = new HashMap<String, String>();
							params.put(OpenFile.PARAM_FILE_NAME, targetFileName);
							params.put(OpenFile.PARAM_FILE_URI, URI_FILE_PREFIX + tmpFile.getAbsolutePath());
							CommandUtils.callCommand(appWorkbenchService.getOpenFileCmdId(), params);
						}
					});
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, ConnectUiPlugin.PLUGIN_ID, "Unable to generate export " + exportId, e);
			}
			return Status.OK_STATUS;
		}
	}

	/* DEPENDENCY INJECTION */
	public void setAppWorkbenchService(AppWorkbenchService appWorkbenchService) {
		this.appWorkbenchService = appWorkbenchService;
	}
}
