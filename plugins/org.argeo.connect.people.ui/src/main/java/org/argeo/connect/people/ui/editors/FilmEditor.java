package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.listeners.PeopleDoubleClickAdapter;
import org.argeo.connect.people.ui.providers.FilmOverviewLabelProvider;
import org.argeo.connect.people.ui.toolkits.EntityToolkit;
import org.argeo.connect.people.ui.toolkits.FilmToolkit;
import org.argeo.connect.people.ui.toolkits.ListToolkit;
import org.argeo.connect.people.ui.utils.JcrUiUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.utils.PeopleJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Editor page that display a film with corresponding details
 */
public class FilmEditor extends AbstractEntityCTabEditor {
	final static Log log = LogFactory.getLog(FilmEditor.class);

	public final static String ID = PeopleUiPlugin.PLUGIN_ID + ".filmEditor";

	// Main business Objects
	private Node film;

	private FilmToolkit filmPanelToolkit;
	private ListToolkit listPanelToolkit;
	private EntityToolkit entityTk;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		film = getEntity();

		String shortName = CommonsJcrUtils.get(film,
				FilmNames.FILM_ORIGINAL_TITLE);

		if (CommonsJcrUtils.checkNotEmptyString(shortName)) {
			if (shortName.length() > SHORT_NAME_LENGHT)
				shortName = shortName.substring(0, SHORT_NAME_LENGHT - 1)
						+ "...";
			setPartName(shortName);
		}
	}

	@Override
	protected boolean canSave() {
		String displayName = CommonsJcrUtils.get(film,
				FilmNames.FILM_ORIGINAL_TITLE);
		if (displayName.length() < 2) {
			String msg = "Please note that you must define an original title"
					+ " that is at least 2 character long.";
			MessageDialog.openError(PeopleUiPlugin.getDefault().getWorkbench()
					.getDisplay().getActiveShell(), "Non-valid information",
					msg);

			return false;
		} else {
			PeopleJcrUtils.checkPathAndMoveIfNeeded(film,
					PeopleConstants.PEOPLE_BASE_PATH + "/"
							+ PeopleNames.PEOPLE_PERSONS);
			return true;
		}
	}

	@Override
	protected void createToolkits() {
		filmPanelToolkit = new FilmToolkit(toolkit, getManagedForm());
		entityTk = new EntityToolkit(toolkit, getManagedForm());
		listPanelToolkit = new ListToolkit(toolkit, getManagedForm(),
				getPeopleService());

	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {

		// Film Details
		String tooltip = "Details for film "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Details", PeopleUiConstants.PANEL_FILM_INFO, tooltip);
		filmPanelToolkit.populateFilmDetailsPanel(innerPannel, film);

		// Synopses
		tooltip = "The synopses for film "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Synopsis",
				PeopleUiConstants.PANEL_SYNOPSES, tooltip);
		List<String> isoLangs = new ArrayList<String>();
		isoLangs.add(PeopleConstants.LANG_DE); 
		isoLangs.add(PeopleConstants.LANG_EN);
		filmPanelToolkit.populateSynopsisPanel(innerPannel, film, isoLangs);

		// Crew
		tooltip = "Staff related to " + JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Crew",
				PeopleUiConstants.PANEL_MEMBERS, tooltip);
		TableViewer viewer = listPanelToolkit.populateMembersPanel(innerPannel,
				film);

		viewer.addDoubleClickListener(new PeopleDoubleClickAdapter() {

			@Override
			protected void processDoubleClick(Object obj) {
				// Here we have PeopleMembers, we want to display linked
				// entities on double click
				if (obj instanceof Node) {
					Node link = (Node) obj;
					CommandUtils.callCommand(getOpenEditorCommandId(),
							OpenEntityEditor.PARAM_ENTITY_UID, CommonsJcrUtils
									.get(link, PeopleNames.PEOPLE_REF_UID));
				}
			}
		});

	}

	@Override
	protected void populateMainInfoDetails(final Composite parent) {
		// Tag Management
		Composite tagsCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		entityTk.populateTagPanel(tagsCmp, film,
				PeopleNames.PEOPLE_ORG_BRANCHES, "Enter a new tag");

		// keep last update.
		super.populateMainInfoDetails(parent);
	}
	
	@Override
	protected void populateTitleComposite(final Composite parent) {
		try {
			parent.setLayout(new FormLayout());

			// READ ONLY PANEL
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
//			roPanelCmp.setData(RWT.CUSTOM_VARIANT,
//					PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

			roPanelCmp.setLayout(new GridLayout());

			// Add a label with info provided by the FilmOverviewLabelProvider
			final Label filmInfoROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			filmInfoROLbl.setData(PeopleUiConstants.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider filmExtractLP = new FilmOverviewLabelProvider(
					false, getPeopleService());

			// EDIT PANEL
			final Composite editPanelCmp = toolkit.createComposite(parent,
					SWT.NONE);
			PeopleUiUtils.setSwitchingFormData(editPanelCmp);
			// editPanelCmp.setData(RWT.CUSTOM_VARIANT,
			// PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
			editPanelCmp.setLayout(new GridLayout(4, false));

			// Film ID
			Label lbl = toolkit.createLabel(editPanelCmp, "ID", SWT.NONE);
			lbl.setLayoutData(new GridData());
			final Text idTxt = toolkit.createText(editPanelCmp, "", SWT.BORDER
					| SWT.SINGLE | SWT.LEFT);
			idTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			// Original Title
			lbl = toolkit.createLabel(editPanelCmp, "Original Title", SWT.NONE);
			final Text origTitleTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			origTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			// Original Title Article
			lbl = toolkit.createLabel(editPanelCmp, "Orig. Title Article",
					SWT.NONE);
			final Text origTitleArticleTxt = toolkit.createText(editPanelCmp,
					"", SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			origTitleArticleTxt.setLayoutData(new GridData(
					GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

			// latin Title
			lbl = toolkit.createLabel(editPanelCmp, "Latin pronounciation",
					SWT.NONE);
			final Text latinTitleTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER | SWT.SINGLE | SWT.LEFT);
			latinTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			final AbstractFormPart editPart = new AbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();

					// EDIT PART
					String filmBusinessID = JcrUtils.get(film,
							FilmNames.FILM_ID);

					if (CommonsJcrUtils.checkNotEmptyString(filmBusinessID))
						idTxt.setText(filmBusinessID);

					String latinTitle = JcrUtils.get(film,
							FilmNames.FILM_ORIG_LATIN_TITLE);
					if (CommonsJcrUtils.checkNotEmptyString(latinTitle))
						latinTitleTxt.setText(latinTitle);

					String origTitle = JcrUtils.get(film,
							FilmNames.FILM_ORIGINAL_TITLE);
					if (CommonsJcrUtils.checkNotEmptyString(origTitle))
						origTitleTxt.setText(origTitle);

					String origTitleArt = JcrUtils.get(film,
							FilmNames.FILM_ORIG_TITLE_ARTICLE);
					if (CommonsJcrUtils.checkNotEmptyString(origTitleArt))
						origTitleArticleTxt.setText(origTitleArt);

					// READ ONLY PART
					String roText = filmExtractLP.getText(film);
					filmInfoROLbl.setText(roText);

					try {
						if (film.isCheckedOut())
							editPanelCmp.moveAbove(roPanelCmp);
						else
							editPanelCmp.moveBelow(roPanelCmp);
					} catch (RepositoryException e) {
						throw new PeopleException(
								"Unable to get checked out status", e);
					}
					// editPanelCmp.getParent().layout();
					roPanelCmp.getParent().layout();
				}
			};

			// Listeners
			// FIXME implement clean management of display name.
			origTitleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_ORIGINAL_TITLE, PropertyType.STRING,
							origTitleTxt.getText())) {
						JcrUiUtils.setJcrProperty(film, Property.JCR_TITLE,
								PropertyType.STRING, origTitleTxt.getText());
						editPart.markDirty();
					}
				}
			});

			idTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film, FilmNames.FILM_ID,
							PropertyType.STRING, idTxt.getText()))
						editPart.markDirty();
				}
			});

			latinTitleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_ORIG_LATIN_TITLE,
							PropertyType.STRING, latinTitleTxt.getText()))
						editPart.markDirty();
				}
			});

			origTitleArticleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_ORIG_TITLE_ARTICLE,
							PropertyType.STRING, origTitleArticleTxt.getText()))
						editPart.markDirty();
				}
			});

			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			// } catch (RepositoryException e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}

}