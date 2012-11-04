package org.argeo.connect.demo.gr.ui.wizards;

import java.io.ByteArrayInputStream;

import javax.jcr.Binary;
import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.eclipse.ui.specific.GenericUploadControl;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


public class UploadFileWizardPage extends WizardPage implements ModifyListener,
		GrConstants {
	private final static Log log = LogFactory
			.getLog(UploadFileWizardPage.class);

	// parent
	private UploadFileWizard wizard;

	// Main business objects
	private GrBackend grBackend;
	private Node currentNode;

	// current page fields to be filled in by the end user
	private Combo documentTypes;

	private GenericUploadControl uploadFileControl;

	public UploadFileWizardPage(String pageName) {
		super(pageName);
	}

	public void createControl(Composite parent) {
		// initialise business objects
		wizard = (UploadFileWizard) getWizard();
		grBackend = wizard.getGrBackend();
		currentNode = wizard.getCurrentNode();

		// Set wizard description
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(GrUiPlugin
				.getMessage("uploadFileWizardGenericDescription"));
		strBuf.append(" [");
		try {
			strBuf.append(currentNode.getName());
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot get current node name.", re);
		}
		strBuf.append("].");
		setDescription(strBuf.toString());

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		populateLayout(composite);
		setControl(composite);
	}

	private void populateLayout(Composite body) {
		Label lbl;

		// Document Type
		// lbl = new Label(body, SWT.NONE);
		// lbl.setText(ClientUiPlugin.getMessage("documentTypeLbl"));
		//
		// documentTypes = new Combo(body, SWT.NONE);
		// List<String> documentTypesLst = wizard.getGrBackend().getSiteTypes();
		// Iterator<String> it = documentTypesLst.iterator();
		// while (it.hasNext()) {
		// documentTypes.add(it.next());
		// }

		// Document upload
		new Label(body, SWT.NONE).setText(GrUiPlugin
				.getMessage("chooseFileToImportLabel"));
		uploadFileControl = new GenericUploadControl(body, SWT.NONE,
				GrUiPlugin.getMessage("browseButtonLbl"));
		uploadFileControl.addModifyListener(this);
	}

	/**
	 * method called when a file is chosen because current class implements
	 * ModifyListener Interface. Refresh among other the 'next' button state.
	 */
	public void modifyText(ModifyEvent event) {
		setErrorMessage(null);
		getWizard().getContainer().updateButtons();
	}

	/**
	 * When coming back to this page due to an error
	 */
	public void onEnterPage(String errorMessage) {
		log.debug("On enter page");
		setErrorMessage(errorMessage);
	}

	// set properties filled in that page.
	boolean performFinish() {
		if (log.isDebugEnabled())
			log.debug("Starting real upload");

		// Upload
		byte[] curFile = uploadFileControl.performUpload();

		if (curFile == null || curFile.length == 0) {
			setErrorMessage(GrUiPlugin
					.getMessage("emptyFileCannotBeUploaded"));
			return false;
		} else {
			ByteArrayInputStream bis = new ByteArrayInputStream(curFile);
			String docName = uploadFileControl.getLastFileUploadedName();
			try {
				Node fileNode = currentNode.addNode(docName, NodeType.NT_FILE);
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
				setErrorMessage(GrUiPlugin.getMessage("existingFileError"));
				return false;
			} catch (Exception e) {
				throw new ArgeoException(
						"Unexpected error while creating new node file", e);
			}
			return true;
		}
	}
}
