package org.argeo.connect.people.rap.commands;

/**
 * Opens a dialog to edit an existing reference with position between two
 * entities.
 */
public class EditEntityReferenceWithPosition {
}
// extends AbstractHandler {
// public final static String ID = PeopleRapPlugin.PLUGIN_ID
// + ".editEntityReferenceWithPosition";
// public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleRapPlugin
// .getImageDescriptor("icons/add.png");
// public final static String DEFAULT_LABEL = "Add...";
// public final static String PARAM_OLD_LINK_JCR_ID = "param.oldLinkJcrId";
// public final static String PARAM_IS_BACKWARD = "param.isBackward";
// public final static String PARAM_TO_SEARCH_NODE_TYPE =
// "param.toSearchNodeType";
//
// /* DEPENDENCY INJECTION */
// private Repository repository;
// private PeopleService peopleService;
//
// public Object execute(final ExecutionEvent event) throws ExecutionException {
//
// String oldLinkJcrId = event.getParameter(PARAM_OLD_LINK_JCR_ID);
// boolean isBackward = new Boolean(event.getParameter(PARAM_IS_BACKWARD));
// String toSearchNodeType = event.getParameter(PARAM_TO_SEARCH_NODE_TYPE);
//
// Session session = null;
// try {
// session = repository.login();
// Node oldLinkNode = session.getNodeByIdentifier(oldLinkJcrId);
//
// Dialog diag = new EditEntityRefWithPositionDialog(
// HandlerUtil.getActiveShell(event), "Edit position",
// repository, peopleService, oldLinkNode, isBackward);
// int result = diag.open();
//
// if (result == Window.OK) {
// IEditorPart iep = HandlerUtil.getActiveWorkbenchWindow(event)
// .getActivePage().getActiveEditor();
// if (iep != null && iep instanceof AbstractEntityEditor)
// ((AbstractEntityEditor) iep).forceRefresh();
// }
// } catch (RepositoryException e) {
// throw new PeopleException("unexpected JCR error while opening "
// + "editor for newly created programm", e);
// } finally {
// JcrUtils.logoutQuietly(session);
// }
// return null;
// }
//
// /* DEPENDENCY INJECTION */
// public void setPeopleService(PeopleService peopleService) {
// this.peopleService = peopleService;
// }
//
// public void setRepository(Repository repository) {
// this.repository = repository;
// }
// }
