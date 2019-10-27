package org.argeo.tracker;

import org.argeo.naming.Distinguished;
import org.argeo.naming.LdapAttrs;
import org.argeo.node.NodeConstants;

/** Resources specific roles used in the code */
public enum TrackerRole implements Distinguished {
	editor, reader;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(TrackerConstants.TRACKER_APP_PREFIX)
				.append(".").append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
