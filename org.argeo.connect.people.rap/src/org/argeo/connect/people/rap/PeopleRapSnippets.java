package org.argeo.connect.people.rap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.commands.DeleteEntity;
import org.argeo.connect.people.rap.commands.EditEntityReference;
import org.argeo.connect.people.rap.commands.EditEntityReferenceWithPosition;
import org.argeo.connect.people.rap.commands.EditJob;
import org.argeo.connect.people.rap.commands.OpenEntityEditor;
import org.argeo.connect.people.rap.commands.RemoveEntityReference;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;

/** Some helper methods to generate html snippets */
public class PeopleRapSnippets {

	/**
	 * Simply formats a couple href / label to display a link in a markup
	 * enabled tree / table / label that will trigger a corresponding RWT
	 * specific listener. Such a listener must be able to understand the
	 * specific format of the value of this href attribute
	 */
	public static String getRWTLink(String href, String value) {
		// TODO remove the hard coded fixed CSS style
		return "<a " + PeopleRapConstants.PEOPLE_STYLE_LINK + " href=\"" + href
				+ "\" target=\"_rwt\">" + value + "</a>";
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</command> from a cell of a HTML list
	 */
	public static String getRemoveReferenceSnippetForLists(Node currNode,
			Node parentVersionableNode) {
		String toRemoveJcrId = CommonsJcrUtils.getIdentifier(currNode);
		String versionableParJcrId = CommonsJcrUtils
				.getIdentifier(parentVersionableNode);
		String href = RemoveEntityReference.ID + "/"
				+ RemoveEntityReference.PARAM_VERSIONABLE_PARENT_JCR_ID + "="
				+ versionableParJcrId + "/"
				+ RemoveEntityReference.PARAM_TOREMOVE_JCR_ID + "="
				+ toRemoveJcrId;
		return getRWTLink(href, PeopleUiConstants.CRUD_DELETE);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>RemoveEntityReference</command> from a cell of a HTML list
	 */
	public static String getRemoveSnippetForLists(Node currNode,
			boolean removeParent) {
		String toRemoveJcrId = CommonsJcrUtils.getIdentifier(currNode);
		String href = DeleteEntity.ID + "/"
				+ DeleteEntity.PARAM_TOREMOVE_JCR_ID + "=" + toRemoveJcrId
				+ "/" + DeleteEntity.PARAM_REMOVE_ALSO_PARENT + "="
				+ removeParent;
		return getRWTLink(href, PeopleUiConstants.CRUD_DELETE);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditEntityReference</command> from a cell of a HTML list
	 */
	public static String getEditSnippetForLists(Node currNode,
			Node parentVersionableNode) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(currNode);
		String versionableParJcrId = CommonsJcrUtils
				.getIdentifier(parentVersionableNode);
		String href = EditEntityReference.ID + "/"
				+ EditEntityReference.PARAM_VERSIONABLE_PARENT_JCR_ID + "="
				+ versionableParJcrId + "/"
				+ EditEntityReference.PARAM_TOEDIT_JCR_ID + "=" + toEditJcrId;
		return getRWTLink(href, PeopleUiConstants.CRUD_EDIT);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditJob</code> command from a cell of a HTML list
	 */
	public static String getEditJobSnippetForLists(Node relevantNode,
			boolean isBackward) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(relevantNode);
		String href = EditJob.ID + "/" + EditJob.PUBLIC_RELEVANT_NODE_JCR_ID
				+ "=" + toEditJcrId + "/" + EditJob.PARAM_IS_BACKWARD + "="
				+ isBackward;
		return getRWTLink(href, PeopleUiConstants.CRUD_EDIT);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>EditEntityReferenceWithPosition</command> from a cell of a HTML list
	 */
	public static String getEditWithPosSnippetForLists(Node linkNode,
			boolean isBackward, String toSearchNodeType) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(linkNode);

		String href = EditEntityReferenceWithPosition.ID + "/"
				+ EditEntityReferenceWithPosition.PARAM_OLD_LINK_JCR_ID + "="
				+ toEditJcrId + "/"
				+ EditEntityReferenceWithPosition.PARAM_IS_BACKWARD + "="
				+ isBackward + "/"
				+ EditEntityReferenceWithPosition.PARAM_TO_SEARCH_NODE_TYPE
				+ "=" + toSearchNodeType;
		return getRWTLink(href, PeopleUiConstants.CRUD_EDIT);
	}

	/**
	 * Create the text value of a link that enable calling the
	 * <code>OpenEditor</code> command from a cell of a HTML list
	 */
	public static String getOpenEditorSnippet(String commandId,
			Node relevantNode, String value) {
		String toEditJcrId = CommonsJcrUtils.getIdentifier(relevantNode);
		String href = commandId + "/" + OpenEntityEditor.PARAM_JCR_ID + "="
				+ toEditJcrId;
		return getRWTLink(href, value);
	}

	/** a snippet to display tags that are linked to the current entity */
	public static String getTags(Node entity) {
		try {
			StringBuilder tags = new StringBuilder();
			if (entity.hasProperty(PeopleNames.PEOPLE_TAGS)) {
				for (Value value : entity
						.getProperty((PeopleNames.PEOPLE_TAGS)).getValues())
					tags.append("#")
							.append(PeopleUiUtils.replaceAmpersand(value
									.getString())).append(" ");
			}
			return PeopleUiUtils.replaceAmpersand(tags.toString());
		} catch (RepositoryException e) {
			throw new PeopleException("Error while getting tags for entity", e);
		}
	}
}