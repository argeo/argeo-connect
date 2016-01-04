package org.argeo.connect.payment.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.auth.CurrentUser;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.payment.PaymentConstants;
import org.argeo.connect.payment.stripe.StripeConstants;
import org.argeo.connect.payment.stripe.StripeService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * State full UI Component that enables the retrieval of a payment token and
 * then triggers the check-out after setting business specific meta data to the
 * corresponding object
 */
public class StripeTokenCollectorComposite extends Composite implements
		PaymentConstants {
	private final static Log log = LogFactory
			.getLog(StripeTokenCollectorComposite.class);
	private static final long serialVersionUID = -1392423995512547856L;

	// Context
	private final StripeService stripeService;
	// Prepared map with relevant transaction info
	private Map<String, Object> chargeParams;
	private String mainInfoFormTitle = "Please provide your card information";
	private String cvcFormTitle = "Review card information and valid";
	private String acceptedCardDesc = "We accept: Visa, MasterCard, American Express";
	private String stripeDesc = "All Payments are processed by "
			+ "<a href='http://stripe.com' target='_blank'>Stripe</a>.<br />"
			+ "We do not store your card information.";

	// Card info
	private Map<String, Object> cardParams;
	private Text cardNameTxt;
	private Text cardCountryTxt;
	private Text cardNbTxt;
	private Text cardExpMonthTxt;
	private Text cardExpYearTxt;
	private Text cardCvcTxt;

	public StripeTokenCollectorComposite(Composite parent, int style,
			StripeService stripeService, Map<String, Object> chargeParams) {
		super(parent, style | SWT.BORDER);
		// Make a copy to insure financial info cannot be changed afterwards
		this.stripeService = stripeService;
		this.chargeParams = new HashMap<String, Object>();
		this.chargeParams.putAll(chargeParams);
		CmsUtils.style(this, PaymentStyles.CARD_INFO_BOX);
		populateNew();
	}

	private void forceLayout() {
		this.getParent().layout(true, true);
	}

	private void populateNew() {
		Composite parent = this;
		EclipseUiUtils.clear(parent);

		int colNb = 3;
		parent.setLayout(new GridLayout(colNb, false));

		// Header
		populateHeader(parent, mainInfoFormTitle, colNb);

		// Gather user info form
		cardNameTxt = createLt(parent, "Cardholder's name", "your name");
		cardNameTxt.setText(CurrentUser.getDisplayName());
		cardCountryTxt = createLt(parent, "Card country", "");
		cardNbTxt = createLt(parent, "Card number", "");
		// Expiration date
		createBoldLabel(parent, "Expiration date");// (MM/YYYY)");
		cardExpMonthTxt = new Text(parent, SWT.LEAD | SWT.BORDER);
		cardExpMonthTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		cardExpMonthTxt.setMessage("MM");
		cardExpYearTxt = new Text(parent, SWT.LEAD | SWT.BORDER);
		cardExpYearTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		cardExpYearTxt.setMessage("YYYY");
		// Init fields if coming back to this page
		if (cardParams != null) {
			resetText(CARD_NAME, cardNameTxt);
			resetText(CARD_COUNTRY, cardCountryTxt);
			resetText(CARD_NB, cardNbTxt);
			resetText(CARD_EXP_MONTH, cardExpMonthTxt);
			resetText(CARD_EXP_YEAR, cardExpYearTxt);
		}

		// Navigation buttons
		Composite buttonCmp = new Composite(parent, SWT.NO_FOCUS);
		buttonCmp.setBackground(parent.getBackground());
		GridData gd = EclipseUiUtils.fillWidth(colNb);
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalIndent = 10;
		buttonCmp.setLayoutData(gd);
		populateNewChargeButton(buttonCmp);

		// Stripe disclaimer
		Label stripeLbl = new Label(parent, SWT.WRAP | SWT.CENTER);
		CmsUtils.markup(stripeLbl);
		CmsUtils.style(stripeLbl, PaymentStyles.CARD_INFO_TEXT);
		// FIXME rather use a box and some css padding
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false, colNb, 1);
		gd.verticalIndent = 10;
		stripeLbl.setLayoutData(gd);
		stripeLbl.setText(stripeDesc);
		forceLayout();
	}

	private void populateValid() {
		Composite parent = this;
		EclipseUiUtils.clear(parent);

		int colNb = 2;
		parent.setLayout(new GridLayout(colNb, false));

		// Header
		populateHeader(parent, cvcFormTitle, colNb);

		// Main info recap
		createLL(parent, "Holder name", (String) cardParams.get(CARD_NAME));
		createLL(parent, "Card country", (String) cardParams.get(CARD_COUNTRY));
		String formattedNb = formatNb((String) cardParams.get(CARD_NB));
		createLL(parent, "Card number", formattedNb);
		createLL(parent, "Expiration Date (MM/YYYY)",
				(String) cardParams.get(CARD_EXP_MONTH) + "/"
						+ (String) cardParams.get(CARD_EXP_YEAR));

		// CVC text field
		createBoldLabel(parent, "CVC");
		cardCvcTxt = new Text(parent, SWT.LEAD | SWT.BORDER);
		cardCvcTxt.setLayoutData(EclipseUiUtils.fillWidth());

		Composite buttonCmp = new Composite(parent, SWT.NO_FOCUS);
		buttonCmp.setBackground(parent.getBackground());
		GridData gd = EclipseUiUtils.fillWidth(colNb);
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalIndent = 10;
		buttonCmp.setLayoutData(gd);
		populateValidChargeButton(buttonCmp);
	}

	private String formatNb(String cardNb) {
		StringBuilder builder = new StringBuilder();
		while (cardNb.length() >= 4) {
			builder.append(cardNb.substring(0, 4)).append(" ");
			cardNb = cardNb.substring(4);
		}
		builder.append(cardNb);
		return builder.toString();
	}

	private void resetText(String key, Text txt) {
		if (cardParams.containsKey(key))
			txt.setText((String) cardParams.get(key));
	}

	private void populateValidChargeButton(Composite parent) {
		parent.setLayout(new GridLayout(3, false));
		createCancelButton(parent);

		Button backBtn = new Button(parent, SWT.FLAT);
		backBtn.setText("Change card info");
		backBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = -6093659462487548599L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				populateNew();
				forceLayout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Button okBtn = new Button(parent, SWT.FLAT);
		okBtn.setText("Do pay");
		okBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = -6650512174441009678L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (lastCheckBeforeCreatingCharge(e.display.getActiveShell()))
					doPay();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	private void populateNewChargeButton(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		createCancelButton(parent);
		Button okBtn = new Button(parent, SWT.FLAT);
		okBtn.setText("Review and Pay");
		okBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = -6093659462487548599L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				reviewAndValid();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void reviewAndValid() {
		// TODO implement sanity checks
		cardParams = new HashMap<String, Object>();
		// TODO add email from the session

		// TODO add name from the session
		String name = cardNameTxt.getText();
		String country = cardCountryTxt.getText();
		String number = cardNbTxt.getText();
		String month = cardExpMonthTxt.getText();
		String year = cardExpYearTxt.getText();

		if (EclipseUiUtils.isEmpty(name) || EclipseUiUtils.isEmpty(country)
				|| EclipseUiUtils.isEmpty(number)
				|| EclipseUiUtils.isEmpty(month)
				|| EclipseUiUtils.isEmpty(year)) {
			MessageDialog.openError(getShell(), "Unvalid information",
					"All fields should be completed, "
							+ "please update information provided.");
			return;
		}
		cardParams.put(CARD_NAME, name);
		cardParams.put(CARD_COUNTRY, country);
		cardParams.put(CARD_NB, number);
		cardParams.put(CARD_EXP_MONTH, month);
		cardParams.put(CARD_EXP_YEAR, year);
		populateValid();
		forceLayout();
	}

	private void doPay() {
		String cvc = cardCvcTxt.getText();
		if (EclipseUiUtils.isEmpty(cvc)) {
			MessageDialog.openError(getShell(), "Unvalid information",
					"Please enter a valid verification code");
			return;
		}

		cardParams.put(CARD_CVC, cvc);
		chargeParams.put(StripeConstants.STRIPE_CHARGE_CARD, cardParams);
		String idemPotencyKey = UUID.randomUUID().toString();
		String chargeId = null;
		try {
			chargeId = stripeService
					.processCharge(chargeParams, idemPotencyKey);
		} catch (Exception e) {
			String msg = e.getMessage()
					+ "\n No payment have been done. Please verify your information "
					+ "and try again or contact an administrator";
			MessageDialog.openError(getShell(), "Unable to process payment",
					msg);
			log.error(e.getMessage() + "\nUnable to process charge for "
					+ chargeParams.toString());
			if (log.isDebugEnabled())
				e.printStackTrace();
			return;
			// throw new PaymentException("Unable to process charge for "
			// + chargeParams.toString(), e);
		}
		afterPaymentDone(chargeId);
		cardParams.clear();
		chargeParams.clear();
	}

	/**
	 * Implementing class might choose to extend this method to provide specific
	 * Behavior on cancel
	 */
	protected void cancelPayment() {
	}

	/**
	 * Implementing class might choose to extend this method to provide specific
	 * check just before launching the charge creation
	 */
	protected boolean lastCheckBeforeCreatingCharge(Shell shell) {
		return true;
	}

	/**
	 * Implementing class might choose to extend this method to provide specific
	 * behavior after the charge has been created correctly
	 */
	protected void afterPaymentDone(String chargeId) {
	}

	// UI HELPERS
	private void populateHeader(Composite parent, String titleLabel, int colNb) {
		// Header: title with lock icon & accepted cards
		Composite titleCmp = new Composite(parent, SWT.NO_FOCUS);
		titleCmp.setLayout(EclipseUiUtils.noSpaceGridLayout(new GridLayout(2,
				false)));
		titleCmp.setBackground(parent.getBackground());
		titleCmp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				false, colNb, 1));
		// Lock icon
		Label imgLbl = new Label(titleCmp, SWT.NONE);
		imgLbl.setImage(PaymentImages.ICON_LOCK);
		CmsUtils.style(imgLbl, PaymentStyles.CARD_INFO_TEXT);
		// Title
		Label cardInfoTitleLbl = new Label(titleCmp, SWT.WRAP);
		CmsUtils.markup(cardInfoTitleLbl);
		CmsUtils.style(cardInfoTitleLbl, PaymentStyles.CARD_INFO_TITLE);
		GridData gd = EclipseUiUtils.fillWidth();
		gd.verticalIndent = 10; // workaround to keep aligned with the icon
		gd.horizontalIndent = 5; // workaround to keep aligned with the icon
		cardInfoTitleLbl.setLayoutData(gd);
		cardInfoTitleLbl.setText(titleLabel);
		// Accepted cards
		Label acceptedCardsLbl = new Label(parent, SWT.WRAP | SWT.CENTER);
		CmsUtils.markup(acceptedCardsLbl);
		CmsUtils.style(acceptedCardsLbl, PaymentStyles.CARD_INFO_TEXT);
		// FIXME rather use a box and some css padding
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false, colNb, 1);
		// gd.verticalIndent = 5;
		acceptedCardsLbl.setLayoutData(gd);
		acceptedCardsLbl.setText(acceptedCardDesc);
		createLabel(parent, PaymentStyles.CARD_INFO_TEXT, colNb);// Empty line
	}

	private Button createCancelButton(Composite parent) {
		Button cancelBtn = new Button(parent, SWT.FLAT);
		cancelBtn.setText("Cancel");
		cancelBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = -6093659462487548599L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelPayment();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		return cancelBtn;
	}

	private Label createBoldLabel(Composite parent, String labelStr) {
		Label label = new Label(parent, SWT.END);
		CmsUtils.markup(label);
		label.setText(labelStr);
		CmsUtils.style(label, PaymentStyles.CARD_INFO_LABEL);
		label.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		return label;
	}

	private Text createLt(Composite parent, String labelStr, String msgStr) {
		createBoldLabel(parent, labelStr);
		Text text = new Text(parent, SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		text.setMessage(msgStr);
		return text;
	}

	private void createLL(Composite parent, String labelStr, String valueStr) {
		createBoldLabel(parent, labelStr);
		Label label = new Label(parent, SWT.LEAD);
		label.setText(valueStr + "  ");
		CmsUtils.style(label, PaymentStyles.CARD_INFO_VALUE);
		label.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, true, false));
	}

	private Label createLabel(Composite parent, String cssStyle, int colSpan) {
		Label label = new Label(parent, SWT.WRAP);
		CmsUtils.markup(label);
		CmsUtils.style(label, cssStyle);
		label.setLayoutData(EclipseUiUtils.fillWidth(colSpan));
		return label;
	}
}