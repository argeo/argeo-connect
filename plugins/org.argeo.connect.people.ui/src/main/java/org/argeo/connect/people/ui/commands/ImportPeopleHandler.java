package org.argeo.connect.people.ui.commands;

import javax.jcr.Repository;

import org.argeo.connect.people.CsvPeopleImporter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ImportPeopleHandler extends AbstractHandler {
	private Repository repository;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String url = "file://";

		CsvPeopleImporter peopleImporter = new CsvPeopleImporter();
		peopleImporter.setUrl(url);
		peopleImporter.setRepository(repository);

		peopleImporter.run();
		return null;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
