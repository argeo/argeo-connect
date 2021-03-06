package org.argeo.connect.ui.widgets;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.argeo.cms.ui.util.CmsUiUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ui.ConnectImages;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.connect.ui.ConnectUiUtils;
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
 */
public class DateText extends Composite {
	private static final long serialVersionUID = 7651166365139278532L;

	// Context
	private Calendar calendar;

	// UI Objects
	private Text dateTxt;
	private Button openCalBtn;

	private DateFormat dateFormat = new SimpleDateFormat(ConnectConstants.DEFAULT_SHORT_DATE_FORMAT);

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
	 * Returns the user defined date as Calendar or null if none has been defined
	 */
	public Calendar getDate() {
		return calendar;
	}

	/** @deprecated use {@link #getDate()} instead */
	@Deprecated
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
		GridLayout gl = ConnectUiUtils.noSpaceGridLayout(2);
		gl.horizontalSpacing = 5;
		dateComposite.setLayout(gl);
		dateTxt = new Text(dateComposite, SWT.BORDER);
		dateTxt.setMessage(ConnectConstants.DEFAULT_SHORT_DATE_FORMAT);
		CmsUiUtils.style(dateTxt, ConnectUiStyles.FORCE_BORDER);
		dateTxt.setLayoutData(new GridData(80, SWT.DEFAULT));
		dateTxt.setToolTipText(
				"Enter a date with form \"" + ConnectConstants.DEFAULT_SHORT_DATE_FORMAT + "\" or use the calendar");
		openCalBtn = new Button(dateComposite, SWT.FLAT);
		openCalBtn.setAlignment(SWT.CENTER);
		openCalBtn.setImage(ConnectImages.CALENDAR);
		// CmsUiUtils.style(openCalBtn, ConnectUiStyles.OPEN_CALENDAR_BTN);
		// openCalBtn.setLayoutData(new GridData(16, 16));

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
							DateText.this.setDate(calendar);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent event) {
			}
		});
	}

	/** @deprecated Use {@link #setDate(Calendar)} instead */
	@Deprecated
	public void setText(Calendar cal) {
		setDate(cal);
	}

	public void setDate(Calendar calendar) {
		String newValueStr = "";
		if (calendar != null)
			newValueStr = dateFormat.format(calendar.getTime());
		if (!newValueStr.equals(dateTxt.getText()))
			dateTxt.setText(newValueStr);
		this.calendar = calendar;
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
			super(source.getDisplay().getActiveShell(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
			populate();
			// Add border and shadow style
			CmsUiUtils.style(CalendarPopup.this, ConnectUiStyles.POPUP_SHELL);
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
			calendar = cal;
		}

		protected void populate() {
			setLayout(EclipseUiUtils.noSpaceGridLayout());

			dateTimeCtl = new DateTime(this, SWT.CALENDAR);
			dateTimeCtl.setLayoutData(EclipseUiUtils.fillAll());
			if (calendar != null)
				dateTimeCtl.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
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
