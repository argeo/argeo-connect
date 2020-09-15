package org.argeo.documents.e4.parts;

import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.documents.composites.DocumentsFolderComposite;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.Jcr;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISessionEvent;
import org.eclipse.rap.rwt.service.UISessionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** Default editor to display and edit a document folder */
public class FolderEditor extends AbstractDocumentsEditor {
	@PostConstruct
	public void createPartControl(Composite parent, EMenuService menuService) {
		init();
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		DocumentsFolderComposite dfc = new DocumentsFolderComposite(parent, SWT.NO_FOCUS, getNode(),
				getDocumentsService()) {
			private static final long serialVersionUID = 4880354934421639754L;

			@Override
			protected void externalNavigateTo(Path path) {
				// TODO rather directly use the jcrPath / an URI?
				Session session = ConnectJcrUtils.getSession(getNode());
				Node currNode = getDocumentsService().getNode(session.getRepository(), path);
				// TODO make it more portable
				RWT.getUISession().addUISessionListener(new UISessionListener() {
					private static final long serialVersionUID = 1L;

					public void beforeDestroy(UISessionEvent event) {
						Jcr.logout(Jcr.session(currNode));
					}
				});
				getSystemWorkbenchService().openEntityEditor(currNode);
			}
		};
		dfc.setLayoutData(EclipseUiUtils.fillAll());
		Path path = getDocumentsService().getPath(getNodeFileSystemProvider(), getNode());
		dfc.populate(path);
		parent.layout(true, true);
	}
}
