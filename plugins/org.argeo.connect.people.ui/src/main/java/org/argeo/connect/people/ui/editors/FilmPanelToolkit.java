package org.argeo.connect.people.ui.editors;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralize the creation of the different editors panels for films.
 */
public class FilmPanelToolkit {
	private final static Log log = LogFactory.getLog(FilmPanelToolkit.class);

	public static void populateSynopsisPanel(Composite panel,
			final Node entity, FormToolkit toolkit, final IManagedForm form) {
		try {
			panel.setLayout(new org.eclipse.swt.layout.GridLayout());
			// Original synopsis
			toolkit.createLabel(panel, "German synopsis: ", SWT.NONE);
			final Text synopsisTxt = toolkit.createText(panel, "", SWT.BORDER
					| SWT.MULTI | SWT.WRAP);
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.widthHint = 200;
			gd.heightHint = 120;
			synopsisTxt.setLayoutData(gd);
			Node origSynopsisNode = FilmJcrUtils.getSynopsisNode(entity, "de");
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
			enSynopsisTxt.setData("LinkedNode", enSynopsisNode.getPath());

			final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
				public void refresh() {
					super.refresh();
					try {
						String path = (String) enSynopsisTxt
								.getData("LinkedNode");
						Node enSynNode = entity.getSession().getNode(path);
						if (enSynNode != null) {
							String syn = JcrUtils.get(enSynNode,
									FilmNames.SYNOPSIS_CONTENT);
							if (!CommonsJcrUtils.isEmptyString(syn))
								enSynopsisTxt.setText(syn);
						}
						path = (String) synopsisTxt.getData("LinkedNode");
						Node origSynNode = entity.getSession().getNode(path);
						if (origSynNode != null) {
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

			form.addPart(editPart);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to create " + "synopsis panel", e);
		}
	}
}