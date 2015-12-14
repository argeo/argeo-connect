package org.argeo.connect.people.core.imports;

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.useradmin.UserAdminWrapper;
import org.argeo.connect.people.core.UserAdminServiceImpl;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.osgi.useradmin.LdifName;
import org.argeo.util.CsvParserWithLinesAsMap;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

/**
 * Parse csv file and create correponding groups. A UserTransaction must exists
 * and it is the caller duty to commit it afterwards
 */
public class GroupsCsvFileParser extends CsvParserWithLinesAsMap {
	private final static Log log = LogFactory.getLog(GroupsCsvFileParser.class);

	private final UserAdminServiceImpl userAdminWrapper;

	public GroupsCsvFileParser(UserAdminWrapper userAdminWrapper) {
		this.userAdminWrapper = (UserAdminServiceImpl) userAdminWrapper;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processLine(Integer lineNumber, Map<String, String> line) {

		String cn = line.get("commonName");
		String desc = line.get("description");
		String memberStr = line.get("members");

		User existingUser = userAdminWrapper.getUserFromLocalId(cn);
		if (existingUser != null) {
			log.warn("Group " + cn
					+ " already exists in the system, skipping line");
			return;
		}
		String dn = userAdminWrapper.buildDefaultDN(cn, Role.GROUP);
		Group group = (Group) userAdminWrapper.getUserAdmin().createRole(dn,
				Role.GROUP);

		Dictionary props = group.getProperties();
		if (EclipseUiUtils.notEmpty(desc))
			props.put(LdifName.description.name(), desc);

		String[] members = JcrUiUtils.parseAndClean(memberStr, ",", true);
		for (String member : members) {
			User user = userAdminWrapper.getUserFromLocalId(member);
			if (user != null)
				group.addMember(user);
			else
				log.warn("Found no role for localId: " + member
						+ ", cannot add to " + cn);
		}
	}
}