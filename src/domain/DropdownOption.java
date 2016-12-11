package domain;

public class DropdownOption {

	private final String label;
	private final String value;
	private final boolean selected;
	private final boolean disabled;

	/**
	 * Default constructor
	 */
	public DropdownOption() {
		this(null, null, false, false);
	}

	/**
	 * All fields constructor
	 * 
	 * @param label
	 * @param value
	 * @param selected
	 * @param disabled
	 */
	public DropdownOption(String label, String value, boolean selected, boolean disabled) {
		super();
		this.label = label;
		this.value = value;
		this.selected = selected;
		this.disabled = disabled;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @return the disabled
	 */
	public boolean isDisabled() {
		return disabled;
	}
}
