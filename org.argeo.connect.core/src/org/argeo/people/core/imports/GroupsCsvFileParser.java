package org.argeo.people.core.imports;

import static org.argeo.connect.util.ConnectUtils.notEmpty;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsUserManager;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.naming.LdapAttrs;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Parse a CSV file and create corresponding groups. A UserTransaction must
 * exists and it is the caller duty to commit it afterwards
 */
public class GroupsCsvFileParser extends CsvParserWithLinesAsMap {
	private final static Log log = LogFactory.getLog(GroupsCsvFileParser.class);

	private final CmsUserManager userAdminService;

	public GroupsCsvFileParser(CmsUserManager userAdminService) {
		this.userAdminService = userAdminService;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {

		String cn = line.get("commonName");
		String desc = line.get("description");
		String memberStr = line.get("members");

		User existingUser = userAdminService.getUserFromLocalId(cn);
		Group group = null;
		if (existingUser != null) {
			log.warn("Group " + cn + " already exists in the system, check if some user must yet be added");
			group = (Group) existingUser;
		} else {
			String dn = userAdminService.buildDefaultDN(cn, Role.GROUP);
			group = (Group) userAdminService.getUserAdmin().createRole(dn, Role.GROUP);
			Dictionary props = group.getProperties();
			if (notEmpty(desc))
				props.put(LdapAttrs.description.name(), desc);
		}

		String[] members = ConnectJcrUtils.parseAndClean(memberStr, ",", true);
		for (String member : members) {
			User user = userAdminService.getUserFromLocalId(member);
			if (user != null)
				group.addMember(user);
			else
				log.warn("Found no role for localId: " + member + ", cannot add to " + cn);
		}
	}
}
