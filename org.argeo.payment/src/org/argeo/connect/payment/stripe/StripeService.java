package org.argeo.connect.payment.stripe;

import static org.argeo.connect.payment.stripe.StripeConstants.STRIPE_ACCOUNT_MANAGED;
import static org.argeo.connect.payment.stripe.StripeConstants.STRIPE_COUNTRY;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.payment.PaymentException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleTypes;

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
	public String createManagedAccount(String countryCode) throws StripeException {
		Map<String, Object> accountParams = new HashMap<String, Object>();
		accountParams.put(STRIPE_COUNTRY, countryCode);
		accountParams.put(STRIPE_ACCOUNT_MANAGED, true);
		Account newAccount = Account.create(accountParams);
		return newAccount.getId();
	}

	private Map<String, Object> stripePropertiesFromPerson(Node person) throws RepositoryException {
		NodeIterator children = person.getNodes();
		Node postalAddress = null;
		while (children.hasNext()) {
			Node child = children.nextNode();
			if (child.isNodeType(PeopleTypes.PEOPLE_POSTAL_ADDRESS)) {
				postalAddress = child;
			}
		}
		if (postalAddress == null)
			throw new PaymentException("A postal address is rquired");
		Map<String, Object> accountParams = new HashMap<String, Object>();
		accountParams.put("type", "custom");
		accountParams.put("email", person.getProperty(PeopleNames.PEOPLE_PRIMARY_EMAIL));
		accountParams.put("country", postalAddress.getProperty(PeopleNames.PEOPLE_COUNTRY));

		accountParams.put("legal_entity.address.city",
				postalAddress.getProperty(PeopleNames.PEOPLE_COUNTRY).getString());
		accountParams.put("legal_entity.address.line1",
				postalAddress.getProperty(PeopleNames.PEOPLE_STREET).getString());
		accountParams.put("legal_entity.address.postal_code",
				postalAddress.getProperty(PeopleNames.PEOPLE_ZIP_CODE).getString());
		Calendar birthDate = person.getProperty(PeopleNames.PEOPLE_BIRTH_DATE).getDate();
		accountParams.put("legal_entity.dob.day", Integer.toString(birthDate.get(Calendar.DAY_OF_MONTH)));
		accountParams.put("legal_entity.dob.month", Integer.toString(birthDate.get(Calendar.MONTH)));
		accountParams.put("legal_entity.dob.year", Integer.toString(birthDate.get(Calendar.YEAR)));
		accountParams.put("legal_entity.first_name", person.getProperty(PeopleNames.PEOPLE_FIRST_NAME));
		accountParams.put("legal_entity.last_name", person.getProperty(PeopleNames.PEOPLE_LAST_NAME));
		accountParams.put("legal_entity.type", "individual");
		return accountParams;
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