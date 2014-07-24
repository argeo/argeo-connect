package org.argeo.connect.people.ui.editors;

/**
 * Search the repository with a given entity type
 */
@Deprecated
public class SearchByTagEditor {
}
// extends AbstractSearchEntityEditor {

// public final static String ID = PeopleUiPlugin.PLUGIN_ID
// + ".searchByTagEditor";
//
// private SimpleResourceDropDown tagDD;
// private Button goBtn;
//
// // Default column
// private List<PeopleColumnDefinition> colDefs = new
// ArrayList<PeopleColumnDefinition>();
// {
// colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
// Property.JCR_TITLE, PropertyType.STRING, "Display Name",
// new JcrRowHtmlLabelProvider(PeopleTypes.PEOPLE_ENTITY,
// Property.JCR_TITLE), 300));
// colDefs.add(new PeopleColumnDefinition(PeopleTypes.PEOPLE_ENTITY,
// PEOPLE_TAGS, PropertyType.STRING, "Tags",
// new JcrRowHtmlLabelProvider(PeopleTypes.PEOPLE_ENTITY,
// PEOPLE_TAGS), 300));
// };
//
// @Override
// public void init(IEditorSite site, IEditorInput input)
// throws PartInitException {
// super.init(site, input);
// }
//
// public void setTagValue(String tag) {
// tagDD.reset(tag);
// refreshStaticFilteredList();
// // TODO solve the drop down problem when setting the text
// goBtn.setFocus();
// }
//
// @Override
// protected boolean queryOnCreation() {
// // the refresh will be done by the method above once the tag value has
// // been set.
// return false;
// }
//
// @Override
// public List<PeopleColumnDefinition> getColumnDefinition(String extractId)
// {
// return colDefs;
// }
//
// /** Override this to provide type specific staic filters */
// protected void populateStaticFilters(Composite body) {
//
// body.setLayout(new GridLayout(3, false));
//
// Text tagTxt = createBoldLT(body, "List entities for tag", "",
// "Select from list to find entities that are categorised with this tag");
// tagDD = new SimpleResourceDropDown(getPeopleUiService(), getSession(),
// getPeopleService().getResourceBasePath(
// PeopleConstants.RESOURCE_TAG), tagTxt);
//
// goBtn = new Button(body, SWT.PUSH);
// goBtn.setText("Refresh list");
// goBtn.addSelectionListener(new SelectionAdapter() {
// private static final long serialVersionUID = 1L;
//
// @Override
// public void widgetSelected(SelectionEvent e) {
// refreshStaticFilteredList();
// }
// });
// }
//
// /** Refresh the table viewer based on the free text search field */
// protected void refreshStaticFilteredList() {
// try {
// Session session = getSession();
// QueryManager queryManager = session.getWorkspace()
// .getQueryManager();
// QueryObjectModelFactory factory = queryManager.getQOMFactory();
// Selector source = factory
// .selector(getEntityType(), getEntityType());
//
// Constraint defaultC = getFreeTextConstraint(factory, source);
//
// // Tag
// String currVal = tagDD.getText();
// if (CommonsJcrUtils.checkNotEmptyString(currVal)) {
// StaticOperand so = factory.literal(session.getValueFactory()
// .createValue(currVal));
// DynamicOperand dyo = factory.propertyValue(
// source.getSelectorName(), PEOPLE_TAGS);
// Constraint currC = factory.comparison(dyo,
// QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);
// defaultC = PeopleUiUtils.localAnd(factory, defaultC, currC);
// }
//
// // TODO handle the case where no TITLE prop is available
// Ordering order = factory.ascending(factory.propertyValue(
// source.getSelectorName(), Property.JCR_TITLE));
// Ordering[] orderings = { order };
// QueryObjectModel query = factory.createQuery(source, defaultC,
// orderings, null);
// QueryResult result = query.execute();
// Row[] rows = CommonsJcrUtils.rowIteratorToArray(result.getRows());
// setViewerInput(rows);
//
// } catch (RepositoryException e) {
// throw new PeopleException("Unable to list " + getEntityType()
// + " entities with static filter ", e);
// }
// }
// }