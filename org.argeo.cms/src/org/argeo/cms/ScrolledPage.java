package org.argeo.cms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * A composite that can be scrolled vertically. It wraps a
 * {@link ScrolledComposite} (and is being wrapped by it), simplifying its
 * configuration.
 */
public class ScrolledPage extends Composite {
	private static final long serialVersionUID = 1593536965663574437L;

	private ScrolledComposite scrolledComposite;

	public ScrolledPage(Composite parent, int style) {
		super(new ScrolledComposite(parent, SWT.V_SCROLL), style);
		scrolledComposite = (ScrolledComposite) getParent();
		scrolledComposite.setContent(this);

		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		scrolledComposite.addControlListener(new ScrollControlListener());
	}

	@Override
	public void layout(boolean changed, boolean all) {
		updateScroll();
		super.layout(changed, all);
	}

	protected void updateScroll() {
		Rectangle r = scrolledComposite.getClientArea();
		Point preferredSize = computeSize(r.width, SWT.DEFAULT);
		scrolledComposite.setMinHeight(preferredSize.y);
	}

	protected ScrolledComposite getScrolledComposite() {
		return this.scrolledComposite;
	}

	private class ScrollControlListener extends
			org.eclipse.swt.events.ControlAdapter {
		private static final long serialVersionUID = -3586986238567483316L;

		public void controlResized(ControlEvent e) {
			updateScroll();
		}
	}
}
