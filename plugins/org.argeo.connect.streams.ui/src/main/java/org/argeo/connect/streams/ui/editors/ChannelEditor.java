package org.argeo.connect.streams.ui.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.editors.AbstractEntityEditor;
import org.argeo.connect.people.ui.editors.EntityAbstractFormPart;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.ui.RssUiPlugin;
import org.argeo.connect.streams.ui.providers.RssListLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

// import org.eclipse.rap.rwt.RWT;

/**
 * Sample editor page that display reference controls and manage life cycle of a
 * given Node
 */
public class ChannelEditor extends AbstractEntityEditor implements RssNames {
	final static Log log = LogFactory.getLog(ChannelEditor.class);

	// local constants
	public final static String ID = RssUiPlugin.PLUGIN_ID + ".chanelEditor";
	// Main business Objects
	private Node channel;

	@Override
	protected void populateTabFolder(CTabFolder tabFolder) {
		// TODO Auto-generated method stub

	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		channel = getNode();
		setPartName(JcrUtils.getStringPropertyQuietly(channel,
				Property.JCR_TITLE));
	}

	@Override
	protected void populateMainInfoComposite(final Composite switchingPanel) {
		switchingPanel.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		readOnlyPanel.setLayout(new GridLayout());
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		final ILabelProvider personLP = new RssListLabelProvider(false);

		// EDIT
		final Composite editPanel = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		editPanel.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

		// intern layout
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		editPanel.setLayout(rl);

		// Create edit text
		final Text titleTxt = createText(editPanel, "Title", "Rss feed title",
				200);
		final Text websiteTxt = createText(editPanel, "Website",
				"This channel provider's website", 200);
		final Text descTxt = createText(editPanel, "A Description", "", 100);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				refreshTextValue(titleTxt, channel, Property.JCR_TITLE);
				refreshTextValue(websiteTxt, channel, RSS_LINK);
				refreshTextValue(descTxt, channel, Property.JCR_DESCRIPTION);

				// READ ONLY PART
				String roText = personLP.getText(channel);
				readOnlyInfoLbl.setText(roText);

				// Manage switch
				if (CommonsJcrUtils.isNodeCheckedOutByMe(channel))
					editPanel.moveAbove(readOnlyPanel);
				else
					editPanel.moveBelow(readOnlyPanel);
				editPanel.getParent().layout();
			}
		};

		// Listeners
		addTxtModifyListener(editPart, titleTxt, channel, Property.JCR_TITLE,
				PropertyType.STRING);
		addTxtModifyListener(editPart, titleTxt, channel, RSS_LINK,
				PropertyType.STRING);
		addTxtModifyListener(editPart, descTxt, channel,
				Property.JCR_DESCRIPTION, PropertyType.STRING);
		getManagedForm().addPart(editPart);
	}

	protected void addTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3940522217518729442L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(entity, propName, propType,
						text.getText()))
					part.markDirty();
			}
		});
	}

	protected void refreshTextValue(Text text, Node entity, String propName) {
		String tmpStr = CommonsJcrUtils.getStringValue(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
	}

	protected Text createText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

}