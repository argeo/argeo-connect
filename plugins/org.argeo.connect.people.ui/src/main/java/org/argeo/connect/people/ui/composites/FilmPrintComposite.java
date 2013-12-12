package org.argeo.connect.people.ui.composites;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple composite widget to display and edit information about a film print
 */
public class FilmPrintComposite extends Composite {
	private static final long serialVersionUID = -3303030374442774568L;

	private final Node filmPrint;
	private final FormToolkit toolkit;
	private final IManagedForm form;
	// Don't forget to unregister on dispose
	private AbstractFormPart formPart;

	public FilmPrintComposite(Composite parent, int style, FormToolkit toolkit,
			IManagedForm form, Node filmPrint) {
		super(parent, style);
		this.filmPrint = filmPrint;
		this.toolkit = toolkit;
		this.form = form;
		populate();
	}
	
//	@Override
//	public void pack(boolean changed) {
//		// super.pack(changed);
//	}

	private void populate() {
		// initialization
		Composite parent = this;
		parent.setLayout(new GridLayout(1, false));
		final Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout(2, false));

		// Main Info
		final Text titleTxt = createLT(group, "Name:", 1);
		final Text typeTxt = createLT(group, "Type:", 1);
		final Text descTxt = createLT(group, "Description:", 2);
		final Text formatTxt = createLT(group, "Format:", 1);
		final Text ratioTxt = createLT(group, "Ratio:", 1);
		final Text soundFormatTxt = createLT(group, "Sound format:", 1);
		final Text languageVersionTxt = createLT(group, "Language versions:", 1);
		final Text feeTxt = createLT(group, "Fee:", 1);
		final Text feeInfoTxt = createLT(group, "Fee additional information:", 1);
		final Text sourceTxt = createLT(group, "Source contact:", 1);
		final Text returnTxt = createLT(group, "Return contact:", 1);

		formPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();

				if (!titleTxt.isDisposed()) {

					String title = CommonsJcrUtils.get(filmPrint,
							Property.JCR_TITLE);

					if (CommonsJcrUtils.checkNotEmptyString(title))
						group.setText(title);
					else
						group.setText("< new >");

					PeopleUiUtils.refreshFormTextWidget(titleTxt, filmPrint,
							Property.JCR_TITLE);
					PeopleUiUtils.refreshFormTextWidget(typeTxt, filmPrint,
							FilmNames.PRINT_TYPE);
					PeopleUiUtils.refreshFormTextWidget(descTxt, filmPrint,
							Property.JCR_DESCRIPTION);
					PeopleUiUtils.refreshFormTextWidget(formatTxt, filmPrint,
							FilmNames.PRINT_FORMAT);
					PeopleUiUtils.refreshFormTextWidget(ratioTxt, filmPrint,
							FilmNames.PRINT_RATIO);
					PeopleUiUtils.refreshFormTextWidget(soundFormatTxt,
							filmPrint, FilmNames.PRINT_SOUND_FORMAT);
					PeopleUiUtils.refreshFormTextWidget(languageVersionTxt,
							filmPrint, FilmNames.PRINT_LANGUAGE_VERSION);
					PeopleUiUtils.refreshFormTextWidget(feeTxt, filmPrint,
							FilmNames.PRINT_FEE);
					PeopleUiUtils.refreshFormTextWidget(feeInfoTxt, filmPrint,
							FilmNames.PRINT_FEE_INFO);
					PeopleUiUtils.refreshFormTextWidget(sourceTxt, filmPrint,
							FilmNames.PRINT_SOURCE_CONTACT);
					PeopleUiUtils.refreshFormTextWidget(returnTxt, filmPrint,
							FilmNames.PRINT_RETURN_CONTACT);

				}
			}
		};

		formPart.refresh();

		PeopleUiUtils.addModifyListener(titleTxt, filmPrint,
				Property.JCR_TITLE, formPart);
		PeopleUiUtils.addModifyListener(typeTxt, filmPrint,
				FilmNames.PRINT_TYPE, formPart);
		PeopleUiUtils.addModifyListener(descTxt, filmPrint,
				Property.JCR_DESCRIPTION, formPart);
		PeopleUiUtils.addModifyListener(formatTxt, filmPrint,
				FilmNames.PRINT_FORMAT, formPart);
		PeopleUiUtils.addModifyListener(ratioTxt, filmPrint,
				FilmNames.PRINT_RATIO, formPart);
		PeopleUiUtils.addModifyListener(soundFormatTxt, filmPrint,
				FilmNames.PRINT_SOUND_FORMAT, formPart);
		PeopleUiUtils.addModifyListener(languageVersionTxt, filmPrint,
				FilmNames.PRINT_LANGUAGE_VERSION, formPart);
		PeopleUiUtils.addModifyListener(feeTxt, filmPrint, FilmNames.PRINT_FEE,
				formPart);
		PeopleUiUtils.addModifyListener(feeInfoTxt, filmPrint,
				FilmNames.PRINT_FEE_INFO, formPart);
		PeopleUiUtils.addModifyListener(sourceTxt, filmPrint,
				FilmNames.PRINT_SOURCE_CONTACT, formPart);
		PeopleUiUtils.addModifyListener(returnTxt, filmPrint,
				FilmNames.PRINT_RETURN_CONTACT, formPart);

		parent.layout();

		formPart.initialize(form);
		form.addPart(formPart);
	}

	@Override
	public boolean setFocus() {
		return true;
	}

	@Override
	public void dispose() {
		form.removePart(formPart);
		formPart.dispose();
		super.dispose();
	}

	private Text createLT(Composite parent, String label, int colspan) {
		Composite cmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		cmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, colspan,
				1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginWidth = gl.verticalSpacing = gl.marginHeight = 0;
		gl.horizontalSpacing = 5;
		cmp.setLayout(gl);
		toolkit.createLabel(cmp, label);
		Text txt = toolkit.createText(cmp, "", SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		return txt;
	}

}