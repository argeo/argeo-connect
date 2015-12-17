package org.argeo.connect.people.rap.editors.parts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleStyles;
import org.argeo.connect.people.rap.editors.utils.AbstractPeopleEditor;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.util.JcrUiUtils;
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
import org.eclipse.ui.forms.AbstractFormPart;

/** A composite to put in a form to manage a date with a pop-up calendar */
public class DateTextPart extends Composite {
	private static final long serialVersionUID = 7651166365139278532L;

	// Context
	private Node node;
	private String propName;

	// UI Objects
	private final AbstractPeopleEditor editor;
	private AbstractFormPart formPart;
	private Text dateTxt;
	private Button openCalBtn;

	private DateFormat dateFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_SHORT_DATE_FORMAT);

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
	public DateTextPart(AbstractPeopleEditor editor, Composite parent,
			int style, AbstractFormPart formPart, Node node, String propName) {
		super(parent, style);
		this.editor = editor;
		this.formPart = formPart;
		this.node = node;
		this.propName = propName;
		populate(this);
	}

	/**
	 * Generally, the form part is null when the control is created, use this to
	 * set initialised formPart afterwards.
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
			throw new PeopleException("Unable to refresh " + propName
					+ " date property for " + node, e);
		}
		setText(cal);
	}

	private void populate(Composite dateComposite) {
		GridLayout gl = PeopleUiUtils.noSpaceGridLayout(2);
		gl.horizontalSpacing = 5;
		dateComposite.setLayout(gl);
		dateTxt = new Text(dateComposite, SWT.BORDER);
		CmsUtils.style(dateTxt, PeopleStyles.PEOPLE_CLASS_FORCE_BORDER);
		dateTxt.setLayoutData(new GridData(150, SWT.DEFAULT));
		dateTxt.setToolTipText("Enter a date with form \""
				+ PeopleUiConstants.DEFAULT_SHORT_DATE_FORMAT
				+ "\" or use the calendar");
		openCalBtn = new Button(dateComposite, SWT.FLAT);
		CmsUtils.style(openCalBtn, PeopleStyles.FLAT_BTN);
		GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.heightHint = 17;
		openCalBtn.setLayoutData(gd);
		openCalBtn.setImage(PeopleRapImages.CALENDAR_BTN);

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
						if (JcrUiUtils.setJcrProperty(node, propName,
								PropertyType.DATE, newVal))
							formPart.markDirty();
					} else if (node.hasProperty(propName)) {
						node.getProperty(propName).remove();
						formPart.markDirty();
					}
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to update " + propName
							+ " date property for " + node, e);
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
					throw new PeopleException(
							"Unable to reset text to old value after parsing of invalid value for "
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
			CmsUtils.style(CalendarPopup.this,
					PeopleStyles.POPUP_SHELL);

			pack();
			layout();
			setLocation(source.toDisplay((source.getLocation().x - 2),
					(source.getSize().y) + 3));

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
			cal.set(dateTimeCtl.getYear(), dateTimeCtl.getMonth(),
					dateTimeCtl.getDay(), 12, 0);
			dateTxt.setText(dateFormat.format(cal.getTime()));
			if (JcrUiUtils.setJcrProperty(node, propName,
					PropertyType.DATE, cal))
				formPart.markDirty();
		}

		protected void populate() {
			setLayout(EclipseUiUtils.noSpaceGridLayout());

			dateTimeCtl = new DateTime(this, SWT.CALENDAR);
			dateTimeCtl.setLayoutData(EclipseUiUtils.fillAll());
			if (currCal != null)
				dateTimeCtl.setDate(currCal.get(Calendar.YEAR),
						currCal.get(Calendar.MONTH),
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