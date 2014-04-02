package org.argeo.connect.people.ui.commands;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.PeopleUiService;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.extracts.RowsToCalcWriter;
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
public class GetCalcExtract extends AbstractHandler {
	public final static String ID = PeopleUiPlugin.PLUGIN_ID
			+ ".getCalcExtract";
	public final static String PARAM_EXTRACT_ID = "param.extractId";

	/* DEPENDENCY INJECTION */
	private PeopleUiService peopleUiService;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String extractId = event.getParameter(PARAM_EXTRACT_ID);

		try {
			IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getActivePart();
			if (iwp instanceof ITableProvider) {
				if (((ITableProvider) iwp).getColumnDefinition(extractId) == null)
					return null;
				else {
					File tmpFile = File
							.createTempFile("people-extract", ".xls");
					tmpFile.deleteOnExit();

					if (CommonsJcrUtils.checkNotEmptyString(extractId))
						callCalcGenerator((ITableProvider) iwp, extractId,
								tmpFile);
					else
						callCalcGenerator((ITableProvider) iwp,
								PeopleUiConstants.DEFAULT_CALC_EXPORT, tmpFile);

					Map<String, String> params = new HashMap<String, String>();
					params.put(OpenFile.PARAM_FILE_NAME, tmpFile.getName());
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

	/** Real call to spreadsheet generator. */
	protected synchronized void callCalcGenerator(ITableProvider provider,
			String extractId, File file) throws Exception {

		// throw new
		// PeopleException("Implement this, broken by the new search strategy");
		RowsToCalcWriter writer = new RowsToCalcWriter();
		// writer.setColumnDefinition();
		writer.writeTableFromRows(file, provider.getRows(extractId),
				provider.getColumnDefinition(extractId));
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleUiService(PeopleUiService peopleUiService) {
		this.peopleUiService = peopleUiService;
	}
}