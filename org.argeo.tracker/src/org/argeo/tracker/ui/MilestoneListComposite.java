package org.argeo.tracker.ui;

import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import javax.jcr.Node;
import javax.jcr.Property;

import org.argeo.cms.ui.workbench.util.CommandUtils;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerNames;
import org.argeo.tracker.core.TrackerUtils;
import org.argeo.tracker.internal.ui.controls.CategoryOverviewChart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/** Canonical task list composite */
public class MilestoneListComposite extends Composite {
	private static final long serialVersionUID = 7277540413496825697L;

	private final AppWorkbenchService workbenchService;
	private final Node project;

	public MilestoneListComposite(Composite parent, int style, AppWorkbenchService appWorkbenchService, Node project) {
		super(parent, style);
		this.workbenchService = appWorkbenchService;
		this.project = project;
		GridLayout layout = EclipseUiUtils.noSpaceGridLayout();
		layout.verticalSpacing = 5;
		this.setLayout(layout);
	}

	public void setInput(Node[] milestones) {
		CmsUtils.clear(this);

		for (Node currMS : milestones)
			appendMilestoneCmp(this, currMS);

		this.layout(true, true);
	}

	private void appendMilestoneCmp(Composite parent, Node milestone) {
		String currTitle = ConnectJcrUtils.get(milestone, Property.JCR_TITLE);
		String currId = ConnectJcrUtils.get(milestone, ConnectNames.CONNECT_UID);
		int totalNb = (int) TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_MILESTONE_UID, currId).getSize();
		int openNb = (int) TrackerUtils.getIssues(project, null, TrackerNames.TRACKER_MILESTONE_UID, currId, true)
				.getSize();
		int closeNb = totalNb - openNb;

		if (totalNb <= 0)
			return;

		Composite boxCmp = new Composite(parent, SWT.NO_FOCUS | SWT.BORDER); //
		boxCmp.setLayoutData(EclipseUiUtils.fillWidth());

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		boxCmp.setLayout(layout);

		Link titleLk = new Link(boxCmp, SWT.WRAP);
		titleLk.setLayoutData(new TableWrapData(FILL_GRAB));
		titleLk.setFont(EclipseUiUtils.getBoldFont(boxCmp));
		titleLk.setText("<a>" + currTitle + "</a>");
		titleLk.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 5342086098924045174L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String jcrId = ConnectJcrUtils.getIdentifier(milestone);
				CommandUtils.callCommand(workbenchService.getOpenEntityEditorCmdId(), ConnectEditor.PARAM_JCR_ID,
						jcrId);
			}
		});

		Composite chartCmp = new Composite(boxCmp, SWT.NO_FOCUS);
		TableWrapData twd = new TableWrapData();
		twd.rowspan = 3;
		twd.heightHint = 40;
		twd.valign = TableWrapData.CENTER;
		chartCmp.setLayoutData(twd);
		chartCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());

		CategoryOverviewChart coc = new CategoryOverviewChart(chartCmp, SWT.NO_FOCUS);
		coc.setInput(currTitle, closeNb, totalNb);
		coc.setLayoutData(new GridData(310, 40));
		coc.layout(true, true);

		Label datesLbl = new Label(boxCmp, SWT.WRAP);

		String ddVal = ConnectJcrUtils.getDateFormattedAsString(milestone, TrackerNames.TRACKER_TARGET_DATE,
				ConnectConstants.DEFAULT_DATE_FORMAT);
		if (EclipseUiUtils.isEmpty(ddVal)) {
			datesLbl.setText("No due date defined");
			datesLbl.setFont(EclipseUiUtils.getItalicFont(boxCmp));
		} else
			datesLbl.setText("Due date: " + ddVal);

		Label descLbl = new Label(boxCmp, SWT.WRAP);
		String desc = ConnectJcrUtils.get(milestone, Property.JCR_DESCRIPTION);
		if (EclipseUiUtils.isEmpty(desc))
			descLbl.setText("-");
		else
			descLbl.setText(desc);
	}
}
