package org.argeo.connect.people.ui.commands;

import java.io.File;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.extracts.CalcFileProvider;
import org.argeo.connect.people.ui.extracts.ITableProvider;
import org.argeo.connect.people.ui.extracts.RowIteratorToCalcWriter;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.rap.rwt.RWT;
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
	public final static String PARAM_EXTACT_ID = "param.extractId";

	private CalcFileProvider efp = new CalcFileProvider();
	private FileHandler fileHandler = new FileHandler(efp);

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String extractId = event.getParameter(PARAM_EXTACT_ID);

		try {
			IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getActivePart();
			if (iwp instanceof ITableProvider) {
				File tmpFile = File.createTempFile("people-extract", ".xls");
				tmpFile.deleteOnExit();
				callCalcGenerator((ITableProvider) iwp, extractId,
						tmpFile);
				fileHandler.openFile(tmpFile.getName(),
						tmpFile.getAbsolutePath());
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
	protected synchronized void callCalcGenerator(
			ITableProvider provider, String extractId, File file)
			throws Exception {
		RowIteratorToCalcWriter writer = new RowIteratorToCalcWriter();
		writer.setColumnDefinition(provider.getColumnDefinition(extractId));
		writer.writeTableFromRowIterator(file,
				provider.getRowIterator(extractId));
	}

	@Override
	public void dispose() {
		super.dispose();
		// free the service handler of the file provider
		RWT.getServiceManager().unregisterServiceHandler(
				FileHandler.DOWNLOAD_SERVICE_NAME);

	}
}