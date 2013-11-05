package org.argeo.connect.people.ui.toolkits;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of the different editors panels for films.
 */
public class FilmToolkit extends EntityToolkit {
	private final static Log log = LogFactory.getLog(FilmToolkit.class);

	private final static String LANG_KEY = "lang";
	private final static String DEFAULT_LANG_KEY = "default";

	private final FormToolkit toolkit;
	private final IManagedForm form;

	public FilmToolkit(FormToolkit toolkit, IManagedForm form) {
		super(toolkit, form);
		// formToolkit
		// managedForm
		this.toolkit = toolkit;
		this.form = form;
	}

	/**
	 * 
	 * @param panel
	 * @param entity
	 * @param descLabel
	 *            typically synopsis pour a film or Description for a
	 *            multilingual description
	 * @param langs
	 *            an ordered list of the various languages that are to be
	 *            displayed, might comes must be first
	 */

	public void populateMultiLangDescPanel(Composite panel, final Node entity,
			String descLabel, List<String> langs) {
		final List<Text> texts = new ArrayList<Text>();

		int style = GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL;
		panel.setLayout(new GridLayout());
		// Original description
		toolkit.createLabel(panel, descLabel, SWT.NONE);
		Text descTxt = toolkit.createText(panel, "", SWT.BORDER | SWT.MULTI
				| SWT.WRAP);
		GridData gd = new GridData(style);
		descTxt.setLayoutData(gd);
		descTxt.setData(LANG_KEY, DEFAULT_LANG_KEY);
		texts.add(descTxt);

		// Alt description

		for (String lang : langs) {
			toolkit.createLabel(panel, descLabel + " " + lang.toUpperCase(),
					SWT.NONE);
			Text altDescTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			altDescTxt.setLayoutData(new GridData(style));
			altDescTxt.setData(LANG_KEY, lang);
			texts.add(altDescTxt);
		}

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				for (Text text : texts) {
					String key = (String) text.getData(LANG_KEY);
					if (DEFAULT_LANG_KEY.equals(key))
						PeopleUiUtils.refreshTextValue(text, entity,
								Property.JCR_DESCRIPTION);
					else {
						Node altDescNode = CommonsJcrUtils.getAltPropertyNode(
								entity, PeopleNames.PEOPLE_ALT_LANGS, key);
						if (altDescNode != null)
							PeopleUiUtils.refreshTextValue(text, altDescNode,
									Property.JCR_DESCRIPTION);
					}
					text.setEnabled(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
				}
			}
		};

