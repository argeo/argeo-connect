package org.argeo.connect.tracker.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.core.PeopleServiceImpl;
import org.argeo.connect.tracker.PeopleTrackerService;
import org.argeo.connect.tracker.TrackerNames;
import org.argeo.connect.tracker.TrackerService;
import org.argeo.connect.tracker.TrackerTypes;
import org.argeo.jcr.JcrUtils;

/** Default Tracker implementation of the people generic backend */
public class PeopleTrackerServiceImpl extends PeopleServiceImpl implements PeopleTrackerService, PeopleConstants {
	private final static Log log = LogFactory.getLog(PeopleTrackerServiceImpl.class);

	private TrackerService issueService = new TrackerServiceImpl(this);

	// TODO clean and generalize this
	private static final Map<String, String> BUSINESS_REL_PATHES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put(TrackerTypes.TRACKER_PROJECT, TrackerNames.TRACKER_PROJECTS);
		BUSINESS_REL_PATHES = Collections.unmodifiableMap(tmpMap);
	}

	@Override
	public String getBasePath(String entityType) {
		if (entityType == null)
			// FIXME we have to provide same base path as the AoService to be
			// able to retrieve resources
			return "/office";
		if (BUSINESS_REL_PATHES.containsKey(entityType))
			return getBasePath(null) + "/" + BUSINESS_REL_PATHES.get(entityType);
		else
			return super.getBasePath(entityType);
	}

	/** Creates various useful parent nodes if needed */
	@Override
	protected void initialiseModel(Session adminSession) throws RepositoryException {
		super.initialiseModel(adminSession);
		JcrUtils.mkdirs(adminSession, getBasePath(TrackerTypes.TRACKER_PROJECT));
		if (adminSession.hasPendingChanges()) {
			adminSession.save();
			log.info("Repository has been initialized " + "with Office's model");
		}
	}

	/* EXPOSES SERVICES */
	@Override
	public TrackerService getTrackerService() {
		return issueService;
	}
}
