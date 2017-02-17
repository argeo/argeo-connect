package org.argeo.connect.people;

import org.argeo.naming.LdapAttrs;
import org.argeo.node.NodeConstants;

/** Resources specific roles used in the code */
public enum PeopleRole {
	editor, reader;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(PeopleConstants.PEOPLE_APP_PREFIX).append(".")
				.append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
