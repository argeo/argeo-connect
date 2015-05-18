package org.argeo.connect.people.rap.editors.utils;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.io.IOUtils;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.rap.PeopleRapImages;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Slightly modifies AbstractPeopleEditor main layout adding a place for an
 * image on the left part of the header
 */
public abstract class AbstractPeopleWithImgEditor extends AbstractPeopleEditor {

	// A corresponding picture that must be explicitly disposed
	protected Image itemPicture = null;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);

		InputStream is = null;
		try {
			if (getNode().hasNode(PeopleNames.PEOPLE_PICTURE)) {
				Node imageNode = getNode().getNode(PeopleNames.PEOPLE_PICTURE)
						.getNode(Node.JCR_CONTENT);
				is = imageNode.getProperty(Property.JCR_DATA).getBinary()
						.getStream();
				itemPicture = new Image(this.getSite().getShell().getDisplay(),
						is);
			} else
				itemPicture = null;
		} catch (Exception e) {
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	protected void createMainLayout(Composite parent) {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		// Internal main Layout
		// The header
		Composite header = toolkit.createComposite(parent, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);

		GridLayout gl;
		if (displayImage())
			gl = PeopleUiUtils.noSpaceGridLayout(3);
		else
			gl = PeopleUiUtils.noSpaceGridLayout(2);

		// So that the buttons are not too close to the right border of the
		// composite.
		gl.marginRight = 5;
		header.setLayout(gl);
		header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		if (displayImage()) {
			// the image
			Composite imgCmp = toolkit.createComposite(header, SWT.NO_FOCUS
					| SWT.NO_SCROLL | SWT.NO_TRIM);
			imgCmp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			populateImagePart(imgCmp);
		}
		// header content
		Composite left = toolkit.createComposite(header, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		left.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateHeader(left);

		// header buttons
		Composite right = toolkit.createComposite(header, SWT.NO_FOCUS
				| SWT.NO_SCROLL | SWT.NO_TRIM);
		GridData gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.verticalIndent = 5;
		right.setLayoutData(gd);
		populateButtonsComposite(right);

		// the body
		Composite body = toolkit.createComposite(parent, SWT.NO_FOCUS);
		body.setLayout(EclipseUiUtils.noSpaceGridLayout());
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		populateBody(body);
	}

	protected void populateImagePart(Composite parent) {
		GridLayout gl = new GridLayout(2, false);
		gl.marginTop = gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		gl.marginWidth = 10;
		gl.marginBottom = 8;
		parent.setLayout(gl);

		Label image = toolkit.createLabel(parent, "", SWT.NO_FOCUS);
		image.setBackground(parent.getBackground());
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		if (getPicture() != null) {
			image.setImage(getPicture());
			gd.horizontalIndent = 5;
			gd.verticalIndent = 5;
		} else {
			gd.widthHint = 10;
		}
		image.setLayoutData(gd);

	}

	protected Image getPicture() {
		return itemPicture;
	}

	@Override
	public void dispose() {
		// Free the resources.
		if (itemPicture != null
				&& !itemPicture.equals(PeopleRapImages.NO_PICTURE))
			itemPicture.dispose();
		super.dispose();
	}

	/**
	 * Overwrite to display no image in the header
	 * 
	 * @return
	 */
	protected boolean displayImage() {
		return true;
	}
}
