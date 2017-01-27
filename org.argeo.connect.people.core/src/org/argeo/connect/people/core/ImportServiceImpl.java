package org.argeo.connect.people.core;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.argeo.connect.people.ImportService;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.util.ConnectJcrUtils;

/** Default implementation of the import service */
public class ImportServiceImpl implements ImportService, PeopleNames {

	private final PeopleService peopleService;

	public ImportServiceImpl(PeopleService peopleService) {
		this.peopleService = peopleService;
	}

	// Filtered properties
	private final List<String> TECHNICAL_PROPERTIES = asList("jcr:uuid",
			"jcr:baseVersion", "jcr:isCheckedOut", "jcr:predecessors",
			"jcr:frozenUuid", "jcr:versionHistory", "jcr:frozenPrimaryType",
			"jcr:primaryType", "jcr:mixinTypes", "jcr:created",
			"jcr:createdBy", "jcr:lastModified", "jcr:lastModifiedBy");

	private final List<String> TECHNICAL_NODES = asList("rep:policy");

	public void mergeNodes(Node masterNode, Node slaveNode)
			throws RepositoryException {
		// current nodes
		PropertyIterator pit = slaveNode.getProperties();
		props: while (pit.hasNext()) {
			Property currProp = pit.nextProperty();
			if (TECHNICAL_PROPERTIES.contains(currProp.getName()))
				continue props;

			Property masterCurrProp = null;
			if (masterNode.hasProperty(currProp.getName()))
				masterCurrProp = masterNode.getProperty(currProp.getName());
			mergeProperty(masterNode, masterCurrProp, currProp);
		}

		NodeIterator nit = slaveNode.getNodes();
		nodes: while (nit.hasNext()) {
			Node currNode = nit.nextNode();
			if (TECHNICAL_NODES.contains(currNode.getName()))
				continue nodes;
			Node masterCurrChild = null;
			// TODO: this skips the additional external IDs from same source as
			// external ID node name is the source ID
			if (masterNode.hasNode(currNode.getName()))
				masterCurrChild = masterNode.getNode(currNode.getName());
			else
				masterCurrChild = masterNode.addNode(currNode.getName(),
						currNode.getPrimaryNodeType().getName());
			mergeNodes(masterCurrChild, currNode);
		}

		// TODO merge mixin?
		if (slaveNode.hasProperty(PeopleNames.PEOPLE_UID))
			mergeInternalReferences(masterNode, slaveNode);

		if (slaveNode.isNodeType("mix:referenceable"))
			mergeJcrReferences(masterNode, slaveNode);
	}

	protected void mergeJcrReferences(Node masterNode, Node slaveNode)
			throws RepositoryException {
		PropertyIterator pit = slaveNode.getReferences();
		while (pit.hasNext()) {
			Property ref = pit.nextProperty();
			Node referencing = ref.getParent();
			// checkCOStatusBeforeUpdate(referencing);
			if (ref.isMultiple()) {
				ConnectJcrUtils.removeRefFromMultiValuedProp(referencing,
						ref.getName(), slaveNode.getIdentifier());
				ConnectJcrUtils.addRefToMultiValuedProp(referencing, ref.getName(),
						masterNode);
			} else
				referencing.setProperty(ref.getName(), masterNode);
		}
	}

	protected void mergeInternalReferences(Node masterNode, Node slaveNode)
			throws RepositoryException {
		NodeIterator nit = internalReferencing(slaveNode);
		String peopleUId = masterNode.getProperty(PEOPLE_UID).getString();
		while (nit.hasNext()) {
			Node referencing = nit.nextNode();
			// checkCOStatusBeforeUpdate(referencing);
			referencing.setProperty(PEOPLE_REF_UID, peopleUId);
		}
	}

	protected NodeIterator internalReferencing(Node slaveNode)
			throws RepositoryException {
		String peopleUId = slaveNode.getProperty(PEOPLE_UID).getString();
		QueryManager qm = slaveNode.getSession().getWorkspace()
				.getQueryManager();
		Query query = qm.createQuery(
				"select * from [nt:base] as nodes where ISDESCENDANTNODE('"
						+ peopleService.getBasePath(null) + "') AND ["
						+ PEOPLE_REF_UID + "]='" + peopleUId + "'" + " ",
				Query.JCR_SQL2);
		return query.execute().getNodes();
	}

	protected void mergeProperty(Node masterNode, Property masterProp,
			Property slaveProp) throws RepositoryException {
		if (slaveProp.isMultiple())
			mergeMultipleProperty(masterNode, masterProp, slaveProp);
		else if (masterProp == null) {
			masterNode.setProperty(slaveProp.getName(), slaveProp.getValue());
		}
		// TODO won't merge properties with empty values.
	}

	private void mergeMultipleProperty(Node masterNode, Property masterProp,
			Property slaveProp) throws RepositoryException {
		Value[] slaveVals = slaveProp.getValues();
		if (masterProp == null) {
			masterNode.setProperty(slaveProp.getName(), slaveVals);
		} else {
			Value[] vals = masterProp.getValues();
			if (vals[0].getType() == PropertyType.STRING) {
				List<String> res = new ArrayList<String>();
				for (Value val : vals)
					res.add(val.getString());
				for (Value val : slaveVals) {
					String currStr = val.getString();
					if (!res.contains(currStr))
						res.add(currStr);
				}
				masterProp.setValue(res.toArray(new String[0]));
			} else if (vals[0].getType() == PropertyType.REFERENCE) {
				List<String> res = new ArrayList<String>();
				for (Value val : vals)
					res.add(val.getString());
				for (Value val : slaveVals) {
					String currStr = val.getString();
					if (!res.contains(currStr))
						res.add(currStr);
				}
				ValueFactory vFactory = masterNode.getSession()
						.getValueFactory();
				int size = res.size();
				Value[] values = new Value[size];
				int i = 0;
				for (String id : res) {
					Value val = vFactory
							.createValue(id, PropertyType.REFERENCE);
					values[i++] = val;
				}
				masterNode.setProperty(slaveProp.getName(), values);
			} else {
				throw new PeopleException(
						"Unsupported multiple property type on property "
								+ masterProp + "for node " + masterNode);
			}
		}
	}
}