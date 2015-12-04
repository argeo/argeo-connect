package org.argeo.connect.payment.ui;

import java.util.HashMap;
import java.util.Map;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.payment.PaymentConstants;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * State full UI Component that enables the retrieval of a payment token and
 * then triggers the check-out after setting business specific meta data to the
 * corresponding object
 */
public class StripeTokenCollectorComposite extends Composite implements
		PaymentConstants {
	private static final long serialVersionUID = -1392423995512547856L;

	private Map<String, Object> chargeParams;
	private Map<String, Object> cardParams;

	// Transaction info

	// Card info
	private Text cardCountryTxt;
	private Text cardNbTxt;
	private Text cardExpMonthTxt;
	private Text cardExpYearTxt;
	private Text cardCvcTxt;

	public StripeTokenCollectorComposite(Composite parent, int style,
			Map<String, Object> chargeParams) {
		super(parent, style | SWT.BORDER);
		// Make a copy to insure financial info cannot be changed afterwards
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
		// Card info
		Label cardInfoTitleLbl = createLabel(parent,
				PaymentStyles.CARD_INFO_TITLE, colNb);
		cardInfoTitleLbl.setText("Please provide your card information.");

		Label descLbl = createLabel(parent, PaymentStyles.CARD_INFO_TEXT, colNb);
		descLbl.setText((String) chargeParams
				.get(PaymentConstants.PAYMENT_DESC));

		cardCountryTxt = createLt(parent, "Card country");
		cardNbTxt = createLt(parent, "Card number");
		cardExpMonthTxt = createLt(parent, "Expiration month (MM)");
		cardExpYearTxt = createLt(parent, "Expiration year (YYYY)");

		if (cardParams != null) {
			resetText(CARD_COUNTRY, cardCountryTxt);
			resetText(CARD_NB, cardNbTxt);
			resetText(CARD_EXP_MONTH, cardExpMonthTxt);
			resetText(CARD_EXP_YEAR, cardExpYearTxt);
		}

		Composite buttonCmp = new Composite(parent, SWT.NO_FOCUS);
		buttonCmp.setBackground(parent.getBackground());
		GridData gd = EclipseUiUtils.fillWidth(colNb);
		gd.horizontalAlignment = SWT.CENTER;
		gd.verticalIndent = 10;
		buttonCmp.setLayoutData(gd);
		populateNewChargeButton(buttonCmp);
	}

	private void populateValid() {
		Composite parent = this;
		EclipseUiUtils.clear(parent);

		int colNb = 2;
		parent.setLayout(new GridLayout(colNb, false));
		// Card info
		Label cardInfoTitleLbl = createLabel(parent,
				PaymentStyles.CARD_INFO_TITLE, colNb);
		cardInfoTitleLbl.setText("Review card information and valid");

		Label descLbl = createLabel(parent, PaymentStyles.CARD_INFO_TEXT, colNb);
		descLbl.setText((String) chargeParams
				.get(PaymentConstants.PAYMENT_DESC));

		createLL(parent, "Card country", (String) cardParams.get(CARD_COUNTRY));
		String formattedNb = formatNb((String) cardParams.get(CARD_NB));
		createLL(parent, "Card number", formattedNb);
		createLL(parent, "Expiration month (MM)",
				(String) cardParams.get(CARD_EXP_MONTH));
		createLL(parent, "Expiration year (YYYY)",
				(String) cardParams.get(CARD_EXP_YEAR));
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
		cardParams.put(CARD_COUNTRY, cardCountryTxt.getText());
		cardParams.put(CARD_NB, cardNbTxt.getText());
		cardParams.put(CARD_EXP_MONTH, cardExpMonthTxt.getText());
		cardParams.put(CARD_EXP_YEAR, cardExpYearTxt.getText());
		populateValid();
		forceLayout();
	}

	private void doPay() {
		cardParams.put(CARD_CVC, cardCvcTxt.getText());
		// TODO really pay
		// TODO reset card info
	}

	/**
	 * Implementing class might choose to extend this method to provide specific
	 * Behavior on cancel
	 */
	protected void cancelPayment() {

	}

	/**
	 * Implementing class might choose to extend this method to provide specific
	 * behavior after the charge has been created correctly
	 */
	protected void afterPaymentDone(String chargeId) {
	}

	// UI HELPERS
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

	private Text createLt(Composite parent, String labelStr) {
		createBoldLabel(parent, labelStr);
		Text text = new Text(parent, SWT.LEAD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
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