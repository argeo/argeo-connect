package org.argeo.people.core.imports;

import static org.argeo.connect.util.ConnectUtils.notEmpty;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.UserAdminService;
import org.argeo.connect.core.UserAdminServiceImpl;
import org.argeo.connect.util.ConnectUtils;
import org.argeo.naming.LdapAttrs;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Base utility to load users demo data **/
public class UsersCsvFileParser extends CsvParserWithLinesAsMap {
	private final static Log log = LogFactory.getLog(UsersCsvFileParser.class);

	private final UserAdminServiceImpl userAdminService;

	public UsersCsvFileParser(UserAdminService userAdminService) {
		this.userAdminService = (UserAdminServiceImpl) userAdminService;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		String userName = line.get("username");
		String firstName = line.get("firstName");
		String lastName = line.get("lastName");
		String email = line.get("email");
		String pwd = line.get("password");
		String desc = line.get("description");

		User existingUser = userAdminService.getUserFromLocalId(userName);
		if (existingUser != null) {
			log.warn("User " + userName
					+ " already exists in the system, skipping line");
			return;
		}
		String dn = userAdminService.buildDefaultDN(userName, Role.USER);
		User user = (User) userAdminService.getUserAdmin().createRole(dn,
				Role.USER);

		Dictionary props = user.getProperties();

		if (notEmpty(lastName))
			props.put(LdapAttrs.sn.name(), lastName);

		if (notEmpty(firstName))
			props.put(LdapAttrs.givenName.name(), firstName);

		String cn = (firstName.trim() + " " + lastName.trim() + " ").trim();
		if (notEmpty(cn))
			props.put(LdapAttrs.cn.name(), cn);

		if (notEmpty(email))
			props.put(LdapAttrs.mail.name(), email);

		if (notEmpty(desc))
			props.put(LdapAttrs.description.name(), desc);
		char[] password = null;
		if (notEmpty(pwd))
			password = pwd.toCharArray();
		else
			password = "demo".toCharArray();
		user.getCredentials().put(null, password);
	}
}
