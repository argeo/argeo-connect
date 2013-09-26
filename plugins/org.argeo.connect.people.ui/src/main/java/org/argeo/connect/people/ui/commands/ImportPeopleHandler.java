package org.argeo.connect.people.ui.commands;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jcr.Repository;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.imports.CsvPeopleImporter;
import org.argeo.eclipse.ui.dialogs.SingleValue;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ImportPeopleHandler extends AbstractHandler {
	private Repository repository;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// String url = "file://";
		String base = PeopleConstants.PEOPLE_BASE_PATH;

		String url = SingleValue.ask("CSV URL", "CSV URL");
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			url = "file://" + url;
		}

		CsvPeopleImporter peopleImporter = new CsvPeopleImporter();
		peopleImporter.setUrl(url);
		peopleImporter.setBase(base);
		peopleImporter.setRepository(repository);

		peopleImporter.run();
		return null;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

}
