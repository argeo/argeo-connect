package org.argeo.connect.people.rap.commands;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.PeopleUiService;
import org.argeo.connect.people.rap.exports.calc.ITableProvider;
import org.argeo.connect.people.rap.exports.calc.RowsToCalcWriter;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.specific.OpenFile;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to generate a spreadsheet displaying nodes and corresponding
 * values for the current active view or editor. Note that this IWorkbenchPart
 * must implement ICalcExtractProvider interface.
 */
public class GetCalcExport extends AbstractHandler {
	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".getCalcExport";
	public final static String PARAM_EXPORT_ID = "param.exportId";

	private final static DateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd_HH-mm");

	/* DEPENDENCY INJECTION */
	private PeopleUiService peopleUiService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String exportId = event.getParameter(PARAM_EXPORT_ID);
		// force default
		if (CommonsJcrUtils.isEmptyString(exportId))
			exportId = PeopleRapConstants.DEFAULT_CALC_EXPORT;

		try {
			IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getActivePart();
			if (iwp instanceof ITableProvider) {
				ITableProvider provider = (ITableProvider) iwp;

				if ((provider).getColumnDefinition(exportId) == null)
					return null;
				else {
					// Create file
					File tmpFile = File
							.createTempFile("people-extract", ".ods");
					tmpFile.deleteOnExit();

					// Effective generation
					callCalcGenerator(provider, exportId, tmpFile);

					// Open result file
					Map<String, String> params = new HashMap<String, String>();
					params.put(OpenFile.PARAM_FILE_NAME, getFileName(provider));
					params.put(OpenFile.PARAM_FILE_URI,
							"file://" + tmpFile.getAbsolutePath());
					CommandUtils.callCommand(
							peopleUiService.getOpenFileCmdId(), params);
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
	protected String getFileName(ITableProvider provider) {
		String prefix = "PeopleExport-";
		String dateVal = df.format(new GregorianCalendar().getTime());
		return prefix + dateVal + ".ods";
	}

	/** Real call to spreadsheet generator. */
	protected synchronized void callCalcGenerator(ITableProvider provider,
			String exportId, File file) throws Exception {

		RowsToCalcWriter writer = new RowsToCalcWriter();
		writer.writeTableFromRows(file, provider.getRows(exportId),
				provider.getColumnDefinition(exportId));
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}