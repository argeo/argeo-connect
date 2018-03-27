package org.argeo.connect.core;

import org.argeo.naming.LdapAttrs;
import org.argeo.node.NodeConstants;

/** Office specific roles used in the code */
public enum OfficeRole {
	coworker, manager;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(OfficeConstants.SUITE_APP_PREFIX).append(".")
				.append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
