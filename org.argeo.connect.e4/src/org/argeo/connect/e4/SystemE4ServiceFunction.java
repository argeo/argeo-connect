package org.argeo.connect.e4;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.argeo.connect.ServiceRanking;
import org.argeo.connect.ui.AppWorkbenchService;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class SystemE4ServiceFunction extends ContextFunction {
	// Injected known AppWorkbenchServices: order is important, first found
	// result will be returned by the various methods.
	private SortedMap<ServiceRanking, AppWorkbenchService> knownAppWbServices = Collections
			.synchronizedSortedMap(new TreeMap<>());

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		EPartService partService = context.get(EPartService.class);
		ECommandService commandService = context.get(ECommandService.class);
		EHandlerService handlerService = context.get(EHandlerService.class);
//		MApplication app = context.get(MApplication.class);

		SystemE4Service systemE4Service = new SystemE4Service(knownAppWbServices, partService, commandService, handlerService);;
//		if (app == null) {// during login
//			context.set(SystemWorkbenchService.class, systemE4Service);
//		} else {// regular use case
//			IEclipseContext appCtx = app.getContext();
//			if (appCtx != null)
//				appCtx.set(SystemWorkbenchService.class, systemE4Service);
//			else
//				context.set(SystemWorkbenchService.class, systemE4Service);
//		}
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
