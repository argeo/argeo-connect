package org.argeo.connect.people.rap.commands;


/**
 * Opens a dialog to add a reference with position between two entities.
 */
@Deprecated
public class AddEntityReferenceWithPosition {}
// extends AbstractHandler {
// public final static String ID = PeopleRapPlugin.PLUGIN_ID
// + ".addEntityReferenceWithPosition";
// public final static ImageDescriptor DEFAULT_IMG_DESCRIPTOR = PeopleRapPlugin
// .getImageDescriptor("icons/add.png");
// public final static String DEFAULT_LABEL = "Add...";
// public final static String PARAM_REFERENCING_JCR_ID =
// "param.referencingJcrId";
// public final static String PARAM_REFERENCED_JCR_ID = "param.referencedJcrId";
// public final static String PARAM_TO_SEARCH_NODE_TYPE =
// "param.toSearchNodeType";
// public final static String PARAM_DIALOG_ID = "param.dialogId";
//
// /* DEPENDENCY INJECTION */
// private PeopleService peopleService;
// private Repository repository;
//
// public Object execute(final ExecutionEvent event) throws ExecutionException {
//
// String referencingJcrId = event.getParameter(PARAM_REFERENCING_JCR_ID);
// String referencedJcrId = event.getParameter(PARAM_REFERENCED_JCR_ID);
// String toSearchNodeType = event.getParameter(PARAM_TO_SEARCH_NODE_TYPE);
// String dialogId = event.getParameter(PARAM_DIALOG_ID);
//
// Session session = null;
// try {
// session = repository.login();
// Node referencing = null;
// if (referencingJcrId != null)
// referencing = session.getNodeByIdentifier(referencingJcrId);
//
// Node referenced = null;
// if (referencedJcrId != null)
// referenced = session.getNodeByIdentifier(referencedJcrId);
//
// Dialog diag = null;
//
// // if (PeopleUiConstants.DIALOG_ADD_ML_MEMBERS.equals(dialogId))
// // diag = new AddMLMembersDialog(
// // HandlerUtil.getActiveShell(event),
// // "Add Mailing List Members...", repository, referencing,
// // new String[] { toSearchNodeType });
// // else
// // if (PeopleUiConstants.DIALOG_ADD_ML_MEMBERSHIP
// // .equals(dialogId))
// // diag = new AddMLMembershipDialog(
// // HandlerUtil.getActiveShell(event),
// // "Add Mailing List membership", repository, referenced,
// // new String[] { toSearchNodeType });
// // else
// diag = new CreateEntityRefWithPositionDialog(
// HandlerUtil.getActiveShell(event), "Create position",
// repository, peopleService, referencing, referenced,
// toSearchNodeType);
//
// int result = diag.open();
//
// if (result == Dialog.OK) {
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
