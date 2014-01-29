package org.argeo.connect.people.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityCTabEditor;
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

	private FilmToolkit filmTk;
	private ListToolkit listTk;
	private EntityToolkit entityTk;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		film = getNode();

		String shortName = CommonsJcrUtils.get(film,
				FilmNames.FILM_CACHE_OTITLE);

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
				FilmNames.FILM_CACHE_OTITLE);
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
		filmTk = new FilmToolkit(toolkit, getManagedForm(), film,
				getPeopleService());
		entityTk = new EntityToolkit(toolkit, getManagedForm());
		listTk = new ListToolkit(toolkit, getManagedForm(), getPeopleService());

	}

	@Override
	protected void populateTabFolder(CTabFolder folder) {

		// Film info
		String tooltip = "Main information for film "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		Composite innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Main info.", PeopleUiConstants.PANEL_FILM_MAIN_INFO, tooltip);
		filmTk.populateFilmMainInfoPanel(innerPannel);

		// Film Details
		tooltip = "Additional information for film "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE,
				"Additional info.", PeopleUiConstants.PANEL_FILM_ADD_INFO,
				tooltip);
		filmTk.populateFilmAdditionalInfoPanel(innerPannel);

		// Synopses
		tooltip = "The synopses for film "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Synopsis",
				PeopleUiConstants.PANEL_SYNOPSES, tooltip);
		List<String> isoLangs = new ArrayList<String>();
		isoLangs.add(PeopleConstants.LANG_DE);
		isoLangs.add(PeopleConstants.LANG_EN);
		filmTk.populateSynopsisPanel(innerPannel, isoLangs);

		// film prints
		tooltip = "Registered film prints for "
				+ JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Film prints",
				PeopleUiConstants.PANEL_SYNOPSES, tooltip);
		filmTk.populateFilmPrintListCmp(innerPannel);

		// Crew
		tooltip = "Staff related to " + JcrUtils.get(film, FilmNames.FILM_ID);
		innerPannel = addTabToFolder(folder, CTAB_COMP_STYLE, "Crew",
				PeopleUiConstants.PANEL_MEMBERS, tooltip);
		TableViewer viewer = listTk.populateMembersPanel(innerPannel, film);

		viewer.addDoubleClickListener(new PeopleDoubleClickAdapter() {

			@Override
			protected void processDoubleClick(Object obj) {
				// Here we have PeopleMembers, we want to display linked
				// entities on double click
				if (obj instanceof Node) {
					Node link = (Node) obj;
					CommandUtils.callCommand(getOpenEntityEditorCmdId(),
							OpenEntityEditor.PARAM_ENTITY_UID, CommonsJcrUtils
									.get(link, PeopleNames.PEOPLE_REF_UID));
				}
			}
		});

	}

	@Override
	protected void populateHeader(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		Composite titleCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(titleCmp);

		Composite tagsCmp = toolkit.createComposite(parent, SWT.NO_FOCUS);
		tagsCmp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		entityTk.populateTagPanel(tagsCmp, film,
				PeopleNames.PEOPLE_TAGS, "Enter a new tag");
	}

	protected void populateTitleComposite(final Composite parent) {
		Label lbl;
		GridData gd;
		try {
			parent.setLayout(new FormLayout());

			// READ ONLY PANEL
			final Composite roPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			roPanelCmp.layout();
			PeopleUiUtils.setSwitchingFormData(roPanelCmp);
			roPanelCmp.setLayout(new GridLayout());
			final Label filmInfoROLbl = toolkit.createLabel(roPanelCmp, "",
					SWT.WRAP);
			filmInfoROLbl.setData(PeopleUiConstants.MARKUP_ENABLED,
					Boolean.TRUE);
			roPanelCmp.layout();

			final ColumnLabelProvider filmExtractLP = new FilmOverviewLabelProvider(
					false, getPeopleService());

			// EDIT PANEL
			final Composite editPanelCmp = toolkit.createComposite(parent,
					SWT.NO_FOCUS);
			PeopleUiUtils.setSwitchingFormData(editPanelCmp);
			editPanelCmp.setLayout(new GridLayout(4, false));

			editPanelCmp.layout();

			// Film Number
			// = toolkit.createLabel(editPanelCmp, "ID", SWT.NONE);
			// lbl.setLayoutData(new GridData());
			PeopleUiUtils.createBoldLabel(toolkit, editPanelCmp, "Film Number");
			final Text idTxt = toolkit.createText(editPanelCmp, "", SWT.BORDER
					| SWT.SINGLE | SWT.LEFT);
			// idTxt.setLayoutData(GridData.FILL_HORIZONTAL);

			editPanelCmp.layout();

			lbl = toolkit.createLabel(editPanelCmp, "");
			lbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
					false, 2, 1));

			editPanelCmp.layout();

			// Original Title
			PeopleUiUtils.createBoldLabel(toolkit, editPanelCmp,
					"Original Title");
			Composite titleCmp = toolkit.createComposite(editPanelCmp,
					SWT.NO_FOCUS);
			titleCmp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false, 3, 1));
			GridLayout layout = PeopleUiUtils.gridLayoutNoBorder(2);
			layout.horizontalSpacing = 5;
			titleCmp.setLayout(layout);

			final Text origTitleArticleTxt = toolkit.createText(titleCmp, "",
					SWT.BORDER);
			gd = new GridData();
			gd.widthHint = 100;
			origTitleArticleTxt.setLayoutData(gd);
			editPanelCmp.layout();

			final Text origTitleTxt = toolkit.createText(titleCmp, "",
					SWT.BORDER);
			origTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			// latin pronunciation & corresponding language
			PeopleUiUtils.createBoldLabel(toolkit, editPanelCmp,
					"Latin pronounciation");
			final Text latinTitleTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER);
			latinTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			PeopleUiUtils.createBoldLabel(toolkit, editPanelCmp,
					"Title Language");
			final Text titleLangTxt = toolkit.createText(editPanelCmp, "",
					SWT.BORDER);
			latinTitleTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL));

			final AbstractFormPart editPart = new AbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();
					boolean isCheckedOut = CommonsJcrUtils
							.isNodeCheckedOutByMe(film);
					if (isCheckedOut) {
						Node originalNode = FilmJcrUtils.getOriginalTitle(film);
						// EDIT PART
						PeopleUiUtils.refreshFormTextWidget(idTxt, film,
								FilmNames.FILM_ID);
						PeopleUiUtils.refreshFormTextWidget(
								origTitleArticleTxt, originalNode,
								FilmNames.FILM_TITLE_ARTICLE);
						PeopleUiUtils.refreshFormTextWidget(origTitleTxt,
								originalNode, FilmNames.FILM_TITLE_VALUE);
						PeopleUiUtils.refreshFormTextWidget(latinTitleTxt,
								originalNode,
								FilmNames.FILM_TITLE_LATIN_PRONUNCIATION);
						PeopleUiUtils.refreshFormTextWidget(titleLangTxt,
								originalNode, PeopleNames.PEOPLE_LANG);

						// TODO FIX THIS
						origTitleArticleTxt.setEnabled(false);
						origTitleTxt.setEnabled(false);
						latinTitleTxt.setEnabled(false);
						origTitleArticleTxt.setEnabled(false);
					}
					// READ ONLY PART
					String roText = filmExtractLP.getText(film);
					filmInfoROLbl.setText(roText);
					if (isCheckedOut)
						editPanelCmp.moveAbove(roPanelCmp);
					else
						editPanelCmp.moveBelow(roPanelCmp);
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
							FilmNames.FILM_CACHE_OTITLE, PropertyType.STRING,
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
							FilmNames.FILM_CACHE_OTITLE_LATIN,
							PropertyType.STRING, latinTitleTxt.getText()))
						editPart.markDirty();
				}
			});

			origTitleArticleTxt.addModifyListener(new ModifyListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void modifyText(ModifyEvent event) {
					if (JcrUiUtils.setJcrProperty(film,
							FilmNames.FILM_CACHE_OTITLE_ARTICLE,
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