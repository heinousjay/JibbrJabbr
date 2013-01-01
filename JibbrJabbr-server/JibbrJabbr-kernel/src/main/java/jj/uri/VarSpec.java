/*
 * adapted from https://github.com/damnhandy/Handy-URI-Templates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jj.uri;

final class VarSpec {

	public static enum VarFormat {
		SINGLE, ARRAY, PAIRS;
	}

	private Modifier modifier = Modifier.NONE;

	private String value;

	private Integer position = null;

	private String variableName;

	/**
	 * Create a new VarSpec.
	 * 
	 * @param modifier
	 * @param value
	 */
	public VarSpec(String value, Modifier modifier) {
		this(value, modifier, null);
	}

	/**
	 * Create a new VarSpec.
	 * 
	 * @param modifier
	 * @param value
	 * @param position
	 */
	public VarSpec(String value, Modifier modifier, Integer position) {
		this.modifier = modifier;
		this.value = value;
		this.position = position;
		initVariableName();
	}

	/**
	 * Get the modifier.
	 * 
	 * @return the modifier.
	 */
	public Modifier getModifier() {
		return modifier;
	}

	/**
	 * Get the value.
	 * 
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get the position.
	 * 
	 * @return the position.
	 */
	public Integer getPosition() {
		return position;
	}

	private void initVariableName() {
		if (modifier != Modifier.NONE) {
			if (modifier == Modifier.PREFIX) {
				String[] values = getValue().split(Modifier.PREFIX.getValue());
				variableName = values[0];
			} else if (modifier == Modifier.EXPLODE) {
				variableName = getValue().substring(0, getValue().length() - 1);
			}
		} else {
			variableName = getValue();
		}
	}

	public String getVariableName() {
		if (variableName == null) {
			return getValue();
		}
		return variableName;
	}

	@Override
	public String toString() {
		return "VarSpec [modifier=" + modifier + 
			", value=" + value + 
			", position=" + position +
			", variableName=" + variableName +
			"]";
	}

}