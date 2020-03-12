package org.argeo.connect.resources;

import org.argeo.api.NodeConstants;
import org.argeo.naming.Distinguished;
import org.argeo.naming.LdapAttrs;

/** Resources specific roles used in the code */
public enum ResourcesRole implements Distinguished {
	editor, reader;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(ResourcesConstants.RESOURCES_APP_PREFIX)
				.append(".").append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
