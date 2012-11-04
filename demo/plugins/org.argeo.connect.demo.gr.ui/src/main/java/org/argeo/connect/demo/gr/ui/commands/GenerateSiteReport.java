package org.argeo.connect.demo.gr.ui.commands;

import java.io.File;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.utils.TmpFileProvider;
import org.argeo.eclipse.ui.specific.FileHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class GenerateSiteReport extends AbstractHandler {
	public final static String ID = GrUiPlugin.PLUGIN_ID
			+ ".generateSiteReport";
	public final static String PARAM_UID = "com.ignfi.gr.client.ui.siteUid";

	/* DEPENDENCY INJECTION */
	private GrBackend grBackend;

	private FileHandler fileHandler;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String uid = event.getParameter(PARAM_UID);
		try {
			File file = grBackend.getSiteReport(uid);
			TmpFileProvider ssfp = new TmpFileProvider();
			FileHandler fileHandler = new FileHandler(ssfp);
			StringBuffer fileName = new StringBuffer();
			fileName.append("SiteReport-");
			// TODO put site name instead of site UID
			fileName.append(uid);
			fileName.append(".pdf");
			fileHandler.openFile(fileName.toString(), file.getAbsolutePath());
			// try {
			// file.delete();
			// } catch (Exception e) {
			// // silent: we also have the delete on exit set.
			// }

		} catch (Exception e) {
			throw new ArgeoException("Cannot generate site report", e);
		}
		return null;
	}

	/* DEPENDENCY INJECTION */
	public void setGrBackend(GrBackend grBackend) {
		this.grBackend = grBackend;
	}

}
