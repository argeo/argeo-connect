package org.argeo.connect.workbench.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Implementation of the IManagedForm that relies on a Form rather than on a
 * scroll form in order to workaround scrolling issues in complex layouts,
 * especially when tables with many lines are displayed.
 */
public class CompositeManagedForm implements IManagedForm {

	private Object input;

	private Object container;

	private FormToolkit toolkit;

	private boolean initialized = false;

	private List<IFormPart> parts = new ArrayList<IFormPart>();

	private Composite composite;

	/**
	 * Creates a managed form that will use the provided toolkit
	 * 
	 * @param toolkit
	 */
	public CompositeManagedForm(Composite composite, FormToolkit toolkit) {
		this.composite = composite;
		this.toolkit = toolkit;
	}

	@Override
	public void addPart(IFormPart part) {
		parts.add(part);
	}

	@Override
	public void removePart(IFormPart part) {
		parts.remove(part);
	}

	@Override
	public IFormPart[] getParts() {
		return parts.toArray(new IFormPart[parts.size()]);
	}

	@Override
	public FormToolkit getToolkit() {
		return toolkit;
	}

	public ScrolledForm getForm() {
		throw new UnsupportedOperationException(
				"Unsupported method: we use a Form rather than a ScrolledForm");
	}

	@Override
	public IMessageManager getMessageManager() {
		throw new UnsupportedOperationException(
				"Unsupported method: we use a Form rather than a ScrolledForm");
	}

	/** Here is the magic */
	@Override
	public void reflow(boolean changed) {
		composite.layout(changed);
	}

	@Override
	public void fireSelectionChanged(IFormPart part, ISelection selection) {
		for (IFormPart currpart : parts) {
			if (part.equals(currpart))
				continue;
			if (currpart instanceof IPartSelectionListener) {
				((IPartSelectionListener) currpart).selectionChanged(part,
						selection);
			}
		}
	}

	@Override
	public void initialize() {
		if (initialized)
			return;
		for (IFormPart part : parts) {
			part.initialize(this);
		}
		initialized = true;
	}

	/**
	 * Disposes all the parts in this form.
	 */
	public void dispose() {
		for (int i = 0; i < parts.size(); i++) {
			IFormPart part = (IFormPart) parts.get(i);
			part.dispose();
		}
		// toolkit is always provided
		// if (ownsToolkit) {
		// toolkit.dispose();
		// }
	}

	@Override
	public void refresh() {
		Thread t = Thread.currentThread();
		Display display = composite.getDisplay();
		Thread dt = display.getThread();
		if (t.equals(dt))
			doRefresh();
		else {
			display.asyncExec(new Runnable() {
				public void run() {
					doRefresh();
				}
			});
		}
	}

	private void doRefresh() {
		int nrefreshed = 0;
		for (IFormPart part : parts) {
			if (part.isStale()) {
				part.refresh();
				nrefreshed++;
			}
		}
		if (nrefreshed > 0)
			reflow(true);
	}

	@Override
	public void commit(boolean onSave) {
		for (IFormPart part : parts) {
			if (part.isDirty())
				part.commit(onSave);
		}
	}

	@Override
	public boolean setInput(Object input) {
		boolean pageResult = false;

		this.input = input;
		for (IFormPart part : parts) {
			boolean result = part.setFormInput(input);
			if (result)
				pageResult = true;
		}
		return pageResult;
	}

	@Override
	public Object getInput() {
		return input;
	}

	/**
	 * Transfers the focus to the first form part.
	 */
	public void setFocus() {
		if (parts.size() > 0) {
			IFormPart part = (IFormPart) parts.get(0);
			part.setFocus();
		}
	}

	@Override
	public boolean isDirty() {
		for (IFormPart part : parts) {
			if (part.isDirty())
				return true;
		}
		return false;
	}

	@Override
	public boolean isStale() {
		for (IFormPart part : parts) {
			if (part.isStale())
				return true;
		}
		return false;
	}

	/**
	 * Overwrite to call the corresponding fireproperty change on the correct
	 * workbench part
	 */
	@Override
	public void dirtyStateChanged() {
	}

	@Override
	public void staleStateChanged() {
		// // TODO enhance stale state management: workaround to refresh active
		// // part when stale state change
		// for (IFormPart part : parts) {
		// if (part.isStale()) {
		// part.refresh();
		// }
		// }
	}

	@Override
	public Object getContainer() {
		return container;
	}

	@Override
	public void setContainer(Object container) {
		this.container = container;
	}
}
