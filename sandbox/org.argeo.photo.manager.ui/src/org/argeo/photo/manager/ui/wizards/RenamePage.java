package org.argeo.photo.manager.ui.wizards;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.argeo.photo.manager.PathConverterResult;
import org.argeo.photo.manager.PatternConverter;
import org.argeo.photo.manager.PhotoDesc;
import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;
import org.argeo.photo.manager.ui.parts.PathConversionViewer;
import org.argeo.photo.manager.ui.parts.PatternConverterViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RenamePage extends WizardPage implements SelectionListener {

	private PatternConverter converter = new PatternConverter() {
		protected void preFormat(Object[] objs) {
			objs[PhotoDesc.FIELD_SRCID] = label.getText();
		}
	};

	private PathConversionViewer pathConversionViewer;
	private PatternConverterViewer patternConverterViewer;

	private Text label;
	private Text fromDir;
	private Button fromDirBrowse;
	private Button refresh;

	public RenamePage(String name, IStructuredSelection selection) {
		super(name);
	}

	public void createControl(Composite parent) {

		Composite panel = new Composite(parent, SWT.NONE);

		panel.setLayout(new GridLayout(1, false));

		{
			Composite sub = new Composite(panel, SWT.NONE);
			sub.setLayout(new GridLayout(3, false));
			sub.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			new Label(sub, SWT.LEAD).setText("From");
			fromDir = new Text(sub, SWT.BORDER);
			fromDir
					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			// FIXME: generalize
			fromDir.setText("/media/NIKON D800/DCIM/100ND800");
			fromDirBrowse = new Button(sub, SWT.NONE);
			fromDirBrowse.setText("Browse...");
			fromDirBrowse.addSelectionListener(this);
		}
		{
			Composite sub = new Composite(panel, SWT.NONE);
			sub.setLayout(new GridLayout(3, false));
			sub.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			new Label(sub, SWT.LEAD).setText("Label");
			label = new Text(sub, SWT.BORDER);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}

		patternConverterViewer = new PatternConverterViewer(panel);
		patternConverterViewer.setInput(converter);
		GridData patternConverterViewerGd = new GridData(GridData.FILL,
				GridData.FILL, true, true);
		patternConverterViewerGd.minimumHeight=150;
		patternConverterViewer.setLayoutData(patternConverterViewerGd);

		refresh = new Button(panel, SWT.NONE);
		refresh.setText("Refresh");
		refresh.addSelectionListener(this);

		pathConversionViewer = new PathConversionViewer(panel);
		pathConversionViewer.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));

		setControl(panel);
	}

	protected void refresh() {
		try {
			// IPath resourcePath = getResourcePath();
			// IPath containerPath = getContainerFullPath();
			// // ResourcesPlugin.getWorkspace().
			// PhotoManagerUiPlugin.stdOut("Refresh resource " + resourcePath);
			// PhotoManagerUiPlugin.stdOut("Refresh container " +
			// containerPath);
			//
			// // File dir = getSpecifiedContainer().getFile(containerPath)
			// // .getLocation().toFile();
			// File dir = getSpecifiedContainer().getRawLocation().toFile();

			File dir = new File(fromDir.getText());
			PhotoManagerUiPlugin.stdOut("Refresh dir " + dir);
			PathConverterResult result = convert(dir);
			if (result == null)
				throw new RuntimeException("No result");
			pathConversionViewer.setPathConverterResult(result);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}

	}

	protected PathConverterResult convert(File dir) {
		File[] files = dir.listFiles();

		if (files == null) {
			PhotoManagerUiPlugin.stdErr("files is null");
			return null;
		}

		Set<String> paths = new TreeSet<String>();
		for (File file : files) {
			// FIXME: more checks?
			String path = file.getPath().substring(dir.getPath().length() + 1);
			paths.add(path);
		}

		return converter.convert(paths);
	}

	public PathConverterResult convert() {
		return convert(new File(fromDir.getText()));
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == fromDirBrowse) {
			Shell shell = PhotoManagerUiPlugin.getDefault().getWorkbench()
					.getDisplay().getActiveShell();
			DirectoryDialog dirDialog = new DirectoryDialog(shell, SWT.OPEN);
			String dirPath = dirDialog.open();
			if (dirPath != null) {
				fromDir.setText(dirPath);
				refresh();
			} else {
				fromDir.setText("Returned dir path was null");
			}
		} else if (e.getSource() == refresh) {
			refresh();
		}

	}

	public String getFromDir() {
		return fromDir.getText();
	}

	public String getLabel() {
		return label.getText();
	}

}
