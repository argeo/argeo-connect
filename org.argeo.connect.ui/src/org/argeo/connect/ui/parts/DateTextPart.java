package org.argeo.connect.ui.parts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectException;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** A composite to put in a form to manage a date with a pop-up calendar */
public class DateTextPart extends Composite {
	private static final long serialVersionUID = 7651166365139278532L;

	// Context
	private Node node;
	private String propName;

	// UI Objects
	private final ConnectEditor editor;
	private AbstractFormPart formPart;
	private Text dateTxt;
	private Button openCalBtn;

	private DateFormat dateFormat = new SimpleDateFormat(ConnectConstants.DEFAULT_SHORT_DATE_FORMAT);

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param toolkit
	 * @param formPart
	 *            if null at creation time, use setFormPart to enable life cycle
	 *            management afterwards
	 * @param node
	 * @param propName
	 */
	public DateTextPart(ConnectEditor editor, Composite parent, int style, AbstractFormPart formPart, Node node,
			String propName) {
		super(parent, style);
		this.editor = editor;
		this.formPart = formPart;
		this.node = node;
		this.propName = propName;
		populate(this);
	}

	/**
	 * Generally, the form part is null when the control is created, use this to set
	 * initialised formPart afterwards.
	 */
	public void setFormPart(AbstractFormPart formPart) {
		this.formPart = formPart;
	}

	public void setText(Calendar cal) {
		String newValueStr = "";
		if (cal != null)
			newValueStr = dateFormat.format(cal.getTime());
		if (!newValueStr.equals(dateTxt.getText()))
			dateTxt.setText(newValueStr);
	}

	public void refresh() {
		Calendar cal = null;
		try {
			if (node.hasProperty(propName))
				cal = node.getProperty(propName).getDate();
			dateTxt.setEnabled(editor.isEditing());
			openCalBtn.setEnabled(editor.isEditing());
		} catch (RepositoryException e) {
			throw new ConnectException("Unable to refresh " + propName + " date property for " + node, e);
		}
		setText(cal);
	}

	private void populate(Composite dateComposite) {
		GridLayout gl = ConnectUiUtils.noSpaceGridLayout(2);
		gl.horizontalSpacing = 5;
		dateComposite.setLayout(gl);
		dateTxt = new Text(dateComposite, SWT.BORDER);
		dateTxt.setMessage(ConnectConstants.DEFAULT_SHORT_DATE_FORMAT);
		CmsUtils.style(dateTxt, ConnectUiStyles.FORCE_BORDER);
		dateTxt.setLayoutData(new GridData(150, SWT.DEFAULT));
		dateTxt.setToolTipText(
				"Enter a date with form \"" + ConnectConstants.DEFAULT_SHORT_DATE_FORMAT + "\" or use the calendar");
		openCalBtn = new Button(dateComposite, SWT.FLAT);
		CmsUtils.style(openCalBtn, ConnectUiStyles.OPEN_CALENDAR_BTN);
		openCalBtn.setLayoutData(new GridData(24, 16));

		openCalBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent event) {
				CalendarPopup popup = new CalendarPopup(dateTxt);
				popup.open();
			}
		});

		dateTxt.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent event) {
				try {
					Calendar newVal = parseDate(false);
					if (newVal != null) {
						if (ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.DATE, newVal))
							formPart.markDirty();
					} else if (node.hasProperty(propName)) {
						node.getProperty(propName).remove();
						formPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new ConnectException("Unable to update " + propName + " date property for " + node, e);
				}
			}

			@Override
			public void focusGained(FocusEvent event) {
			}
		});
	}

	private Calendar parseDate(boolean openWrongFormatError) {
		String dateStr = dateTxt.getText();

		if (EclipseUiUtils.notEmpty(dateStr)) {
			try {
				Date date = dateFormat.parse(dateStr);
				Calendar cal = new GregorianCalendar();
				cal.setTime(date);
				return cal;
			} catch (ParseException pe) {
				// Silent. Manage error popup?
				refresh();
				try {
					if (node.hasProperty(propName))
						return node.getProperty(propName).getDate();
				} catch (RepositoryException re) {
					throw new ConnectException("Unable to reset text to old value after parsing of invalid value for "
							+ propName + " of " + node, re);
				}
			}
		}
		return null;
	}

	private class CalendarPopup extends Shell {
		private static final long serialVersionUID = 1L;
		private Calendar currCal = null;
		private DateTime dateTimeCtl;

		public CalendarPopup(Control source) {
			super(source.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
			currCal = parseDate(false);

			populate();

			// Add border and shadow style
			CmsUtils.style(CalendarPopup.this, ConnectUiStyles.POPUP_SHELL);

			pack();
			layout();
			setLocation(source.toDisplay((source.getLocation().x - 2), (source.getSize().y) + 3));

			addShellListener(new ShellAdapter() {
				private static final long serialVersionUID = 5178980294808435833L;

				@Override
				public void shellDeactivated(ShellEvent e) {
					close();
					dispose();
				}

			});
			open();
		}

		private void setProperty() {
			Calendar cal = new GregorianCalendar();
			cal.set(dateTimeCtl.getYear(), dateTimeCtl.getMonth(), dateTimeCtl.getDay(), 12, 0);
			dateTxt.setText(dateFormat.format(cal.getTime()));
			if (ConnectJcrUtils.setJcrProperty(node, propName, PropertyType.DATE, cal))
				formPart.markDirty();
		}

		protected void populate() {
			setLayout(EclipseUiUtils.noSpaceGridLayout());

			dateTimeCtl = new DateTime(this, SWT.CALENDAR);
			dateTimeCtl.setLayoutData(EclipseUiUtils.fillAll());
			if (currCal != null)
				dateTimeCtl.setDate(currCal.get(Calendar.YEAR), currCal.get(Calendar.MONTH),
						currCal.get(Calendar.DAY_OF_MONTH));

			dateTimeCtl.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = -8414377364434281112L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					setProperty();
				}
			});

			dateTimeCtl.addMouseListener(new MouseListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void mouseUp(MouseEvent e) {
				}

				@Override
				public void mouseDown(MouseEvent e) {
				}

				@Override
				public void mouseDoubleClick(MouseEvent e) {
					setProperty();
					close();
					dispose();
				}
			});
		}
	}
}