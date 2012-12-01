package org.argeo.photo.manager;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.argeo.photo.manager.ui.PhotoManagerUiPlugin;

public class PatternConverter implements PathConverter {
	private final List<Patterns> conversionPatterns;
	private Boolean forceLowerCase = true;

	public PatternConverter() {
		conversionPatterns = new Vector<Patterns>();
		// TODO: add comparator for pattern priorities
		conversionPatterns.add(new Patterns("dsc_{1,number,integer}.nef",
				"{0}-{1,number,000}.nef"));
		conversionPatterns.add(new Patterns("dsc_{1,number,integer}.jpg",
				"jpeg/{0}-{1,number,000}.jpg"));
	}

	public PathConverterResult convert(Set<String> paths) {
		PathConverterResult result = new PathConverterResult(paths);
		for (String fromPath : paths) {
			String toPath = null;
			Exception formatException = null;
			for (Patterns patterns : conversionPatterns) {
				String fromPattern = patterns.getFromPattern();
				String toPattern = patterns.getToPattern();

				// TODO: cache compiled formats for optimization
				MessageFormat fromFormat = new MessageFormat(fromPattern);
				MessageFormat toFormat = new MessageFormat(toPattern);

				try {
					PhotoManagerUiPlugin.stdOut("fromPattern: " + fromPattern);
					PhotoManagerUiPlugin.stdOut("fromPath: " + fromPath);
					String fromPathToUse;
					if (forceLowerCase)
						fromPathToUse = fromPath.toLowerCase();
					else
						fromPathToUse = fromPath;
					Object objs[] = fromFormat.parse(fromPathToUse);
					preFormat(objs);
					// objs[PhotoDesc.FIELD_SRCID] = "TEST_LABEL";
					toPath = toFormat.format(objs);
					PhotoManagerUiPlugin.stdOut("toPath: " + toPath);
					break;// break patterns loop if success
				} catch (Exception e) {
					formatException = e;
				}
			}

			if (toPath == null) {
				if (formatException == null)
					throw new RuntimeException("No to path and no exception.");
				// formatException.printStackTrace();
				result.getConversionErrors().put(fromPath, formatException);
			} else {
				// Check duplicated
				if (result.getMapping().containsValue(toPath)) {
					for (String fromPathT : result.getMapping().keySet()) {
						String toPathT = result.getMapping().get(fromPathT);
						if (toPath.equals(toPathT)) {
							result.getDuplicated().add(fromPathT);
						}
						result.getDuplicated().add(fromPathT);
					}
				}
				// Add to mapping
				result.getMapping().put(fromPath, toPath);
			}
		}

		return result;
	}

	public List<Patterns> getConversionPatterns() {
		return conversionPatterns;
	}

	protected void preFormat(Object[] objs) {

	}

	public static class Patterns {
		private String fromPattern;
		private String toPattern;

		public Patterns(String fromPattern, String toPattern) {
			this.fromPattern = fromPattern;
			this.toPattern = toPattern;
		}

		public String getToPattern() {
			return toPattern;
		}

		public void setToPattern(String toPattern) {
			this.toPattern = toPattern;
		}

		public String getFromPattern() {
			return fromPattern;
		}

		public void setFromPattern(String fromPattern) {
			this.fromPattern = fromPattern;
		}

	}

}
