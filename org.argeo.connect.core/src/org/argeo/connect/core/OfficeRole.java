package org.argeo.connect.core;

import org.argeo.api.NodeConstants;
import org.argeo.naming.Distinguished;
import org.argeo.naming.LdapAttrs;

/** Office specific roles used in the code */
public enum OfficeRole implements Distinguished {
	coworker, manager;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(OfficeConstants.SUITE_APP_PREFIX).append(".")
				.append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
