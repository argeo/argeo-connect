package org.argeo.cms.text;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.cms.CmsUiProvider;
import org.argeo.cms.CmsUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Display the text of the context, and provide an editor if the user can edit. */
public class SimpleTextPage implements CmsUiProvider {
	private final static Log log = LogFactory.getLog(SimpleTextPage.class);

	private List<String> paragraphs = new ArrayList<String>();
	private Composite textCmp;
	private ScrolledComposite scrolledArea;
	private Text editor;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		
		Composite shell = parent;
		
		scrolledArea = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledArea.setExpandVertical(true);
		scrolledArea.setExpandHorizontal(true);
		scrolledArea
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		textCmp = new Composite(scrolledArea, SWT.NONE);
		textCmp.setLayout(CmsUtils.noSpaceGridLayout());
		scrolledArea.setContent(textCmp);

		// http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/CreateaScrolledCompositewithwrappingcontent.htm
		scrolledArea.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				updateScroll();
			}
		});

		// textCmp = parent;
		// textCmp = new Composite(parent, SWT.NONE);
		// textCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// textCmp.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_AREA);

		// RowLayout layout = new RowLayout();
		// layout.type = SWT.VERTICAL;
		// layout.fill = true;
		// layout.wrap = false;
		// layout.pack = false;

		refresh();
		return null;
	}

	protected void refresh() {
		if (textCmp == null)
			return;
		// clear
		for (Control child : textCmp.getChildren())
			child.dispose();

		for (String parag : paragraphs) {
			Label lbl = new Label(textCmp, SWT.LEAD | SWT.WRAP);
			lbl.setText(parag);
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			lbl.setData(RWT.MARKUP_ENABLED, true);
			lbl.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_DEFAULT);
		}

		editor = new Text(textCmp, SWT.MULTI | SWT.WRAP);
		GridData textLayoutData = new GridData(GridData.FILL, GridData.FILL,
				true, true);
		// textLayoutData.heightHint = 300;
		textLayoutData.minimumHeight = 200;
		editor.setLayoutData(textLayoutData);
		editor.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_EDITOR);
		// editor.addModifyListener(new ModifyListener() {
		//
		// @Override
		// public void modifyText(ModifyEvent event) {
		// if (log.isDebugEnabled())
		// log.debug(event.time + " " + editor.getText());
		// }
		// });
		editor.addKeyListener(new TextKeyListener());
		editor.setFocus();
		textCmp.layout(true, true);
		updateScroll();
	}

	protected void updateScroll() {
		Rectangle r = scrolledArea.getClientArea();
		Point preferredSize = textCmp.computeSize(r.width, SWT.DEFAULT);
		scrolledArea.setMinHeight(preferredSize.y);
	}

	private class TextKeyListener implements KeyListener {
		private static final long serialVersionUID = -7720848595910906899L;

		@Override
		public void keyPressed(KeyEvent e) {
			// if (log.isDebugEnabled())
			// log.debug(e.character);
			if (e.character == '\r') {
				try {
					@SuppressWarnings("unchecked")
					List<String> lines = IOUtils.readLines(new StringReader(
							editor.getText()));
					paragraphs.addAll(lines);
				} catch (IOException e1) {
					throw new ArgeoException("Cannot read " + editor.getText(),
							e1);
				}
				refresh();
			}

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// if (log.isDebugEnabled())
			// log.debug(e.time + " " + e);
		}

	}
}
