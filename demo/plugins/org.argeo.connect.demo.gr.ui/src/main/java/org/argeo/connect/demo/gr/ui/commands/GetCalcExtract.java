package org.argeo.connect.demo.gr.ui.commands;

import java.io.File;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.exports.ICalcExtractProvider;
import org.argeo.connect.demo.gr.ui.exports.JcrToCalcWriter;
import org.argeo.connect.demo.gr.ui.providers.TmpFileProvider;
import org.argeo.eclipse.ui.specific.FileHandler;
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
	public final static String ID = GrUiPlugin.PLUGIN_ID + ".getCalcExtract";

	private TmpFileProvider tfp = new TmpFileProvider();
	private FileHandler fileHandler = new FileHandler(tfp);

	public final static String PARAM_EXTRACT_ID = GrUiPlugin.PLUGIN_ID
			+ ".extractId";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String extractId = event.getParameter(PARAM_EXTRACT_ID);

		try {
			IWorkbenchPart iwp = HandlerUtil.getActiveWorkbenchWindow(event)
					.getActivePage().getActivePart();
			if (iwp instanceof ICalcExtractProvider) {
				File tmpFile = File.createTempFile("gr-extract", ".xls");
				tmpFile.deleteOnExit();
				extractExcel((ICalcExtractProvider) iwp, extractId, tmpFile);
				fileHandler.openFile(tmpFile.getName(),
						tmpFile.getAbsolutePath());
			} else
				throw new ArgeoException(iwp.toString()
						+ " is not an instance of "
						+ "ICalcExtractProvider interface." + "Command " + ID
						+ " can be call only when active part "
						+ "implements ICalcExtractProvider interface.");
			return null;
		} catch (Exception e) {
			throw new ArgeoException("Cannot create and populate spreadsheet",
					e);
		}
	}

	/** Real call to spreadsheet generator. */
	protected synchronized void extractExcel(ICalcExtractProvider provider,
			String extractId, File file) throws Exception {
		JcrToCalcWriter writer = new JcrToCalcWriter(provider, extractId);
		writer.writeSpreadSheet(file, provider.getNodeList(extractId));
	}
}