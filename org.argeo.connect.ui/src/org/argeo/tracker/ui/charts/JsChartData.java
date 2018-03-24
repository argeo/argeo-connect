package org.argeo.tracker.ui.charts;

/** Configure chartjs objects via JSon */
@SuppressWarnings("unused")
@Deprecated
class JsChartData {
	private String[] labels;
	private Object[] datasets;

	public JsChartData(String[] labels, Object[] datasets) {
		this.labels = labels;
		this.datasets = datasets;
	}
}
