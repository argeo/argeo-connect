/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.demo.gr.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.ui.GrImages;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

public class ImportFromDeviceWizard extends Wizard {
	private final static Log log = LogFactory
			.getLog(ImportFromDeviceWizard.class);

	private ImportWizardPage importPage;
	private ValidWizardPage validWizardPage;
	private final Node folder;

	public ImportFromDeviceWizard(Node folder) {
		this.folder = folder;
		setWindowTitle(GrMessages.get().wizard_importInstances_title);
	}

	@Override
	public void addPages() {
		importPage = new ImportWizardPage();
		addPage(importPage);

		validWizardPage = new ValidWizardPage();
		addPage(validWizardPage);

		setNeedsProgressMonitor(true);
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The real upload to
	 * the JCR repository is done here.
	 */
	@Override
	public boolean performFinish() {

		// Does nothing for the time being. Implement this
		if (true)
			return true;

		// Initialization
		final String objectPath = importPage.getObjectPath();
		if (objectPath == null || !new File(objectPath).exists()) {
			ErrorFeedback.show("Directory " + objectPath + " does not exist");
			return false;
		}

		Boolean failed = false;
		final File dir = new File(objectPath).getAbsoluteFile();
		final Long sizeB = directorySize(dir, 0l);
		final Stats stats = new Stats();
		Long begin = System.currentTimeMillis();
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					try {
						Integer sizeKB = (int) (sizeB / FileUtils.ONE_KB);
						monitor.beginTask("", sizeKB);
						importDirectoryContent(folder, dir, monitor, stats);
						monitor.done();
					} catch (Exception e) {
						if (e instanceof RuntimeException)
							throw (RuntimeException) e;
						else
							throw new ArgeoException("Cannot import "
									+ objectPath, e);
					}
				}
			});
		} catch (Exception e) {
			ErrorFeedback.show("Cannot import " + objectPath, e);
			failed = true;
		}

		Long duration = System.currentTimeMillis() - begin;
		Long durationS = duration / 1000l;
		String durationStr = (durationS / 60) + " min " + (durationS % 60)
				+ " s";
		StringBuffer message = new StringBuffer("Imported\n");
		message.append(stats.fileCount).append(" files\n");
		message.append(stats.dirCount).append(" directories\n");
		message.append(FileUtils.byteCountToDisplaySize(stats.sizeB));
		if (failed)
			message.append(" of planned ").append(
					FileUtils.byteCountToDisplaySize(sizeB));
		message.append("\n");
		message.append("in ").append(durationStr).append("\n");
		if (failed)
			MessageDialog.openError(getShell(), "Import failed",
					message.toString());
		else
			MessageDialog.openInformation(getShell(), "Import successful",
					message.toString());

		return true;
	}

	/** Recursively computes the size of the directory in bytes. */
	protected Long directorySize(File dir, Long currentSize) {
		Long size = currentSize;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				size = directorySize(file, size);
			} else {
				size = size + file.length();
			}
		}
		return size;
	}

	/**
	 * Import files to the repository if they are not already present
	 */
	protected void importDirectoryContent(Node folder, File dir,
			IProgressMonitor monitor, Stats stats) {
		try {

			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					// SKIP
				} else {
					Long fileSize = file.length();
					// we skip temporary files that are created by apps when a
					// file is being edited.
					// TODO : make this configurable.
					if (file.getName().lastIndexOf('~') != file.getName()
							.length() - 1) {
						monitor.subTask(file.getName() + " ("
								+ FileUtils.byteCountToDisplaySize(fileSize)
								+ ") " + file.getCanonicalPath());
						try {

							importOneFile(file);
							stats.fileCount++;
							stats.sizeB = stats.sizeB + fileSize;
						} catch (Exception e) {
							log.warn("Import of "
									+ file
									+ " ("
									+ FileUtils
											.byteCountToDisplaySize(fileSize)
									+ ") failed: " + e);
							folder.getSession().refresh(false);
						}
						monitor.worked((int) (fileSize / FileUtils.ONE_KB));
					}
				}
			}
		} catch (Exception e) {
			throw new ArgeoException("Cannot import " + dir + " to " + folder,
					e);
		}

	}

	private void importOneFile(File file) throws Exception {
		// TODO implement this
		// Session session = folder.getSession();
		// // TODO generate a name for the newly created node
		// session.importXML(folder.getPath() + "/a name", new FileInputStream(
		// file), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
		// session.save();
	}

	static class Stats {
		public Long fileCount = 0l;
		public Long dirCount = 0l;
		public Long sizeB = 0l;
	}

	private class ImportWizardPage extends WizardPage {

		// private DirectoryFieldEditor dfe;

		public ImportWizardPage() {
			super("TODO i18n");
			setDescription("Please select odk root folder for your mounted device");
		}

		public void createControl(Composite parent) {
			Label lbl = new Label(parent, SWT.NONE);
			lbl.setText("implement here a file browser for both RAP and RCP");
			// FIXME this below is RCP specific
			// dfe = new DirectoryFieldEditor("directory", "From", parent);
			// setControl(dfe.getTextControl(parent));
			setControl(lbl);
		}

		public String getObjectPath() {
			// return dfe.getStringValue();
			return null;
		}

	}

	private class ValidWizardPage extends WizardPage {

		// This class widget
		private TableViewer nodeViewer;

		public ValidWizardPage() {
			super("ValidWizardPage - TODO i18n");
		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			nodeViewer = createItemsViewer(composite);
			refreshData();
			// Don't forget this.
			setControl(composite);
		}

		private TableViewer createItemsViewer(Composite parent) {

			int style = SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL;
			Table table = new Table(parent, style);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			GridData gd = new GridData(GridData.FILL_BOTH
					| GridData.GRAB_VERTICAL);
			table.setLayoutData(gd);

			// Only one column for the time being
			ViewerUtils.createColumn(table, "New files", SWT.LEFT, 120);
			TableViewer viewer = new TableViewer(table);
			viewer.setLabelProvider(new NodeLabelProvider());
			viewer.setContentProvider(new BasicContentProvider());
			return viewer;
		}

		private void refreshData() {
			try {

				// TODO dummy generation of node for the PoC
				if (!folder.hasNode("gr.siteVisite_2012-11-16_10-22-11")) {
					folder.addNode("gr.siteVisite_2012-11-16_10-22-11");
					folder.addNode("gr.siteVisite_2012-11-17_09-48-15");
					folder.addNode("gr.siteVisite_2012-11-17_11-05-07");
					folder.addNode("gr.siteVisite_2012-11-17_13-41-51");
					folder.addNode("gr.siteVisite_2012-11-17_15-25-33");
					folder.addNode("gr.siteVisite_2012-11-17_19-48-11");
					folder.addNode("gr.siteVisite_2012-11-19_08-59-03");
					folder.addNode("gr.siteVisite_2012-11-19_11-35-01");
					folder.addNode("gr.siteVisite_2012-11-19_15-22-17");
					folder.getSession().save();
				}

				int newFileNb = 9;

				// refresh the table content
				NodeIterator ni = folder.getNodes();
				ArrayList<Node> nodes = new ArrayList<Node>();
				while (ni.hasNext())
					nodes.add(ni.nextNode());
				nodeViewer.setInput(nodes);

				// update wizard description
				setDescription(newFileNb
						+ " new files have been found (See list below) "
						+ "and are about to be imported. \n"
						+ "Click on Finish to launch the process.");
			} catch (RepositoryException re) {
				throw new ArgeoException(
						"Unexpected error while getting property values", re);
			}
		}

		private class NodeLabelProvider extends ColumnLabelProvider {

			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				try {
					if (element instanceof Node) {
						Node node = (Node) element;
						cell.setText(node.getName());
						cell.setImage(GrImages.ICON_FORM_INSTANCE);
					} else
						throw new ArgeoException("Unexpected element type");

				} catch (RepositoryException re) {
					throw new ArgeoException(
							"Unexpected error while getting property values",
							re);
				}
			}
		}

		private class BasicContentProvider implements
				IStructuredContentProvider {
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}

			public void dispose() {
			}

			@SuppressWarnings("unchecked")
			public Object[] getElements(Object obj) {
				return ((List<Node>) obj).toArray();
			}
		}
	}
}