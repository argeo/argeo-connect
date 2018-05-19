package org.argeo.connect.e4;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.argeo.connect.ServiceRanking;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;

public class SystemE4ServiceFunction extends ContextFunction {
	// Injected known AppWorkbenchServices: order is important, first found
	// result will be returned by the various methods.
	private SortedMap<ServiceRanking, AppWorkbenchService> knownAppWbServices = Collections
			.synchronizedSortedMap(new TreeMap<>());

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
//		MApplication app = context.get(MApplication.class);
//		IEclipseContext appContext = app.getContext();
//		EPartService partService = appContext.get(EPartService.class);
//		ECommandService commandService = appContext.get(ECommandService.class);
//		EHandlerService handlerService = appContext.get(EHandlerService.class);

		SystemE4Service systemE4Service = new SystemE4Service(knownAppWbServices, context);

		// if (app == null) {// during login
		// context.set(SystemWorkbenchService.class, systemE4Service);
		// } else {// regular use case
		// IEclipseContext appCtx = app.getContext();
		// if (appCtx != null)
		// appCtx.set(SystemWorkbenchService.class, systemE4Service);
		// else
		// context.set(SystemWorkbenchService.class, systemE4Service);
		// }
		context.set(SystemWorkbenchService.class, systemE4Service);
		return systemE4Service;
	}

	public void addAppService(AppWorkbenchService appService, Map<String, Object> properties) {
		// String serviceRankingStr = properties.get(Constants.SERVICE_RANKING);
		// int serviceRanking = serviceRankingStr == null ? 0 :
		// Integer.parseInt(serviceRankingStr);
		// Integer serviceRanking = (Integer) properties.get(Constants.SERVICE_RANKING);
		// System.out.println(serviceRanking + " | " + properties);
		knownAppWbServices.put(new ServiceRanking(properties), appService);
	}

	public void removeAppService(AppWorkbenchService appService, Map<String, Object> properties) {
		knownAppWbServices.remove(new ServiceRanking(properties));
	}

}
