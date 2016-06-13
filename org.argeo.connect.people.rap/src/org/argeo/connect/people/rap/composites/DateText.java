package org.argeo.connect.people.rap.composites;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.rap.PeopleStyles;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
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

/**
 * A text composite to request end user for a date using a text and a calendar
 * popup
 * */
public class DateText extends Composite {
	private static final long serialVersionUID = 7651166365139278532L;

	// Context
	private Calendar calendar;

	// UI Objects
	private Text dateTxt;
	private Button openCalBtn;

	private DateFormat dateFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_SHORT_DATE_FORMAT);

	/**
	 * 
	 * @param parent
	 * @param style
	 * @param node
	 * @param propName
	 */
	public DateText(Composite parent, int style) {
		super(parent, style);
		populate(this);
	}

	/**
	 * Returns the user defined date as Calendar or null if none has been
	 * defined
	 */
	public Calendar getCalendar() {
		return calendar;
	}

	/** Enable setting a custom tooltip on the underlyting text */
	public void setToolTipText(String toolTipText) {
		dateTxt.setToolTipText(toolTipText);
	}

	/** Enable setting a custom message on the underlying text */
	public void setMessage(String message) {
		dateTxt.setMessage(message);
	}

	private void populate(Composite dateComposite) {
		GridLayout gl = PeopleUiUtils.noSpaceGridLayout(2);
		gl.horizontalSpacing = 5;
		dateComposite.setLayout(gl);
		dateTxt = new Text(dateComposite, SWT.BORDER);
		CmsUtils.style(dateTxt, PeopleStyles.PEOPLE_CLASS_FORCE_BORDER);
		dateTxt.setLayoutData(new GridData(80, SWT.DEFAULT));
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
				String newVal = dateTxt.getText();
				// Enable reset of the field
				if (EclipseUiUtils.isEmpty(newVal))
					calendar = null;
				else {
					try {
						Calendar newCal = parseDate(newVal);
						// DateText.this.setText(newCal);
						calendar = newCal;
					} catch (ParseException pe) {
						// Silent. Manage error popup?
						if (calendar != null)
							DateText.this.setText(calendar);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent event) {
			}
		});
	}

	public void setText(Calendar cal) {
		String newValueStr = "";
		if (cal != null)
			newValueStr = dateFormat.format(cal.getTime());
		if (!newValueStr.equals(dateTxt.getText()))
			dateTxt.setText(newValueStr);
	}

	private Calendar parseDate(String newVal) throws ParseException {
		if (EclipseUiUtils.notEmpty(newVal)) {
			Date date = dateFormat.parse(newVal);
			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			return cal;
		}
		return null;
	}

	private class CalendarPopup extends Shell {
		private static final long serialVersionUID = 1L;
		private DateTime dateTimeCtl;

		public CalendarPopup(Control source) {
			super(source.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
			populate();
			// Add border and shadow style
			CmsUtils.style(CalendarPopup.this, PeopleStyles.POPUP_SHELL);
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
			calendar = cal;
		}

		protected void populate() {
			setLayout(EclipseUiUtils.noSpaceGridLayout());

			dateTimeCtl = new DateTime(this, SWT.CALENDAR);
			dateTimeCtl.setLayoutData(EclipseUiUtils.fillAll());
			if (calendar != null)
				dateTimeCtl.setDate(calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH),
						calendar.get(Calendar.DAY_OF_MONTH));

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