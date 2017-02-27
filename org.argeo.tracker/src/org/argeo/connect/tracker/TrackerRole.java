package org.argeo.connect.tracker;

import org.argeo.naming.LdapAttrs;
import org.argeo.node.NodeConstants;

/** Resources specific roles used in the code */
public enum TrackerRole {
	editor, reader;

	public String dn() {
		return new StringBuilder(LdapAttrs.cn.name()).append("=").append(TrackerConstants.TRACKER_APP_PREFIX).append(".")
				.append(name()).append(",").append(NodeConstants.ROLES_BASEDN).toString();
	}
}
