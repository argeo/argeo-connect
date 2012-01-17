/*
 * Copyright (C) 2010 Mathieu Baudier <mbaudier@argeo.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
