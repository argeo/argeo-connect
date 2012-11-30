/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.argeo.ArgeoException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.FactoryBean;

/**
 * Creates a GeoTools {@link SimpleFeatureType} based on the properties of a
 * bean class.
 */
public class BeanFeatureTypeBuilder<T> implements FactoryBean {
	private final BeanWrapper classBeanWrapper;

	private SimpleFeatureType cachedFeatureType;
	private List<String> cachedAttributeList;
	private String name;

	private final CoordinateReferenceSystem crs;

	public BeanFeatureTypeBuilder(Class<? extends T> clss) {
		this(null, clss);
	}

	public BeanFeatureTypeBuilder(String name, Class<? extends T> clss) {
		this(name, clss, null);
	}

	public BeanFeatureTypeBuilder(String name, Class<? extends T> clss,
			CoordinateReferenceSystem crs) {
		this.classBeanWrapper = new BeanWrapperImpl(clss);
		this.name = name;
		try {
			this.crs = crs != null ? crs : CRS.decode("EPSG:4326");
		} catch (Exception e) {
			throw new ArgeoException("Cannot set CRS", e);
		}
		if (this.name == null)
			this.name = getClassBeanWrapper().getWrappedClass().getSimpleName();
		cachedFeatureType = doBuildFeatureType();
	}

	protected SimpleFeatureType doBuildFeatureType() {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();

		builder.setName(name);

		// TODO: make it configurable
		builder.setNamespaceURI("http://localhost/");

		// CRS has to be set BEFORE adding geometry attributes
		builder.setCRS(crs);

		cachedAttributeList = new ArrayList<String>();
		props: for (PropertyDescriptor pd : getClassBeanWrapper()
				.getPropertyDescriptors()) {
			String propertyName = pd.getName();
			if (propertyName.equals("class"))
				continue props;
			if (Collection.class.isAssignableFrom(pd.getPropertyType()))
				continue props;
			builder.add(pd.getName(), pd.getPropertyType());
			cachedAttributeList.add(pd.getName());
		}

		return builder.buildFeatureType();
	}

	public SimpleFeatureType getFeatureType() {
		if (cachedFeatureType == null) {
			cachedFeatureType = doBuildFeatureType();
		}
		return cachedFeatureType;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends T> getWrappedClass() {
		return (Class<? extends T>) classBeanWrapper.getWrappedClass();
	}

	protected void resetFeatureType() {
		cachedFeatureType = null;
		if (cachedAttributeList != null) {
			cachedAttributeList.clear();
			cachedAttributeList = null;
		}
	}

	protected List<String> getCachedAttributeList() {
		if (cachedAttributeList == null)
			throw new ArgeoException(
					"Cached attribute list not set: initialize the object properly before calling this method");
		return cachedAttributeList;
	}

	public SimpleFeature buildFeature(T object) {
		return buildFeature(object, null);
	}

	public SimpleFeature buildFeature(Object object, String id) {
		if (object == null)
			throw new ArgeoException("Cannot build feature from null");

		if (!((Class<?>) classBeanWrapper.getWrappedClass())
				.isAssignableFrom(object.getClass())) {
			throw new ArgeoException("Object type " + object.getClass()
					+ " not compatible with wrapped class "
					+ classBeanWrapper.getWrappedClass());
		}

		BeanWrapper instanceWrapper = new BeanWrapperImpl(object);
		SimpleFeatureType type = getFeatureType();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
		for (String attr : getCachedAttributeList()) {
			featureBuilder.add(instanceWrapper.getPropertyValue(attr));
		}

		return featureBuilder.buildFeature(id);
	}

	/**
	 * Converts a feature with another type by creating a new feature with the
	 * properties that they have in common.
	 */
	public SimpleFeature convertFeature(SimpleFeature feature) {
		SimpleFeatureType type = getFeatureType();

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
		Iterator<Property> properties = feature.getProperties().iterator();
		while (properties.hasNext()) {
			Property property = properties.next();
			if (type.indexOf(property.getName()) >= 0) {// has prop
				featureBuilder.set(property.getName(), property.getValue());
			}
		}
		return featureBuilder.buildFeature(null);
	}

	protected BeanWrapper getClassBeanWrapper() {
		return classBeanWrapper;
	}

	public Object getObject() throws Exception {
		return getFeatureType();
	}

	public Class<?> getObjectType() {
		return classBeanWrapper.getWrappedClass();
	}

	public boolean isSingleton() {
		return true;
	}

}
