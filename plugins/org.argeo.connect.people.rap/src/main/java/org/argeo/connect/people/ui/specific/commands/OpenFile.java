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
package org.argeo.connect.people.ui.specific.commands;

import org.argeo.connect.people.rap.PeopleRapPlugin;

/**
 * Rap specific handler to open a file stored in the server file system, among
 * other tmp files created for exports.
 * 
 * TODO must be refactorised with new download files approach.
 * 
 */
public class OpenFile { // extends org.argeo.eclipse.ui.specific.OpenFile

	// NOTE: A DownloadFsFileService must be created via the plugin.xml
	// and its id must be injected in param downloadServiceHandlerId

	public final static String ID = PeopleRapPlugin.PLUGIN_ID + ".openFile";
}