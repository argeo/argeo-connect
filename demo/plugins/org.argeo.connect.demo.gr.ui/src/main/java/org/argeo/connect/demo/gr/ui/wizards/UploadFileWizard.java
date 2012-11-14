package org.argeo.connect.demo.gr.ui.wizards;

import java.io.ByteArrayInputStream;

import javax.jcr.Binary;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.providers.GrNodeLabelProvider;
import org.argeo.eclipse.ui.specific.GenericUploadControl;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class UploadFileWizard extends Wizard {
	private final static Log log = LogFactory.getLog(UploadFileWizard.class);

	// Main business objects
	private Node currentNode;

	private UploadFileWizardPage uploadFileWizardPage;

	public UploadFileWizard(GrBackend grBackend, Node currentNode) {
		this.currentNode = currentNode;
		setWindowTitle(GrMessages.get().wizard_attachDoc_title);
	}

	@Override
	public void addPages() {
		try {
			addPage(new UploadFileWizardPage("unused title"));
		} catch (Exception e) {
			throw new GrException("Cannot add page to wizard ", e);
		}
	}

	/**
	 * Called when the user click on 'Finish' in the wizard. The real upload to
	 * the JCR repository is done here.
	 */
	@Override
	public boolean performFinish() {
		if (log.isTraceEnabled())
			log.debug("Wizard Perform finish");
		return uploadFileWizardPage.performFinish();
	}

	@Override
	public boolean performCancel() {
		return super.performCancel();
	}

	public boolean canFinish() {
		// TODO : check if the doc has been correctly updated
		return true;
	}

	private class UploadFileWizardPage extends WizardPage implements
			ModifyListener, GrConstants {

		private GenericUploadControl uploadFileControl;

		public UploadFileWizardPage(String pageName) {
			super(pageName);
		}

		public void createControl(Composite parent) {
			setDescription(NLS.bind(GrMessages.get().wizard_attachDoc_msg,
					GrNodeLabelProvider.getName(currentNode)));

			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			populateLayout(composite);
			setControl(composite);
		}

		private void populateLayout(Composite body) {
			// Document upload
			new Label(body, SWT.NONE)
					.setText(GrMessages.get().wizard_attachDoc_lbl);
			uploadFileControl = new GenericUploadControl(body, SWT.NONE,
					GrMessages.get().browseButtonLbl);
			uploadFileControl.addModifyListener(this);
		}

		/**
		 * method called when a file is chosen because current class implements
		 * ModifyListener Interface. Refresh among other the 'next' button
		 * state.
		 */
		public void modifyText(ModifyEvent event) {
			setErrorMessage(null);
			getWizard().getContainer().updateButtons();
		}

		// set properties filled in that page.
		boolean performFinish() {
			if (log.isDebugEnabled())
				log.debug("Starting real upload");

			// Upload
			byte[] curFile = uploadFileControl.performUpload();

			if (curFile == null || curFile.length == 0) {
				setErrorMessage(GrMessages.get().emptyFileCannotBeUploaded);
				return false;
			} else {
				ByteArrayInputStream bis = new ByteArrayInputStream(curFile);
				String docName = uploadFileControl.getLastFileUploadedName();
				try {
					Node fileNode = currentNode.addNode(docName,
							NodeType.NT_FILE);
					Node resNode = fileNode.addNode(Property.JCR_CONTENT,
							NodeType.NT_RESOURCE);
					Binary binary = null;
					try {
						binary = currentNode.getSession().getValueFactory()
								.createBinary(bis);
						resNode.setProperty(Property.JCR_DATA, binary);
					} finally {
						if (binary != null)
							binary.dispose();
						IOUtils.closeQuietly(bis);
					}
				} catch (ItemExistsException iee) {
					setErrorMessage(GrMessages.get().existingFileError);
					return false;
				} catch (Exception e) {
					throw new GrException(
							"Unexpected error while creating new node file", e);
				}
				return true;
			}
		}

	}
}