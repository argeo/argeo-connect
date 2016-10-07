package org.argeo.connect.people.core.imports;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.UserAdminService;
import org.argeo.connect.people.core.UserAdminServiceImpl;
import org.argeo.connect.people.util.UserAdminUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
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

		if (EclipseUiUtils.notEmpty(lastName))
			props.put(LdapAttrs.sn.name(), lastName);

		if (EclipseUiUtils.notEmpty(firstName))
			props.put(LdapAttrs.givenName.name(), firstName);

		String cn = UserAdminUtils.buildDefaultCn(firstName, lastName);
		if (EclipseUiUtils.notEmpty(cn))
			props.put(LdapAttrs.cn.name(), cn);

		if (EclipseUiUtils.notEmpty(email))
			props.put(LdapAttrs.mail.name(), email);

		if (EclipseUiUtils.notEmpty(desc))
			props.put(LdapAttrs.description.name(), desc);
		char[] password = null;
		if (EclipseUiUtils.notEmpty(pwd))
			password = pwd.toCharArray();
		else
			password = "demo".toCharArray();
		user.getCredentials().put(null, password);
	}
}