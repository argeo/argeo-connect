package org.argeo.connect.people.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;

import org.argeo.connect.people.LabelService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.utils.CommonsJcrUtils;

/** Concrete access to People's LabelServices */
public class LabelServiceImpl implements LabelService {

	@SuppressWarnings("unused")
	private PeopleService peopleService;

	public LabelServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	@Override
	public String getItemDefaultEnLabel(String itemName) {
		return null;
	}

	@Override
	public String getItemLabel(String itemName, String langIso) {
		return null;
	}

	@Override
	public String[] getDefinedValues(Node node, String propertyName) {
		return null;
	}

	@Override
	public Map<String, String> getDefinedValueMap(Node node, String propertyName) {
		return null;
	}

	@Override
	public List<String> getValueList(Session session, String basePath,
			String filter) {
		return getValueList(session, NodeType.MIX_TITLE, basePath, filter);
	}

	@Override
	public List<String> getValueList(Session session, String nodeType,
			String basePath, String filter) {
		List<String> values = new ArrayList<String>();
		try {
			String queryStr = "select * from [" + nodeType
					+ "] as nodes where ISDESCENDANTNODE('" + basePath + "')";
			if (CommonsJcrUtils.checkNotEmptyString(filter))
				queryStr += " AND LOWER(nodes.[" + Property.JCR_TITLE
						+ "]) like \"%" + filter.toLowerCase() + "%\"";
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

	@Override
	public List<String> getInstancePropCatalog(Session session,
			String resourcePath, String propertyName, String filter) {
		List<String> result = new ArrayList<String>();
		try {
			if (session.nodeExists(resourcePath)) {
				Node node = session.getNode(resourcePath);
				if (node.hasProperty(propertyName)) {
					Value[] values = node.getProperty(propertyName).getValues();
					for (Value value : values) {
						String curr = value.getString();
						if (CommonsJcrUtils.isEmptyString(filter)
								|| CommonsJcrUtils.checkNotEmptyString(curr)
								&& curr.toLowerCase().contains(
										filter.toLowerCase()))
							result.add(curr);
					}
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get " + propertyName
					+ " values for node at path " + resourcePath, re);
		}

		return result;
	}
}
