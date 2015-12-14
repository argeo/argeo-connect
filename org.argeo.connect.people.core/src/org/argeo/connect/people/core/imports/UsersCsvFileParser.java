package org.argeo.connect.people.core.imports;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.useradmin.UserAdminUtils;
import org.argeo.cms.util.useradmin.UserAdminWrapper;
import org.argeo.connect.people.core.UserAdminServiceImpl;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.osgi.useradmin.LdifName;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/** Base utility to load users demo data **/
public class UsersCsvFileParser extends CsvParserWithLinesAsMap {
	private final static Log log = LogFactory.getLog(UsersCsvFileParser.class);

	private final UserAdminServiceImpl userAdminWrapper;

	public UsersCsvFileParser(UserAdminWrapper userAdminWrapper) {
		this.userAdminWrapper = (UserAdminServiceImpl) userAdminWrapper;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {
		String userName = line.get("people:username");
		String firstName = line.get("people:firstName");
		String lastName = line.get("people:lastName");
		String email = line.get("people:email");
		String desc = line.get("people:description");

		User existingUser = userAdminWrapper.getUserFromLocalId(userName);
		if (existingUser != null){
			log.warn("User " + userName
					+ " already exists in the system, skipping line");
			return;
		}
		String dn = userAdminWrapper.buildDefaultDN(userName, Role.USER);
		User user = (User) userAdminWrapper.getUserAdmin().createRole(dn,
				Role.USER);

		Dictionary props = user.getProperties();

		if (EclipseUiUtils.notEmpty(lastName))
			props.put(LdifName.sn.name(), lastName);

		if (EclipseUiUtils.notEmpty(firstName))
			props.put(LdifName.givenName.name(), firstName);

		String cn = UserAdminUtils.buildDefaultCn(firstName, lastName);
		if (EclipseUiUtils.notEmpty(cn))
			props.put(LdifName.cn.name(), cn);

		if (EclipseUiUtils.notEmpty(email))
			props.put(LdifName.mail.name(), email);

		if (EclipseUiUtils.notEmpty(desc))
			props.put(LdifName.description.name(), desc);

		// TODO add the ability to define a common password for all imported
		// user before launching the import
		char[] password = "demo".toCharArray();
		user.getCredentials().put(null, password);

	}
}