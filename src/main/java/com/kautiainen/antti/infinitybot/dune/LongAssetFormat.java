package com.kautiainen.antti.infinitybot.dune;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kautiainen.antti.infinitybot.model.Trait;

import reactor.util.annotation.NonNull;

/**
 * 
 * The asset format defines the basic human readable format of the character.
 * 
 * @author Antti Kautiainen
 *
 */
public class LongAssetFormat extends java.text.Format {

	/**
	 * The serial version of the asset format.
	 */
	private static final long serialVersionUID = -8191523570657608138L;

	/**
	 * The field of an asset.
	 */
	public static final Format.Field ASSET_FIELD = new Format.Field("asset") {
		
		/**
		 * Get the sub fields of the asset.
		 * 
		 * @return The sub fields of the asset field.
		 */
		public List<Format.Field> getSubFields() {
			return Arrays.asList(NAME_FIELD, QUALITY_FIELD, LEVEL_FIELD, DESCRIPTION_FIELD);
		}
	};
	
	/**
	 * ComplexFieldEntry stores a complex field entry, and its capture group name.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static interface ComplexFieldEntry {
		
		/**
		 * Get the capture group name of a group with a number of previous groups with the same name.
		 * 
		 * @param fieldName The group name.
		 * @param groupCount The number of groups of the same name before this group.
		 * @return The valid capture group name for the given group.
		 * @throws NoSuchElementException The given group name is not a valid group name.
		 * @throws IllegalArgumentException The group count is invalid.
		 */
		static String getCaptureGroupName(String fieldName, Integer groupCount)
				throws NoSuchElementException, IllegalArgumentException {
			if (groupCount == null) {
				return ComplexField.getCaptureGroupName(fieldName);
			} else if (groupCount < 0) {
				throw new IllegalArgumentException("Invalid group count");
			} else {
				return ComplexField.getCaptureGroupName(fieldName) + "_" + groupCount;
			}
		}
		
		/**
		 * Get the field of the entry.
		 * 
		 * @return The field of the entry, if any exists.
		 */
		default Optional<Format.Field> getField() {
			return Optional.ofNullable(getComplexField());
		}
		
		/**
		 * Get the complex field of the entry.
		 * 
		 * @return The complex field of the entry.
		 */
		public LongAssetFormat.ComplexField getComplexField();
		
