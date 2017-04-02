package org.argeo.tracker.internal.ui.controls;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.CmsUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.tracker.TrackerException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/** Simple composite widget to display a gauge value as chart */
public class RepartitionChart extends Composite {
	private static final long serialVersionUID = -2758463753509540538L;
	private final Log log = LogFactory.getLog(RepartitionChart.class);

	private final static String[] COLORS = { "#FF6384", "#FFCE56", "#36A2EB", "#FF99FF", "#84ff63", "#CE506B",
			"#DEB390", "#C89127", "#024913", "#b59bc6", "#0e2024" };

	private String options = "options: { " + "legend: { display: true, position: 'right' } , "
			+ "title: { display: true, position: 'bottom', fontSize: 15,  "
			+ "fontFamily: \"'Helvetica Neue', 'Helvetica', 'Arial', sans-serif\", "
			+ "fontColor: '#909B9B', fontStyle: 'normal', text: customisedTitle }, "
			+ "animation:{animateScale:true}"
			+ "}";

	public RepartitionChart(Composite parent, int style) {
		super(parent, style);
	}

	public void setInput(String title, Map<String, String> valueMap, int width, int height) {
		Composite parent = this;
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		CmsUtils.clear(parent);

		StringBuilder labels = new StringBuilder();
		StringBuilder data = new StringBuilder();
		StringBuilder bgColors = new StringBuilder();
		StringBuilder hoColors = new StringBuilder();

		int i = 0;
		for (String name : valueMap.keySet()) {
			labels.append("'").append(name).append("', ");
			data.append(valueMap.get(name)).append(", ");
			bgColors.append("'").append(COLORS[i]).append("', ");
			hoColors.append("'").append(COLORS[i]).append("', ");
			i++;
		}

		final String jscData = "{labels: [" + labels.substring(0, labels.length() - 2) + "], datasets: [{ "
				+ "borderWidth: 0.5, borderColor:'#909B9B', data: ["
				+ data.substring(0, data.length() - 2) + "], " + " backgroundColor: [ "
				+ bgColors.substring(0, bgColors.length() - 2) + "], " + " hoverBackgroundColor: [ "
				+ hoColors.substring(0, hoColors.length() - 2) + "] " + "} ]}";

		final Browser browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new GridData(width, height));
		String html;
		InputStream in = null;
		try {
			in = getClass().getResourceAsStream("/org/argeo/tracker/internal/charts/RepartitionPie.html");
			html = IOUtils.toString(in);
		} catch (IOException e) {
			throw new TrackerException("Cannot read HTML", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		browser.addProgressListener(new ProgressListener() {
			private static final long serialVersionUID = 2852043674756868097L;

			@Override
			public void completed(ProgressEvent event) {
				try {
					String type = "pie";
					StringBuilder js = new StringBuilder();
					js.append("var customisedTitle='").append(title).append("';\n");
					js.append("var chartData=").append(jscData).append(";\n");
					js.append("new Chart(").append("document.getElementById('chart').getContext('2d'),").append("{")
							.append("type:'").append(type).append("',").append("data:chartData,\n").append(options)
							.append("});");
					try {
						if (log.isTraceEnabled())
							log.debug(js.toString());
						browser.evaluate(js.toString());
					} catch (SWTException e) {
						log.error(e.getMessage() + "\n" + js + "\n");
					}
				} catch (Exception e) {
					new TrackerException("Unable to prepare chart", e);
				}
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});
		browser.setText(html);
		parent.layout(true, true);
	}

	@Override
	public boolean setFocus() {
		return true;
	}
}
