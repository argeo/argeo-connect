package org.argeo.photo.manager.ui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.Bundle;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PhotoManagerPerspective.ID;
	}

	/** see http://wiki.eclipse.org/index.php/Common_Navigator_Framework */
//	public IAdaptable getDefaultPageInput() {
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		return workspace.getRoot();
//	}

	/** see http://wiki.eclipse.org/index.php/Common_Navigator_Framework */
	public void initialize(IWorkbenchConfigurer configurer) {

//		IDE.registerAdapters();
//		
//		final String ICONS_PATH = "icons/full/";
//		final String PATH_OBJECT = ICONS_PATH + "obj16/";
//		Bundle ideBundle = Platform.getBundle("org.eclipse.ui.ide");
//		declareWorkbenchImage(configurer, ideBundle,
//				IDE.SharedImages.IMG_OBJ_PROJECT, PATH_OBJECT + "prj_obj.gif",
//				true);
//		declareWorkbenchImage(configurer, ideBundle,
//				IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT
//						+ "cprj_obj.gif", true);

	}

	/** see http://wiki.eclipse.org/index.php/Common_Navigator_Framework */
	private void declareWorkbenchImage(IWorkbenchConfigurer configurer_p,
			Bundle ideBundle, String symbolicName, String path, boolean shared) {
		URL url = ideBundle.getEntry(path);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		configurer_p.declareImage(symbolicName, desc, shared);
	}
}