		/**
		 * Get the capture group name.
		 * 
		 * @return The capture group name of the entry.
		 */
		default String getCaptureGroupName() {
			return ComplexField.getCaptureGroupName(getComplexField().getName());
		}
	}
	
	/**
	 * SimpleComplexFieldEntry is a simple implementation of the complex field entry.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class SimpleComplexFieldEntry implements LongAssetFormat.ComplexFieldEntry {
		
		private LongAssetFormat.ComplexField field_;
		
		public SimpleComplexFieldEntry(LongAssetFormat.ComplexField field) throws IllegalArgumentException {
			if (field == null) throw new IllegalArgumentException("Undefined field", new NullPointerException());
			field_ = field;
		}
		
		@Override
		public LongAssetFormat.ComplexField getComplexField() {
			return field_;
		}
	}
	
	/**
	 * Sub field reference entry determines dynamically the field and the capture group
	 * name from the given owner sub fields.
	 * 
	 * The implementation is synchronized locking the owner for the duration of the determination
	 * of the capture group.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class SubFieldReferenceEntry implements LongAssetFormat.ComplexFieldEntry {
		
		/**
		 * The always defined owner of the referenced sub-field entry.
		 */
		private final LongAssetFormat.ComplexField owner_; 
		
		/**
		 * The index of the referenced sub field entry.
		 */
		private final int index_; 
		
		/**
		 * Create a new sub field reference entry referencing a sub field with given index.
		 * 
		 * @param owner The owner of the sub field. Has to be defined.
		 * @param subFieldIndex The index of the referenced sub field.
		 * @throws IndexOutOfBoundsException The given index is invalid.
		 * @throws NullPointerException
		 */
		public SubFieldReferenceEntry(LongAssetFormat.ComplexField owner, int subFieldIndex)
				throws IndexOutOfBoundsException, NullPointerException {
			if (owner == null) throw new NullPointerException("Undefined owner");
			if (subFieldIndex < 0) throw new IndexOutOfBoundsException("Negative sub field index");
			this.owner_ = owner;
			this.index_ = subFieldIndex;
		}
		
		@Override
		public LongAssetFormat.ComplexField getComplexField() {
			synchronized (owner_) {
				if (index_ >= 0 && index_ < owner_.subFields_.size()) {
					// Getting the sub field.
					return owner_.subFields_.get(index_);
				} else {
					// The field does not exist.
					return null; 
				}
			}
		}
		
		@Override
		public String getCaptureGroupName() {
			Integer index = null;
			String fieldName = null;
			synchronized (owner_) {
				LongAssetFormat.ComplexField field = getComplexField();
				index = getSubFieldIndex(owner_, field.getName(), index_);
			}
			// Generating the result.
			return (fieldName == null ? null : ComplexFieldEntry.getCaptureGroupName(fieldName, index));
		}

		/**
		 * Calculate the sub field index of the sub field with given name at the given end index.
		 * 
		 * @param owner The owner of the sub fields.
		 * @param fieldName The field name. 
		 * @param endIndex The end index.
		 * @return If the given owner or field name is undefined, an undefined value. If the given field would
		 *  have index of 0, an undefined value is returned. Otherwise a defined index usable as capture group index
		 *  is returned.
		 * @throws IndexOutOfBoundsException The given end index was invalid.
		 */
		public static Integer getSubFieldIndex(LongAssetFormat.ComplexField owner, String fieldName, int endIndex)
				throws IndexOutOfBoundsException {
			if (owner == null || fieldName == null || !ComplexField.validFieldName(fieldName)) return null; 
			Integer result = null;
			synchronized (owner) {
				long count = owner.subFields_.subList(0,  endIndex).stream().filter(
						(LongAssetFormat.ComplexField subField) -> (subField != null && fieldName == subField.getName())).count();
				if (count > 0 && count < Integer.MAX_VALUE) {
					result = (int)count;
				}
			}
			return result; 
		}
		
	}
	
	/**
	 * Complex field storing value recognition pattern, and possible sub fields.
	 * 
	 * @author Antti Kautiainen
	 *
	 */
	public static class ComplexField extends Format.Field {
		
		/**
		 * The pattern of the complex format, if any exists.
		 */
		private Optional<Pattern> pattern_;
		
		/**
		 * The sorted set of the reserved capture group names. These capture group names are field names.
		 */
		private java.util.SortedSet<String> captureGroupNames_;
		
		/**
		 * The list of sub fields of the pattern. The sub field capture group names has to be found from the 
		 * capture group names. If a group exists multiple times, the sub group name should have index suffix 
		 * separated with '_' in the capture group names. The sub-group match is used as sub field source for 
		 * matching, and it should match to the sub-field pattern.
		 */
		private java.util.List<LongAssetFormat.ComplexField> subFields_ = new java.util.ArrayList<>();
		
		/**
		 * Check the capture group name.
		 * 
		 * @param name The group name.
		 * @return The field name has to be a valid capture group name.
		 */
		public static boolean validCaptureGroupName(String name) {
			return name != null && Pattern.matches("[a-zA-Z][a-zA-Z0-9_]*", name);
		}
		
		
		/**
		 * Check the name.
		 * 
		 * @param name The tested name.
		 * @return True, if and only if the given name is valid field name.
		 */
		public static boolean validFieldName(String name) {
			return name != null && Pattern.matches("[a-zA-Z][\\sa-zA-Z0-9]*", name);
		}
		
		/**
		 * Get capture group of the group name.
		 * 
		 * @param groupName The group name.
		 * @return The capture group name of the given valid gropu nam.e
		 * @throws NoSuchElementException The given group name does not have valid group name.
		 */
		public static String getCaptureGroupName(String groupName)
				throws NoSuchElementException {
			if (validFieldName(groupName)) {
				return groupName.replaceAll("\\s", "_");
			} else {
				throw new NoSuchElementException("No valid capture group exists");
			}
		}
		
		/**
		 * Create a new complex field without a pattern, capture groups, or sub fields.
		 * 
		 * @param name The name of the complex field.
		 * @throws IllegalArgumentException The given name is invalid.
		 */
		public ComplexField(String name)
				throws IllegalArgumentException {
			this(name, null, null, null);
		}
		
		/**
		 * Create a new complex field with given name, pattern, capture group names, and sub fields.
		 * 
		 * If pattern is defined, the sub field patterns has to be contained in the pattern with their
		 * capture groups, and the capture group names has to contain all capture group names. 
		 * 
		 * If pattern is not defined, the pattern is formed by combining the sub field patterns, and the 
		 * capture group names are created from the capture group names of the given sub fields.
		 *  
		 * @param name The name of the field.
		 * @param valuePattern
		 * @param captureGroupNames
		 * @param subFields
		 * @throws IllegalArgumetnException Any parameter was invalid.
		 */
		public ComplexField(String name, Pattern valuePattern, 
				java.util.Set<String> captureGroupNames, 
				java.util.List<LongAssetFormat.ComplexField> subFields)
						throws IllegalArgumentException {
			super(name);
			if (!validFieldName(name)) {
				throw new IllegalArgumentException("Invalid name");
			}
			
			
			if (valuePattern == null && captureGroupNames == null) {
				// Constructing the pattern and the capture group names.
				// - as the lack of capturing pattern means the sub pattern cannot have conflicting captures, it is okay.
				this.subFields_.addAll(subFields);
				pattern_ = Optional.empty();
				captureGroupNames_ = Collections.emptySortedSet();
			} else {
				// Testing the sub patterns and building the owners of the mandatory capture groups.
				pattern_ = Optional.ofNullable(valuePattern);
				captureGroupNames_ = ( captureGroupNames==null || captureGroupNames.isEmpty() ) ? Collections.emptySortedSet() :
					new TreeSet<>(captureGroupNames);
				
				// Testing sub fields.
				if (subFields != null) {
					int index = 0; 
					for (LongAssetFormat.ComplexField field: subFields) {
						if (field == null) throw new IllegalArgumentException("Undefined sub field", new NullPointerException());
						LongAssetFormat.SubFieldReferenceEntry entry = new SubFieldReferenceEntry(this, index);
						String captureGroupName = entry.getCaptureGroupName();
						if (captureGroupName == null) {
							throw new IllegalArgumentException("A sub field without appropriate capture entry!");
						}
						// Adding the field. 
						this.subFields_.add(field);
						index++;
					}
				}
			}
		}
		
		@Override
		public String getName() {
			return super.getName();
		}
		
		/**
		 * Does the complex field contain a capture group.
		 * 
		 * @param captureGroupName The tested capture group name.
		 * @return True, if and only if the given capture group name belongs to the capture groups names of the complex field.
		 */
		public boolean containsCaptureGroup(String captureGroupName) {
			return this.captureGroupNames_.contains(captureGroupName);
		}
		
		
		/**
		 * The value matching pattern of the current complex field.
		 * 
		 * @return The pattern matching to the value of the current pattern. If this value is empty, 
		 *  no matching pattern exists.
		 */
		public Optional<Pattern> getValuePattern() {
			return pattern_;
		}
		
		/**
		 * Get the reserved named capture group names. 
		 * 
		 * @return The unmodifiable sorted set of the reserved capture group names.
		 */
		public java.util.SortedSet<String> getReservedNamedCaptureGroupNames() {
			return Collections.unmodifiableSortedSet(this.captureGroupNames_);
		}
		
		public java.util.Map<String, Object> parseObject(String source, ParsePosition pos) {
			Optional<Pattern> pattern = getValuePattern();
			if (pattern.isPresent()) {
				// Using the capture groups of the pattern.
				CharSequence parsed = null;
				Matcher matcher = pattern.get().matcher(source);
				if (matcher.find(pos.getIndex()) && matcher.start() == pos.getIndex()) {
					// The parse was found.
					// - checking the sub fields.
					java.util.Map<String, Object> result = new TreeMap<>();
					LongAssetFormat.SubFieldReferenceEntry entry; 
					for (String captureGroupName: this.getReservedNamedCaptureGroupNames()) {
						result.put(captureGroupName, matcher.group(captureGroupName));
					}
					for (int i = 0; i < this.subFields_.size(); i++) {
						entry = new SubFieldReferenceEntry(this, i);
						String key = entry.getCaptureGroupName();
						ParsePosition subPos = new ParsePosition(0);
						java.util.Map<String, Object> subResult = entry.getComplexField().parseObject((String)result.get(key), subPos);
						if (subPos.getErrorIndex() >= 0) {
							pos.setErrorIndex(matcher.start(key));
							return null;
						} else {
							result.put(key, subResult);
						}
					}
					return result;
				} else {
					// The parse failed.
					pos.setErrorIndex(pos.getIndex());
					return null; 
				}
			} else {
				// The pattern is not present.
				java.util.Map<String, Object> result = new TreeMap<>();
				int subFieldIndex = 0;
				ParsePosition subPosition = new ParsePosition(pos.getIndex());
				String key;
				for (LongAssetFormat.ComplexField subField: this.subFields_) {
					java.util.Map<String, Object> subResult = subField.parseObject(source, subPosition);
					if (subPosition.getErrorIndex() >= 0) {
						pos.setErrorIndex(subPosition.getErrorIndex());
						return null;
					} else {
						// We do have result.
						key = new SubFieldReferenceEntry(this, subFieldIndex).getCaptureGroupName();
						if (key != null) {
							result.put(key, subResult);
						}
					}
					subFieldIndex++;
				}
				pos.setIndex(subPosition.getIndex());
				
				return result; 
			}
		}
		
	}
	
	/**
	 * The field of a name.
	 */
	public static final Format.Field NAME_FIELD = new Format.Field("name") {
		
		/**
		 * Get the serialization version.
		 */
		private static final long serialVersionUID = 1L;
	};
	
	/**
	 * The field of a quality.
	 */
	public static final Format.Field QUALITY_FIELD = new Format.Field("quality") {
		/**
		 * Get the serialization version.
		 */
		private static final long serialVersionUID = 1L;
	};

	/**
	 * The field of a level.
	 */
	public static final Format.Field LEVEL_FIELD = new Format.Field("level") {
		/**
		 * Get the serialization version.
		 */
		private static final long serialVersionUID = 1L;
	};
	
	/**
	 * The field of description.
	 */
	public static final Format.Field DESCRIPTION_FIELD = new Format.Field("description") {
		/**
		 * Get the serialization version.
		 */
		private static final long serialVersionUID = 1L;
	};

	private static final Pattern NAME_PARSE_PATTERN = Asset.NAME_PARSE_PATTERN;
	
	private static final Pattern DESCRIPTION_PARSE_PATTERN = 
			Pattern.compile(":\\s+(?<" +  Asset.DESCRIPTION_GROUP_NAME + ">" + "[^\\n]*" + ")");

	private static final Pattern LEVEL_PARSE_PATTERN = Pattern.compile("\\(" + 
	"(?<" + Trait.LEVEL_GROUP_NAME + ">\\d+)" +
	"\\)");

	private static final Pattern QUALITY_PARSE_PATTERN = Pattern.compile("\\(Q" + 
	"(?<" + Trait.LEVEL_GROUP_NAME + ">\\d+)" +
	"\\)");;
	
	
	private static final Pattern ASSET_PARSE_PATTERN = Pattern.compile("" + 
	NAME_PARSE_PATTERN + 
	QUALITY_PARSE_PATTERN + 
	LEVEL_PARSE_PATTERN + 
	DESCRIPTION_PARSE_PATTERN
	);
			
	/**
	 * Get the set of valid integer fields.
	 * 
	 * @return The set of valid integer fields.
	 */
	public java.util.Set<Format.Field> getStringFields() {
		return new java.util.HashSet<>(Arrays.asList(DESCRIPTION_FIELD, NAME_FIELD));			
	}

	public boolean validStringField(Format.Field field) {
		return getStringFields().contains(field);
	}
			
	/**
	 * Get the set of valid integer fields.
	 * 
	 * @return The set of valid integer fields.
	 */
	public java.util.Set<Format.Field> getIntegerFields() {
		return new java.util.HashSet<>(Arrays.asList(QUALITY_FIELD, LEVEL_FIELD));
	}
	
	/**
	 * Test the validity of the given field.
	 * 
	 * @param field The tested field.
	 * @return True, if and only if teh field is valid.
	 */
	public boolean validIntegerField(Format.Field field) {
		return getIntegerFields().contains(field);
	}
			
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
	throws IllegalArgumentException {
		if (obj instanceof String desc) {
			// description
			if (validStringField(pos.getFieldAttribute())) {
				pos.setBeginIndex(toAppendTo.length());
				if (desc != null && desc.isBlank()) {
					toAppendTo.append(desc.trim());
				}
			} else {
				throw new IllegalArgumentException("Cannot format given field");
			}
		} else if (obj instanceof Integer value) {
			// level or quality.
			if (validIntegerField(pos.getFieldAttribute())) {
				pos.setBeginIndex(toAppendTo.length());
				toAppendTo.append("(");
				if (pos.getFieldAttribute() == QUALITY_FIELD) {
					toAppendTo.append("Q");
				}
				toAppendTo.append(")");
				toAppendTo.append(value);
			} else {
				throw new IllegalArgumentException("Cannot format given field");
			}
		} else if (obj instanceof Asset asset) {
			pos.setBeginIndex(toAppendTo.length());
			toAppendTo.append(asset.getName());
			Optional<Integer> value;
			value = asset.getQuality();
			if (value.isPresent()) {
				toAppendTo.append("(Q");
				format(value.get(), toAppendTo, new FieldPosition(QUALITY_FIELD));
				toAppendTo.append(")");
			}
			value = asset.getLevel();
			if (value.isPresent()) {
				toAppendTo.append("(");
				format(value.get(), toAppendTo, new FieldPosition(LEVEL_FIELD));
				toAppendTo.append(")");
			}
			
			Optional<String> desc = asset.getDescription();
			if (desc.isPresent() && !desc.get().trim().isEmpty()) {
				toAppendTo.append(": ");
				format(desc.get().trim(), toAppendTo, new FieldPosition(DESCRIPTION_FIELD));
				toAppendTo.append(";;");
			}				
		} else {
			throw new IllegalArgumentException("Cannot format given value - not an asset");
		}
		
		pos.setEndIndex(toAppendTo.length());
		return toAppendTo;
	}
	
	/**
	 * Parse an integer. 
	 * 
	 * @param source The source.
	 * @return Undefined value, if given source is undefined. Otherwise the integer parsed
	 *  from the source.
	 * @throws NumberFormatException The given source was not a number.
	 */
	public static Integer parseInteger(String source) throws NumberFormatException  {
		if (source == null) return null;
		return Integer.parseInt(source);
	}

	
	/**
	 * Parse sub field. The parse position is updated accordingly. 
	 * <p>If the parse fails at the parse position, the error index is set.</p>
	 * <P>If the parse succeeds at the parse position, the parse position is updated to the 
	 *  end of the match.</p>
	 * 
	 * @param matcher The matcher containing the match of the given pattern. The matcher will be altered when
	 *  seeking match of a pattern.
	 * @param pattern The pattern used for match.
	 * @param pos The position of the parsing. This will be updated.
	 * @return The parsed object, if the parse succeeds. An undefined value on failed parse.
	 */
	protected Object parseObject(@NonNull Matcher matcher, final @NonNull Pattern pattern, @NonNull ParsePosition pos) {
		int index = pos.getIndex();
		if (matcher.find(pos.getIndex()) && matcher.start() == index) {
			// We do have a match.
			String valueRep;
			Object result = null;
			if (pattern == QUALITY_PARSE_PATTERN) {
				try {
					valueRep = matcher.group(Asset.QUALITY_GROUP_NAME);
					result = valueRep==null?Asset.DEFAULT_QUALITY:Integer.parseInt(valueRep);
					pos.setIndex(matcher.end());
					return result;
				} catch (Exception e) {
					pos.setErrorIndex(index);
					return null;
				}
			} else if (pattern == LEVEL_PARSE_PATTERN) {
				try {
					valueRep = matcher.group(Asset.LEVEL_GROUP_NAME);
					result = valueRep==null?Asset.DEFAULT_LEVEL:Integer.parseInt(valueRep);
					pos.setIndex(matcher.end());
					return result;
				} catch (Exception e) {
					pos.setErrorIndex(index);
					return null;
				}							
			} else if (pattern == DESCRIPTION_PARSE_PATTERN) {
				valueRep = matcher.group(Asset.DESCRIPTION_GROUP_NAME);
				pos.setIndex(matcher.end());
				return valueRep;
			} else if (pattern == ASSET_PARSE_PATTERN) {
				// We do have asset.
				pos.setIndex(matcher.end());
				return new SimpleAsset(
						matcher.group(Asset.NAME_GROUP_NAME), 
						parseInteger(matcher.group(Asset.LEVEL_GROUP_NAME)), 
						parseInteger(matcher.group(Asset.QUALITY_GROUP_NAME)), 
						matcher.group(Asset.DESCRIPTION_GROUP_NAME));
			} else {
				// This should never happen - returning null is reserved for situation inheritor
				// adds new field pattern, and does not deal with it before calling super class version of the method.
				return null;
			}
			
		} else {
			// Match was not found.
			pos.setErrorIndex(index);
			return null;
		}
	}
	
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if (pos.getErrorIndex() >= 0) return null;
		int maxIndex = source==null?0:source.length();
		int index = pos.getIndex();
		if (index > maxIndex) {
			return null; 
		}
		Object result = null;
		Matcher matcher; 
		for (Pattern pattern: getParsePatterns()) {
			matcher = pattern.matcher(source);
			result = parseObject(matcher, pattern, pos);
			if (pos.getErrorIndex() >= 0) {
				// Parse failed. This is ignored - reseting the error index.
				pos.setErrorIndex(-1);
			} else {
				// Parse succeeded - returning the result.
				return result; 
			}
		}
		return result;
	}

	/**
	 * Get the parse patterns in the order of testing.
	 * 
	 * @return An always defined list of patterns used to parse object.
	 */
	protected @NonNull List<Pattern> getParsePatterns() {
		return Arrays.asList(QUALITY_PARSE_PATTERN, LEVEL_PARSE_PATTERN, DESCRIPTION_PARSE_PATTERN, ASSET_PARSE_PATTERN);
	}
	
}