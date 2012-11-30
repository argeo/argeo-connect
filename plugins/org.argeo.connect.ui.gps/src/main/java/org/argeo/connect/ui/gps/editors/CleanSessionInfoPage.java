/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.ui.gps.editors;

import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.commands.OpenNewRepoWizard;
import org.argeo.connect.ui.gps.commons.ModifiedFieldListener;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.handlers.IHandlerService;

/** Manages all metadata corresponding to the current clean data session */
public class CleanSessionInfoPage extends AbstractCleanDataEditorPage {
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);
	// local variables
	public final static String ID = "cleanDataEditor.metaDataPage";

	private HashMap<String, String> repos;
	// Current page widgets
	private Text sessionDisplayName;
	private Text sessionDescription;
	private Combo localRepoCombo;

	private FormToolkit tk;

	public CleanSessionInfoPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		tk = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		form.getBody().setLayout(new TableWrapLayout());

		createFields(form.getBody());
	}

	private Section createFields(Composite parent) {
		GridData gd;

		// clean session metadata
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(ConnectGpsUiPlugin
				.getGPSMessage(ConnectGpsLabels.METADATA_SECTION_TITLE));

		Composite body = tk.createComposite(section, SWT.WRAP);
		section.setClient(body);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 3;
		body.setLayout(layout);

		// Session display name
		Label label = tk.createLabel(body, ConnectGpsUiPlugin
				.getGPSMessage(ConnectGpsLabels.PARAM_SET_LABEL_LBL));
		sessionDisplayName = new Text(body, SWT.FILL | SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		gd.widthHint = 330;
		sessionDisplayName.setLayoutData(gd);
		if (getJcrStringValue(Property.JCR_TITLE) != null)
			sessionDisplayName.setText(getJcrStringValue(Property.JCR_TITLE));
		else
			try {
				sessionDisplayName.setText(getEditor().getCurrentCleanSession()
						.getName());
			} catch (RepositoryException re) {
			} // Silent

		// Local repository name
		label = new Label(body, SWT.NONE);
		label.setText("Choose a local repository:");
		localRepoCombo = new Combo(body, SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gd.widthHint = 200;
		localRepoCombo.setLayoutData(gd);

		populateRepoCombo(localRepoCombo);

		// Add new repository button
		Button execute = new Button(body, SWT.PUSH | SWT.LEFT);
		execute.setText("Add new repository");
		gd = new GridData();
		gd.horizontalAlignment = SWT.LEFT;
		execute.setLayoutData(gd);

		Listener executeListener = new Listener() {
			public void handleEvent(Event event) {
				callNewRepoCommand();
				populateRepoCombo(localRepoCombo);
			}
		};
		execute.addListener(SWT.Selection, executeListener);

		// Session description
		label = tk.createLabel(body, ConnectGpsUiPlugin
				.getGPSMessage(ConnectGpsLabels.PARAM_SET_COMMENTS_LBL));
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		label.setLayoutData(gd);
		sessionDescription = tk.createText(body, null, SWT.BORDER
				| SWT.V_SCROLL | SWT.WRAP);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gd.heightHint = 100;
		gd.widthHint = 310;
		gd.horizontalSpan = 2;
		sessionDescription.setLayoutData(gd);
		if (getJcrStringValue(Property.JCR_DESCRIPTION) != null)
			sessionDescription
					.setText(getJcrStringValue(Property.JCR_DESCRIPTION));

		AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				if (onSave)
					try {
						Node currentSessionNode = getEditor()
								.getCurrentCleanSession();
						currentSessionNode.setProperty(Property.JCR_TITLE,
								sessionDisplayName.getText());
						currentSessionNode.setProperty(
								Property.JCR_DESCRIPTION,
								sessionDescription.getText());
						// prevent error thrown if user hasn't selected any
						// repo.
						if (localRepoCombo.getSelectionIndex() >= 0) {
							// TODO enhance. Works because repo list is usually
							// small
							String tmpStr = localRepoCombo
									.getItem(localRepoCombo.getSelectionIndex());
							if (tmpStr != null)
								for (String key : repos.keySet()) {
									if (tmpStr.equals(repos.get(key))) {
										currentSessionNode
												.setProperty(
														ConnectNames.CONNECT_LOCAL_REPO_NAME,
														key);
										break;
									}
								}
						}
						super.commit(onSave);
					} catch (RepositoryException re) {
						throw new ArgeoException(
								"Error while trying to persist Meta Data for Session",
								re);
					}
				else if (log.isTraceEnabled())
					log.debug("commit(false)");
			}
		};
		sessionDisplayName.addModifyListener(new ModifiedFieldListener(part));
		sessionDescription.addModifyListener(new ModifiedFieldListener(part));
		localRepoCombo.addSelectionListener(new ComboListener(part));
		getManagedForm().addPart(part);
		return section;
	}

	// Fill with existing values :
	private String getJcrStringValue(String jcrPropertyName) {
		String value = null;
		try {
			Node curNode = getEditor().getCurrentCleanSession();
			if (curNode.hasProperty(jcrPropertyName))
				value = curNode.getProperty(jcrPropertyName).getString();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error while getting persisted value for property"
							+ jcrPropertyName, re);
		}
		return value;
	}

	// fill the combo
	private void populateRepoCombo(Combo combo) {
		repos = new HashMap<String, String>();
		try {
			Node parNode = getEditor().getUiJcrServices()
					.getLocalRepositoriesParentNode();
			NodeIterator ni = parNode.getNodes();
			while (ni.hasNext()) {
				Node curNode = ni.nextNode();
				if (curNode.isNodeType(ConnectTypes.CONNECT_LOCAL_REPOSITORY)) {
					repos.put(curNode.getName(),
							curNode.getProperty(Property.JCR_TITLE).getString());
				}
			}
			combo.removeAll();
			for (String key : repos.keySet()) {
				combo.add(repos.get(key));
			}

			// Initialize to persisted repo name
			Node sessionNode = getEditor().getCurrentCleanSession();
			if (sessionNode.hasProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)) {
				String tmp = sessionNode
						.getProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
						.getString().trim();
				if (repos.containsKey(tmp)) {
					combo.select(combo.indexOf(repos.get(tmp)));
				} else
					ErrorFeedback
							.show("Session is linked to a absent local repository");
			}
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error wile populating comboo list", re);
		}
	}

	private void callNewRepoCommand() {
		try {
			IWorkbench iw = ConnectGpsUiPlugin.getDefault().getWorkbench();
			IHandlerService handlerService = (IHandlerService) iw
					.getService(IHandlerService.class);
			// get the command from plugin.xml
			IWorkbenchWindow window = iw.getActiveWorkbenchWindow();
			ICommandService cmdService = (ICommandService) window
					.getService(ICommandService.class);
			Command cmd = cmdService.getCommand(OpenNewRepoWizard.ID);
			// build the parameterized command
			ParameterizedCommand pc = new ParameterizedCommand(cmd, null);
			// execute the command
			handlerService = (IHandlerService) window
					.getService(IHandlerService.class);
			handlerService.executeCommand(pc, null);
		} catch (Exception e) {
			throw new ArgeoException("Unexpected error wile calling the "
					+ "'Open new repository wizard' command", e);
		}
	}


	// Inner classes
	private class ComboListener implements SelectionListener {
		private AbstractFormPart part;

		public ComboListener(AbstractFormPart part) {
			this.part = part;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			part.markDirty();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// Do nothing
		}
	}
}
