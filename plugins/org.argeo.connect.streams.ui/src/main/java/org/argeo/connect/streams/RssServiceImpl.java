package org.argeo.connect.streams;

public class RssServiceImpl implements RssService {

	// SyndEntry syndEntry = (SyndEntry) ((IStructuredSelection) event
	// .getSelection()).getFirstElement();
	// try {
	// PlatformUI
	// .getWorkbench()
	// .getActiveWorkbenchWindow()
	// .getActivePage()
	// .openEditor(
	// new LinkEditorInput(feedsNode, syndEntry),
	// linkEditorId);
	// } catch (PartInitException e) {
	// throw new ArgeoException("Cannot init part", e);
	// }

	// public Object[] getElements(Object inputElement) {
	// try {
	// URL feedUrl = new URL(inputElement.toString());
	//
	// SyndFeedInput input = new SyndFeedInput();
	// SyndFeed feed = input.build(new XmlReader(feedUrl));
	// List<SyndEntry> entries = new ArrayList<SyndEntry>();
	// entries: for (SyndEntry syndEntry : (List<SyndEntry>) feed
	// .getEntries()) {
	// if (syndEntry.getTitle() == null
	// || syndEntry.getTitle().trim().equals(""))
	// continue entries;
	// entries.add(syndEntry);
	// }
	//
	// return entries.toArray();
	// } catch (Exception e) {
	// throw new ArgeoException("Cannot read feed", e);
	// }
	// }

	//
	// SyndEntry entry = lei.getSyndEntry();
	// Calendar publishedDate = new GregorianCalendar();
	// publishedDate.setTime(entry.getPublishedDate());
	// linkNode = JcrUtils.mkdirs(session, linkPath);
	// linkNode.addMixin(ConnectTypes.CONNECT_SYND_ENTRY);
	// linkNode.setProperty(ArgeoNames.ARGEO_URI, url);
	// linkNode.setProperty(Property.JCR_TITLE, entry.getTitle());
	// linkNode.setProperty(Property.JCR_DESCRIPTION, entry
	// .getDescription().getValue());
	// linkNode.setProperty(
	// ConnectNames.CONNECT_AUTHOR,
	// (String[]) entry.getAuthors().toArray(
	// new String[entry.getAuthors().size()]));
	// linkNode.setProperty(ConnectNames.CONNECT_PUBLISHED_DATE,
	// publishedDate);
	// linkNode.setProperty(ConnectNames.CONNECT_UPDATED_DATE,
	// publishedDate);

}
