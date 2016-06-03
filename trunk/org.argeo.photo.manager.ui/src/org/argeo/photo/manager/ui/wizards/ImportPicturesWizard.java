package org.argeo.photo.manager.ui.wizards;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.photo.manager.PathConverterResult;
import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;

/** Import pictures, renaming them based on pattern. */
public class ImportPicturesWizard extends Wizard {
	private RenamePage renamePage;
	private Log log = LogFactory.getLog(ImportPicturesWizard.class);

	@Override
	public void addPages() {
		renamePage = new RenamePage("Rename", null);
		addPage(renamePage);
	}

	@Override
	public boolean performFinish() {
		final PathConverterResult result = renamePage.convert();
		if (result.isValid()) {

			DirectoryDialog ddlg = new DirectoryDialog(Display.getCurrent()
					.getActiveShell(), SWT.OPEN);
			String basePath = ddlg.open();
			String fromDirPath = renamePage.getFromDir();
			String label = renamePage.getLabel();
			if (basePath == null)
				return false;

			try {
				ConvertJob convertJob = new ConvertJob(result, basePath,
						fromDirPath, label);
				getContainer().run(true, true, convertJob);
				return true;
			} catch (Exception e) {
				throw new ArgeoException("Cannot import to " + basePath, e);
			}
		} else {
			PhotoManagerUiPlugin.stdErr("Result is not valid:");
			for (String key : result.getConversionErrors().keySet()) {
				PhotoManagerUiPlugin.stdErr(key + ": "
						+ result.getConversionErrors().get(key));
			}
			for (String duplicated : result.getDuplicated()) {
				PhotoManagerUiPlugin.stdErr(duplicated);
			}
			return false;
		}
	}

	private class ConvertJob implements IRunnableWithProgress {
		PathConverterResult result;
		String basePath;
		String fromDirPath;
		String label;

		public ConvertJob(PathConverterResult result, String basePath,
				String fromDirPath, String label) {
			super();
			this.result = result;
			this.basePath = basePath;
			this.fromDirPath = fromDirPath;
			this.label = label;
		}

		public void run(IProgressMonitor monitor) {
			String toDirPath = basePath + File.separator + label;
			monitor.beginTask("Paths", result.getPaths().size());
			for (String path : result.getPaths()) {
				monitor.subTask(path);
				String fromPath = fromDirPath + File.separator + path;
				String toPath = toDirPath + File.separator
						+ result.getMapping().get(path);
				File toFile = new File(toPath);
				toFile.getParentFile().mkdirs();
				try {
					FileUtils.copyFile(new File(fromPath), toFile, true);
					log.info("Copied " + fromPath + " to " + toFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
				monitor.worked(1);
			}
			monitor.done();
		}
	}
}
