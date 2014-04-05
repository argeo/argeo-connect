package org.argeo.connect.people.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.commands.OpenSearchByTagEditor;
import org.argeo.connect.people.ui.commands.OpenSearchEntityEditor;
import org.argeo.connect.people.utils.CommonsJcrUtils;

/**
 * Centralize here the definition of context specific parameter (for instance
 * the name of the command to open editors so that it is easily extended by
 * specific extensions
 */
public class PeopleUiServiceImpl implements PeopleUiService {

	/**
	 * Provide the plugin specific ID of the {@code OpenEntityEditor} command
	 * and thus enable the opening plugin specific editors
	 */
	public String getOpenEntityEditorCmdId() {
		return OpenEntityEditor.ID;
	}

	@Override
	public String getOpenSearchEntityEditorCmdId() {
		return OpenSearchEntityEditor.ID;
	}

	@Override
	public String getOpenSearchByTagEditorCmdId() {
		return OpenSearchByTagEditor.ID;
	}

	@Override
	public String getOpenFileCmdId() {
		return "org.argeo.connect.people.ui.specific.openFile";
	}

	public List<String> getDefinedFilteredTags(Session session, String filter) {
		List<String> tags = new ArrayList<String>();
		try {
			Query query = session
					.getWorkspace()
					.getQueryManager()
					.createQuery(
							"select * from [" + NodeType.MIX_TITLE
									+ "] as tags where ISDESCENDANTNODE('"
									+ PeopleConstants.PEOPLE_TAGS_BASE_PATH
									+ "') AND tags.[" + Property.JCR_TITLE
									+ "] like '%" + filter
									+ "%' ORDER BY tags.[" + Property.JCR_TITLE
									+ "]", Query.JCR_SQL2);
			NodeIterator nit = query.execute().getNodes();

			while (nit.hasNext()) {
				Node curr = nit.nextNode();
				if (curr.hasProperty(Property.JCR_TITLE))
					tags.add(curr.getProperty(Property.JCR_TITLE).getString());
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get tags values for node ", re);
		}
		return tags;
	}

	public List<String> getValueList(Session session, String basePath,
			String filter) {
		return getValueList(session, NodeType.MIX_TITLE,
				basePath, filter);
	}

	public List<String> getValueList(Session session, String nodeType,
			String basePath, String filter) {
		List<String> values = new ArrayList<String>();
		try {
			String queryStr = "select * from [" + nodeType
					+ "] as nodes where ISDESCENDANTNODE('" + basePath + "')";
			if (CommonsJcrUtils.checkNotEmptyString(filter))
				queryStr += " AND nodes.[" + Property.JCR_TITLE + "] like '%"
						+ filter + "%'";
			queryStr += " ORDER BY nodes.[" + Property.JCR_TITLE + "]";

			Query query = session.getWorkspace().getQueryManager()
					.createQuery(queryStr, Query.JCR_SQL2);
			NodeIterator nit = query.execute().getNodes();

			while (nit.hasNext()) {
				Node curr = nit.nextNode();
				if (curr.hasProperty(Property.JCR_TITLE))
					values.add(curr.getProperty(Property.JCR_TITLE).getString());
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get tags values for node ", re);
		}
		return values;
	}
}
