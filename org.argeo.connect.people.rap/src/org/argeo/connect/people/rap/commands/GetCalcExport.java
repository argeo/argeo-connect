package org.argeo.connect.people.rap.commands;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.query.Row;

import org.argeo.ArgeoMonitor;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleWorkbenchService;
import org.argeo.connect.people.rap.exports.calc.IJcrTableViewer;
import org.argeo.connect.people.rap.exports.calc.NodesToCalcWriter;
import org.argeo.connect.people.rap.exports.calc.RowsToCalcWriter;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseArgeoMonitor;
import org.argeo.eclipse.ui.workbench.CommandUtils;
import org.argeo.eclipse.ui.workbench.commands.OpenFile;
import org.argeo.jcr.JcrUtils;
import org.argeo.security.ui.PrivilegedJob;
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
 * Command handler to generate a spreadsheet displaying nodes and corresponding
 * values for the current active view or editor. Note that this IWorkbenchPart
 * must implement ICalcExtractProvider interface.
 */
public class GetCalcExport extends AbstractHandler {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".getCalcExport";
	public final static String PARAM_EXPORT_ID = "param.exportId";

	private final static DateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd_HH-mm");

	/* DEPENDENCY INJECTION */
	private PeopleWorkbenchService peopleWorkbenchService;
	private Repository repository;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String exportId = event.getParameter(PARAM_EXPORT_ID);
		// force default
		if (CommonsJcrUtils.isEmptyString(exportId))
			exportId = PeopleRapConstants.DEFAULT_CALC_EXPORT;

		try {
			IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getActivePart();
			if (iwp instanceof IJcrTableViewer) {
				IJcrTableViewer provider = (IJcrTableViewer) iwp;

				if ((provider).getColumnDefinition(exportId) == null)
					return null;
				else {
					new GenerateExtract(HandlerUtil.getActivePart(event)
							.getSite().getShell().getDisplay(), repository,
							provider, exportId).schedule();
				}
			} else
				throw new PeopleException(iwp.toString()
						+ " is not an instance of "
						+ "ICalcExtractProvider interface." + " Command " + ID
						+ " can be call only when active part "
						+ "implements ICalcExtractProvider interface.");
			return null;
		} catch (Exception e) {
			throw new PeopleException("Cannot create and populate spreadsheet",
					e);
		}
	}

	/** Override to provide other naming strategy */
	protected String getFileName(IJcrTableViewer provider) {
		String prefix = "PeopleExport-";
		String dateVal = df.format(new GregorianCalendar().getTime());
		return prefix + dateVal + ".ods";
	}

	/** Real call to spreadsheet generator. */
	protected synchronized void callCalcGenerator(IJcrTableViewer provider,
			String exportId, File file) throws Exception {

		Object[] elements = provider.getElements(exportId);
		if (elements instanceof Row[]) {
			RowsToCalcWriter writer = new RowsToCalcWriter();
			writer.writeTableFromRows(file,
					(Row[]) elements,
					provider.getColumnDefinition(exportId));
		} else if (elements instanceof Node[]) {
			NodesToCalcWriter writer = new NodesToCalcWriter();
			writer.writeTableFromNodes(file,
					(Node[]) elements,
					provider.getColumnDefinition(exportId));
		}
	}

	/** Privileged job that performs the extract asynchronously */
	private class GenerateExtract extends PrivilegedJob {

		private Display display;
		private Repository repository;
		private IJcrTableViewer provider;
		private String exportId;

		public GenerateExtract(Display display, Repository repository,
				IJcrTableViewer provider, String exportId) {
			super("Generating the export");
			this.display = display;
			this.repository = repository;
			this.provider = provider;
			this.exportId = exportId;

		}

		protected IStatus doRun(IProgressMonitor progressMonitor) {
			Session session = null;
			try {
				ArgeoMonitor monitor = new EclipseArgeoMonitor(progressMonitor);
				if (monitor != null && !monitor.isCanceled()) {
					monitor.beginTask("Getting objects", -1);

					session = repository.login();

					// Create file
					final File tmpFile = File.createTempFile("people-extract",
							".ods");
					// TODO does not work: files are deleted only when the
					// corresponding JVM is closed
					tmpFile.deleteOnExit();

					// TODO An error will be thrown if the end user click on
					// "run in background" and then close the corresponding
					// editor

					// Effective generation
					callCalcGenerator(provider, exportId, tmpFile);

					display.asyncExec(new Runnable() {

						@Override
						public void run() {
							// Open result file
							Map<String, String> params = new HashMap<String, String>();
							params.put(OpenFile.PARAM_FILE_NAME,
									getFileName(provider));
							params.put(OpenFile.PARAM_FILE_URI, "file://"
									+ tmpFile.getAbsolutePath());
							CommandUtils.callCommand(
									peopleWorkbenchService.getOpenFileCmdId(),
									params);

						}
					});
				}
			} catch (Exception e) {
				return new Status(IStatus.ERROR, PeopleRapPlugin.PLUGIN_ID,
						"Unable to generate export " + exportId, e);
			} finally {
				JcrUtils.logoutQuietly(session);
			}
			return Status.OK_STATUS;
		}
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleWorkbenchService(
			PeopleWorkbenchService peopleWorkbenchService) {
		this.peopleWorkbenchService = peopleWorkbenchService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
}