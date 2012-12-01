package org.argeo.photo.manager.ui;

import java.io.File;
import java.text.MessageFormat;
import java.text.ParseException;

import org.argeo.photo.manager.PhotoDesc;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class RenameView extends ViewPart {
	public static final String ID = PhotoManagerUiPlugin.PLUGIN_ID
			+ ".renameView";

	private TableViewer viewer;

	private String label = "204-GuerreLiban-D-0607";

	private String dirPattern = "/mnt/wind/labo/Temp_GuerreLiban/{0}/LARGE";
	private String fromPattern = "{1,number,00}_{2}.jpg";
	private String toPattern = "{0}-{1,number,00}.jpg";

	private MessageFormat fromFormat = new MessageFormat(fromPattern);
	private MessageFormat toFormat = new MessageFormat(toPattern);

	// private Long srcId = new Long(26);

	private File dir = null;

	private Text dirT;
	private Text labelT;
	private Text from;
	private Text to;
	private Button refresh;
	private Button rename;
	private Text consoleT;

	public void createPartControl(Composite root) {
		Composite parent = new Composite(root, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));

		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true, 1, 30));
		panel.setLayout(new GridLayout(2, false));

		new Label(panel, SWT.LEFT).setText("Label");
		labelT = createText(panel);
		labelT.setText(label);

		new Label(panel, SWT.LEFT).setText("Dir");
		dirT = createText(panel);
		dirT.setText(dirPattern);

		new Label(panel, SWT.LEFT).setText("From");
		from = createText(panel);
		from.setText(fromPattern);

		new Label(panel, SWT.LEFT).setText("To");
		to = createText(panel);
		to.setText(toPattern);

		refresh = new Button(panel, SWT.PUSH);
		refresh.setText("Refresh");
		refresh.addSelectionListener(new SelListener());

		rename = new Button(panel, SWT.PUSH);
		rename.setText("Rename");
		rename.addSelectionListener(new SelListener());

		consoleT = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL);
		consoleT.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true, 1, 5));
		consoleT.setText("");

		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.BORDER);
		scroll.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.setMinSize(600, 400);

		viewer = new TableViewer(scroll, SWT.MULTI | SWT.V_SCROLL);
		Table table = viewer.getTable();
		scroll.setContent(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn colFrom = new TableColumn(table, SWT.LEFT);
		colFrom.setText("From");
		colFrom.setWidth(300);
		TableColumn colTo = new TableColumn(table, SWT.LEFT);
		colTo.setText("To");
		colTo.setWidth(300);

		updateFromUi();

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
	}

	private Text createText(Composite panel) {
		Text text = new Text(panel, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		return text;
	}

	protected String getNewName(File orig) throws ParseException {
		Object objs[] = fromFormat.parse(orig.getName());
		objs[PhotoDesc.FIELD_SRCID] = label;
		return MessageFormat.format(toPattern, objs);
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void stdOut(Object obj) {
		if (obj != null)
			consoleT.append(obj.toString() + "\n");
		else
			consoleT.append("null\n");
	}

	private void stdErr(Object obj) {
		stdOut("ERR " + obj);
	}

	protected void updateFromUi() {
		// Update from user input
		fromPattern = from.getText();
		toPattern = to.getText();
		dirPattern = dirT.getText();
		label = labelT.getText();
		fromFormat = new MessageFormat(fromPattern);
		toFormat = new MessageFormat(toPattern);

		String dirPath = MessageFormat.format(dirPattern,
				new Object[] { label });
		dir = new File(dirPath);
	}

	protected class SelListener implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			updateFromUi();

			if (!dir.exists()) {
				stdErr("Dir " + dir + " does not exist. Doing nothing.");
				return;
			}

			if (e.getSource() == refresh) {
				viewer.refresh();
				stdOut("List refreshed.");
			} else if (e.getSource() == rename) {
				File[] files = dir.listFiles();
				for (File file : files) {
					if (file.isDirectory())
						continue;

					try {
						String newName = getNewName(file);
						File newFile = new File(dir.getPath() + File.separator
								+ newName);
						if (newFile.exists()) {
							stdErr("New file " + newFile
									+ " already exists, skipping...");
							continue;
						}

						stdOut("Renaming " + file.getName() + " to "
								+ newFile.getName());
						file.renameTo(newFile);
					} catch (Exception e1) {
						stdErr("Cannot rename " + file + ": " + e1);
					}
				}
			}
		}
	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (dir == null || !dir.exists())
				return new Object[] { "Nothing to display for dir " + dir };
			else
				return dir.listFiles();
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (!(obj instanceof File)) {
				if (index == 0)
					return obj != null ? obj.toString() : "null";
				else
					return "";
			}

			File file = (File) obj;

			if (index == 0) {
				return file.getName();
			} else if (index == 1) {
				try {
					return getNewName(file);
				} catch (ParseException e) {
					// Check if parsable by the toPattern
					try {
						Object objs[] = toFormat.parse(file.getName());
						Object fieldSrc = objs[PhotoDesc.FIELD_SRCID];
						stdOut("fieldSrc=" + fieldSrc);
						if (fieldSrc != null && label.equals(fieldSrc)) {
							return "Already converted";
						}
					} catch (ParseException e1) {
						// silent
						stdErr("Exception: " + e1);
					}
					return e.getMessage();
				}
			} else {
				return null;
			}
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}

	}
}