		for (final Text text : texts) {
			final String currLang = (String) text.getData(LANG_KEY);
			if (DEFAULT_LANG_KEY.equals(currLang))
				PeopleUiUtils.addTxtModifyListener(editPart, text, entity,
						Property.JCR_DESCRIPTION, PropertyType.STRING);
			else {
				text.addModifyListener(new ModifyListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void modifyText(ModifyEvent event) {
						String altDesc = text.getText();
						if (CommonsJcrUtils.getAltPropertyNode(entity,
								PeopleNames.PEOPLE_ALT_LANGS, currLang) == null)
							if (CommonsJcrUtils.checkNotEmptyString(altDesc))
								CommonsJcrUtils.getOrCreateAltLanguageNode(
										entity, currLang);// create node
							else
								return;
						Node altTitle = CommonsJcrUtils.getAltPropertyNode(
								entity, PeopleNames.PEOPLE_ALT_LANGS, currLang);
						if (JcrUiUtils.setJcrProperty(altTitle,
								Property.JCR_DESCRIPTION, PropertyType.STRING,
								altDesc))
							editPart.markDirty();
					}
				});
			}
		}
		editPart.initialize(form);
		form.addPart(editPart);
	}

	//
	// public void populateDescPanel(Composite panel, final Node entity,
	// String descLabel, List<String> langs) {
	// try {
	// panel.setLayout(new GridLayout());
	//
	// for (String lang : langs) {
	// String langLabel = peopleSer
	//
	// toolkit.createLabel(panel, "German synopsis: ", SWT.NONE);
	// final Text synopsisTxt = toolkit.createText(panel, "", SWT.BORDER
	// | SWT.MULTI | SWT.WRAP);
	// GridData gd = new GridData(GridData.FILL_BOTH);
	// gd.widthHint = 200;
	// gd.heightHint = 120;
	// synopsisTxt.setLayoutData(gd);
	// Node origSynopsisNode = FilmJcrUtils.getSynopsisNode(entity, "de");
	// // force creation to avoid npe and ease form life cycle
	// // if (origSynopsisNode == null)
	// // origSynopsisNode = FilmJcrUtils.addOrUpdateSynopsisNode(entity,
	// // null, null, "de");
	// synopsisTxt.setData("LinkedNode", origSynopsisNode.getPath());
	//
	// }

	public void populateSynopsisPanel(Composite panel, final Node entity) {
		try {
			panel.setLayout(new GridLayout());
			// Original synopsis
			toolkit.createLabel(panel, "German synopsis: ", SWT.NONE);
			final Text synopsisTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.widthHint = 200;
			gd.heightHint = 120;
			synopsisTxt.setLayoutData(gd);
			Node origSynopsisNode = FilmJcrUtils.getSynopsisNode(entity, "de");
			// force creation to avoid npe and ease form life cycle
			if (origSynopsisNode == null)
				origSynopsisNode = FilmJcrUtils.addOrUpdateSynopsisNode(entity,
						null, null, PeopleConstants.LANG_DE);
			synopsisTxt.setData("LinkedNode", origSynopsisNode.getPath());

			// EN synopsis
			toolkit.createLabel(panel, "English synopsis: ", SWT.NONE);
			final Text enSynopsisTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			gd = new GridData(GridData.FILL_BOTH);
			gd.widthHint = 200;
			gd.heightHint = 120;
			enSynopsisTxt.setLayoutData(gd);
			Node enSynopsisNode = FilmJcrUtils.getSynopsisNode(entity, "en");
			// force creation to avoid npe and ease form life cycle
			if (enSynopsisNode == null)
				enSynopsisNode = FilmJcrUtils.addOrUpdateSynopsisNode(entity,
						null, null, PeopleConstants.LANG_EN);
			enSynopsisTxt.setData("LinkedNode", enSynopsisNode.getPath());

			final AbstractFormPart editPart = new AbstractFormPart() {
				public void refresh() {
					super.refresh();
					try {
						String path = (String) enSynopsisTxt
								.getData("LinkedNode");
						Session session = entity.getSession();
						if (session.nodeExists(path)) {
							Node enSynNode = session.getNode(path);
							String syn = JcrUtils.get(enSynNode,
									FilmNames.SYNOPSIS_CONTENT);
							if (!CommonsJcrUtils.isEmptyString(syn))
								enSynopsisTxt.setText(syn);
						}

						path = (String) synopsisTxt.getData("LinkedNode");
						if (session.nodeExists(path)) {
							Node origSynNode = entity.getSession()
									.getNode(path);
							String syn = JcrUtils.get(origSynNode,
									FilmNames.SYNOPSIS_CONTENT);
							if (!CommonsJcrUtils.isEmptyString(syn))
								synopsisTxt.setText(syn);
						} else
							log.debug("no node");

						boolean isCheckouted = entity.isCheckedOut();
						synopsisTxt.setEnabled(isCheckouted);
						enSynopsisTxt.setEnabled(isCheckouted);
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Cannot get synopsis node from widget", e);
					}
				}
			};

			synopsisTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					try {
						String path = (String) synopsisTxt
								.getData("LinkedNode");

						Node synopsisNode = entity.getSession().getNode(path);
						if (synopsisNode != null) {
							if (JcrUiUtils.setJcrProperty(synopsisNode,
									FilmNames.SYNOPSIS_CONTENT,
									PropertyType.STRING, synopsisTxt.getText()))
								editPart.markDirty();
						}
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Cannot get synopsis node from widget", e);
					}

				}
			});

			enSynopsisTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					try {
						String path = (String) enSynopsisTxt
								.getData("LinkedNode");
						Node enSynopsisNode = entity.getSession().getNode(path);
						if (enSynopsisNode != null) {
							if (JcrUiUtils.setJcrProperty(enSynopsisNode,
									FilmNames.SYNOPSIS_CONTENT,
									PropertyType.STRING,
									enSynopsisTxt.getText()))
								editPart.markDirty();
						}
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Cannot get synopsis node from widget", e);
					}
				}
			});

			editPart.initialize(form);
			form.addPart(editPart);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create " + "synopsis panel", e);
		}
	}
}