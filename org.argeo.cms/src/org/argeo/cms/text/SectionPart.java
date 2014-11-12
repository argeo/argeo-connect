package org.argeo.cms.text;

import org.argeo.cms.CmsNames;

public interface SectionPart extends NodeTextPart, CmsNames {
	public String getNodeId();

	public Section getSection();
}
