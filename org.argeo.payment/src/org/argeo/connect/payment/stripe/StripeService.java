package org.argeo.connect.payment.stripe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.payment.PaymentException;
import org.argeo.node.NodeUtils;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.PeopleJcrUtils;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.CountrySpec;
import com.stripe.net.RequestOptions;

/** Centralises call to the Stripe API */
public class StripeService {
	private final static Log log = LogFactory.getLog(StripeService.class);

	private final static String STRIPE = "Stripe";

	private List<Locale> availableCountries;

	public StripeService() {
		String apiKey = System.getProperty(StripeConstants.STRIPE_API_KEY_PROP);
		if (apiKey != null)
			Stripe.apiKey = apiKey;
		else
			log.warn("No stripe API key found, " + "the key must be set externally or "
					+ "the following calls to Stripe API will fail");
	}

	public void init() {
		try {
			// retrieve available countries
			if (log.isDebugEnabled())
				log.debug("Retrieving Stripe available countries...");
			List<Locale> availableCountries = new ArrayList<>();
			Map<String, Object> params = new HashMap<String, Object>();
			Iterable<CountrySpec> countrySpecs = CountrySpec.list(params).autoPagingIterable();
			for (CountrySpec countrySpec : countrySpecs) {
				Locale locale = new Locale("", countrySpec.getId());
				availableCountries.add(locale);
			}
			this.availableCountries = Collections.unmodifiableList(availableCountries);
		} catch (StripeException e) {
			throw new PaymentException("Cannot list available countries", e);
		}

	}

	public void destroy() {

	}

	/** Does not work with account that have not yet been verified */
	public String createManagedAccount(Session session, String username, String ipAddress)
			throws RepositoryException, StripeException {
		Node profile = PeopleJcrUtils.getProfile(session, username);
		if (profile == null)
			throw new PaymentException("Cannot find profile for user " + username);
		if (profile.hasNode(ConnectNames.CONNECT_EXTERNAL_IDS + '/' + STRIPE)) {
			throw new PaymentException("Already a Stripe account for " + username);
		}
		Map<String, Object> accountParams = accountParamsFromPerson(profile);

		// Stripe TOS
		Map<String, Object> tosAcceptanceParams = new HashMap<String, Object>();
		tosAcceptanceParams.put("date", (long) System.currentTimeMillis() / 1000L);
		tosAcceptanceParams.put("ip", ipAddress);
		accountParams.put("tos_acceptance", tosAcceptanceParams);

		try {
			Account newAccount = Account.create(accountParams);
			String stripeId = newAccount.getId();
			Node externalIds = profile.addNode(ConnectNames.CONNECT_EXTERNAL_IDS);
			Node stripeExternalId = externalIds.addNode(STRIPE, ConnectTypes.CONNECT_EXTERNAL_ID);
			stripeExternalId.setProperty(ConnectNames.CONNECT_SOURCE_ID, STRIPE);
			stripeExternalId.setProperty(ConnectNames.CONNECT_EXTERNAL_UID, stripeId);
			session.save();
			if (log.isDebugEnabled())
				log.debug("Stripe account " + stripeId + " created for " + username);
			return stripeId;
		} finally {
		}
	}

	private Map<String, Object> accountParamsFromPerson(Node person) throws RepositoryException {
		Node contacts = person.getNode(PeopleNames.PEOPLE_CONTACTS);
		NodeIterator children = contacts.getNodes();
		Node postalAddress = null;
		while (children.hasNext()) {
			Node child = children.nextNode();
			if (child.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS)) {
				postalAddress = child;
				break;
			}
		}
		if (postalAddress == null)
			throw new PaymentException("A postal address is required");
		Map<String, Object> account = new HashMap<String, Object>();
		account.put("type", "custom");
		account.put("email", person.getProperty(PeopleNames.PEOPLE_PRIMARY_EMAIL).getString());
		account.put("country", postalAddress.getProperty(PeopleNames.PEOPLE_COUNTRY).getString());

		Map<String, Object> legalEntity = new HashMap<String, Object>();
		legalEntity.put("first_name", person.getProperty(PeopleNames.PEOPLE_FIRST_NAME).getString());
		legalEntity.put("last_name", person.getProperty(PeopleNames.PEOPLE_LAST_NAME).getString());
		legalEntity.put("type", "individual");

		Map<String, Object> address = new HashMap<String, Object>();
		address.put("city", postalAddress.getProperty(PeopleNames.PEOPLE_CITY).getString());
		address.put("line1", postalAddress.getProperty(PeopleNames.PEOPLE_STREET).getString());
		address.put("postal_code", postalAddress.getProperty(PeopleNames.PEOPLE_ZIP_CODE).getString());
		legalEntity.put("address", address);

		Map<String, Object> dateOfBirth = new HashMap<String, Object>();
		Calendar birthDate = person.getProperty(PeopleNames.PEOPLE_BIRTH_DATE).getDate();
		dateOfBirth.put("day", Integer.toString(birthDate.get(Calendar.DAY_OF_MONTH)));
		dateOfBirth.put("month", Integer.toString(birthDate.get(Calendar.MONTH)));
		dateOfBirth.put("year", Integer.toString(birthDate.get(Calendar.YEAR)));
		legalEntity.put("dob", dateOfBirth);

		account.put("legal_entity", legalEntity);
		return account;
	}

	public List<Locale> listAvailableCountries(Comparator<Locale> comparator) {
		if (comparator == null)// stripe order
			return availableCountries;
		List<Locale> res = new ArrayList<>(availableCountries);
		Collections.sort(res, comparator);
		return res;
	}

	/** Retrieves all charges for the current connected account */
	public List<Charge> listCharges(String filter) throws StripeException {
		ChargeCollection charges = Charge.list(null);
		return charges.getData();
	}

	/** */
	public String processCharge(Map<String, Object> chargeParams, String idemPotencyKey) throws StripeException {
		RequestOptions options = RequestOptions.builder().setIdempotencyKey(idemPotencyKey).build();
		Charge charge = Charge.create(chargeParams, options);
		String chargeId = charge.getId();
		return chargeId;
	}
}