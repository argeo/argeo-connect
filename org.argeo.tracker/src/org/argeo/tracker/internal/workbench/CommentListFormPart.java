package org.argeo.tracker.internal.workbench;

import static org.eclipse.ui.forms.widgets.TableWrapData.FILL_GRAB;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.activities.ActivitiesNames;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.tracker.TrackerException;
import org.argeo.tracker.TrackerService;
import org.argeo.tracker.TrackerTypes;
import org.argeo.tracker.internal.ui.TrackerLps;
import org.argeo.tracker.internal.ui.TrackerUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class CommentListFormPart extends Composite {
	private static final long serialVersionUID = -8671301499186962746L;

	private final IManagedForm managedForm;
	private final TrackerService trackerService;
	private final Node task;

	public CommentListFormPart(IManagedForm managedForm, Composite parent, int style, TrackerService trackerService,
			Node task) {
		super(parent, style);
		this.managedForm = managedForm;
		this.trackerService = trackerService;
		this.task = task;

		this.setLayout(new TableWrapLayout());
		Section section = createCommentSection(this);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
	}

	// THE COMMENT LIST
	private Section createCommentSection(Composite parent) {
		FormToolkit tk = managedForm.getToolkit();
		Section section = TrackerUiUtils.addFormSection(tk, parent, "Comments");

		Composite body = ((Composite) section.getClient());
		body.setLayout(new TableWrapLayout());

		Composite newCommentCmp = new Composite(body, SWT.NO_FOCUS);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		newCommentCmp.setLayout(layout);

		// Add a new comment fields
		final Text newCommentTxt = new Text(newCommentCmp, SWT.MULTI | SWT.WRAP | SWT.BORDER);
		TableWrapData twd = new TableWrapData(FILL_GRAB);
		newCommentTxt.setLayoutData(twd);

		newCommentTxt.setMessage("Enter a new comment...");
		newCommentTxt.addFocusListener(new FocusListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void focusLost(FocusEvent event) {
				String currText = newCommentTxt.getText();
				if (EclipseUiUtils.isEmpty(currText)) {
					TableWrapData twd = ((TableWrapData) newCommentTxt.getLayoutData());
					twd.heightHint = SWT.DEFAULT;
					newCommentTxt.getParent().layout(true, true);
					managedForm.reflow(true);
				}
			}

			@Override
			public void focusGained(FocusEvent event) {
				TableWrapData twd = ((TableWrapData) newCommentTxt.getLayoutData());
				twd.heightHint = 200;
				newCommentTxt.getParent().layout(true, true);
				managedForm.reflow(true);
			}
		});
		Button okBtn = new Button(newCommentCmp, SWT.BORDER | SWT.PUSH | SWT.BOTTOM);
		okBtn.setLayoutData(new TableWrapData(TableWrapData.CENTER, TableWrapData.TOP));
		okBtn.setText("OK");

		// Existing comment list
		final Composite commentsCmp = new Composite(body, SWT.NO_FOCUS);
		commentsCmp.setLayout(new TableWrapLayout());
		commentsCmp.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		SectionPart part = new SectionPart(section) {

			private ColumnLabelProvider lp = new TrackerLps().new IssueCommentOverviewLabelProvider();

			@Override
			public void refresh() {
				if (commentsCmp.isDisposed())
					return;
				CmsUtils.clear(commentsCmp);

				List<Node> comments = getComments();
				for (Node comment : comments)
					addCommentCmp(commentsCmp, lp, null, comment);

				parent.layout(true, true);
				super.refresh();
			}
		};
		part.initialize(managedForm);
		managedForm.addPart(part);

		okBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = -5295361445564398576L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String newTag = newCommentTxt.getText();
				if (EclipseUiUtils.notEmpty(newTag)) {
					Session tmpSession = null;
					try {
						// We use a new session that is saved
						String issuePath = task.getPath();
						tmpSession = task.getSession().getRepository().login();
						Node issueExt = tmpSession.getNode(issuePath);
						trackerService.addComment(issueExt, newTag);
						tmpSession.save();
						task.getSession().refresh(true);
					} catch (RepositoryException re) {
						throw new TrackerException("Unable to add comment " + newTag + " on " + task, re);
					} finally {
						JcrUtils.logoutQuietly(tmpSession);
					}
					part.refresh();
					// part.markDirty();
				}
				// Reset the "new comment" field
				newCommentTxt.setText("");
				// okBtn.setFocus();
				TableWrapData twd = ((TableWrapData) newCommentTxt.getLayoutData());
				twd.heightHint = SWT.DEFAULT;
				newCommentTxt.getParent().layout(true, true);
				managedForm.reflow(true);
			}
		});
		return section;
	}

	private List<Node> getComments() {
		List<Node> comments = new ArrayList<Node>();
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(XPathUtils.descendantFrom(task.getPath()));
			builder.append("//element(*, ");
			builder.append(TrackerTypes.TRACKER_COMMENT);
			builder.append(")");
			builder.append("order by @").append(ActivitiesNames.ACTIVITIES_ACTIVITY_DATE);
			builder.append(", @").append(Property.JCR_CREATED);
			builder.append(" descending");
			NodeIterator nit = XPathUtils.createQuery(task.getSession(), builder.toString()).execute().getNodes();
			while (nit.hasNext())
				comments.add(nit.nextNode());
		} catch (RepositoryException re) {
			throw new TrackerException("Unable retrieve comments for " + task, re);
		}
		return comments;
	}

	private void addCommentCmp(Composite parent, ColumnLabelProvider lp, AbstractFormPart formPart, Node comment) {
		// retrieve properties
		String description = ConnectJcrUtils.get(comment, Property.JCR_DESCRIPTION);

		Composite commentCmp = new Composite(parent, SWT.NO_FOCUS);
		commentCmp.setLayoutData(new TableWrapData(FILL_GRAB));
		commentCmp.setLayout(new TableWrapLayout());

		// First line
		Label overviewLabel = new Label(commentCmp, SWT.WRAP);
		overviewLabel.setLayoutData(new TableWrapData(FILL_GRAB));
		overviewLabel.setText(lp.getText(comment));
		overviewLabel.setFont(EclipseUiUtils.getBoldFont(parent));

		// Second line: description
		Label descLabel = new Label(commentCmp, SWT.WRAP);
		descLabel.setLayoutData(new TableWrapData(FILL_GRAB));
		descLabel.setText(description);

		// third line: separator
		Label sepLbl = new Label(commentCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		sepLbl.setLayoutData(new TableWrapData(FILL_GRAB));
	}
}
