/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.people.rap.commands;

public class OpenPeopleUserEditor {}
//
//import org.argeo.connect.people.rap.PeopleRapPlugin;
//import org.argeo.connect.people.rap.editors.UserEditor;
//import org.argeo.security.ui.admin.editors.ArgeoUserEditorInput;
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.handlers.HandlerUtil;
//
///** Command handler to set visible or open a People specific user editor. */
//public class OpenPeopleUserEditor extends AbstractHandler {
//	public final static String ID = PeopleRapPlugin.PLUGIN_ID
//			+ ".openPeopleUserEditor";
//	public final static String PARAM_USERNAME = "param.username";
//
//	public Object execute(ExecutionEvent event) throws ExecutionException {
//		try {
//			ArgeoUserEditorInput editorInput = new ArgeoUserEditorInput(
//					event.getParameter(PARAM_USERNAME));
//			IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(
//					event).getActivePage();
//			activePage.openEditor(editorInput, UserEditor.ID);
//		} catch (Exception e) {
//			throw new ExecutionException("Cannot open editor", e);
//		}
//		return null;
//	}
//}
