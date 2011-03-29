package org.argeo.connect.ui.editors;

import javax.jcr.Node;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.sun.syndication.feed.synd.SyndEntry;

public class LinkEditorInput implements IEditorInput {
	private final String url;
	private final SyndEntry syndEntry;
	private final Node context;

	public LinkEditorInput(Node context, String url) {
		this.context = context;
		this.url = url;
		this.syndEntry = null;
	}

	public LinkEditorInput(Node context, SyndEntry syndEntry) {
		this.context = context;
		this.url = syndEntry.getLink();
		this.syndEntry = syndEntry;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return url;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return url;
	}

	public String getUrl() {
		return url;
	}

	public SyndEntry getSyndEntry() {
		return syndEntry;
	}

	public Node getContext() {
		return context;
	}

}
