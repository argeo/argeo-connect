package org.argeo.connect.demo.gr.ui.wizards;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.service.GrBackend;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.eclipse.jface.wizard.Wizard;


public class UploadFileWizard extends Wizard {
	private final static Log log = LogFactory.getLog(UploadFileWizard.class);

	// Main business objects
	private GrBackend grBackend;
	private Node currentNode;

	private UploadFileWizardPage uploadFileWizardPage;

	public UploadFileWizard(GrBackend grBackend, Node currentNode) {
		this.grBackend = grBackend;
		this.currentNode = currentNode;
		setWindowTitle(GrUiPlugin.getMessage("uploadFileWizardTitle"));
	}

	@Override
	public void addPages() {
		try {
			uploadFileWizardPage = new UploadFileWizardPage("unused title");
			addPage(uploadFileWizardPage);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add page to wizard ", e);
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

	// Package protected methods to expose business objects.
	GrBackend getGrBackend() {
		return grBackend;
	}

	Node getCurrentNode() {
		return currentNode;
	}
}
