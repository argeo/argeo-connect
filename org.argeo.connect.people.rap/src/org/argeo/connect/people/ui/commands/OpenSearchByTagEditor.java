package org.argeo.connect.people.ui.commands;


/**
 * Open an editor that display a table filtered by tag
 */
public class OpenSearchByTagEditor 
{}
// extends AbstractHandler {
// public final static String ID = PeopleUiPlugin.PLUGIN_ID
// + ".openSearchByTagEditor";
//
// public final static String PARAM_TAG_VALUE = "param.tagValue";
//
// public Object execute(ExecutionEvent event) throws ExecutionException {
//
// String tagValue = event.getParameter(PARAM_TAG_VALUE);
// try {
// SearchNodeEditorInput eei = new SearchNodeEditorInput(
// PeopleTypes.PEOPLE_ENTITY);
//
// IEditorPart part = HandlerUtil.getActiveWorkbenchWindow(event)
// .getActivePage().openEditor(eei, getEditorId());
//
// SearchByTagEditor editor = (SearchByTagEditor) part;
// if (editor != null)
// editor.setTagValue(tagValue == null ? "" : tagValue);
//
// } catch (PartInitException pie) {
// throw new PeopleException(
// "Unexpected PartInitException while opening entity editor",
// pie);
// }
// return null;
// }
//
// protected String getEditorId() {
// return SearchByTagEditor.ID;
// }
// }