package org.argeo.connect;

import java.util.Map;

public class ServiceRanking implements Comparable<ServiceRanking> {
	public final static String SERVICE_ID = "service.id";
	public final static String SERVICE_RANKING = "service.ranking";

	private final Long id;
	private final Integer ranking;

	public ServiceRanking(Map<String, Object> properties) {
		this((Long) properties.get(SERVICE_ID),
				properties.containsKey(SERVICE_RANKING) ? (Integer) properties.get(SERVICE_RANKING) : 0);
	}

	public ServiceRanking(Long id, Integer ranking) {
		if (id == null)
			throw new IllegalArgumentException("Service id cannot be null");
		if (ranking == null)
			throw new IllegalArgumentException("Service ranking cannot be null");
		this.id = id;
		this.ranking = ranking;
	}

	@Override
	public int hashCode() {
		return id.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ServiceRanking))
			return false;
		ServiceRanking o = (ServiceRanking) obj;
		return id == o.id;
	}

	/**
	 * Inverted with the canonical order, so that the highest ranking services come
	 * first.
	 */
	@Override
	public int compareTo(ServiceRanking o) {
		if (ranking == o.ranking)
			return id.compareTo(o.id);
		else
			return o.ranking.compareTo(ranking);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new ServiceRanking(id, ranking);
	}

	@Override
	public String toString() {
		return ranking + "-" + id;
	}

}
