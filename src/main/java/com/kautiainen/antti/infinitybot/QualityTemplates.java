package com.kautiainen.antti.infinitybot;

import com.kautiainen.antti.infinitybot.model.FunctionalSpecial;
import com.kautiainen.antti.infinitybot.model.QualityTemplate;

/**
 * The registry of quality templates. 
 * @author Antti Kautiainen
 *
 */
public class QualityTemplates extends QualityRegistry {

	/**
	 * Get the serialization version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The default minimum level of the registered quality. 
	 * @return The default minimum level, if any exists. 
	 */
	public Integer getDefaultMinimumLevel() {
		return 0; 
	}
	
	/**
	 * The default maximum level of the registered quality. 
	 * @return The default maximum level, if any exists. 
	 */
	public Integer getDefaultMaximumLevel() {
		return 9; 
	}
	
	@Override
	public boolean register(com.kautiainen.antti.infinitybot.model.Special added) {
		if (added != null && added.getValue() == 0) {
			// We do have quality template to register. 
			if (added instanceof QualityTemplate) {
				// We are adding quality template.
				return super.register(added); 
			} else if (added instanceof FunctionalSpecial) {
				return super.register(new QualityTemplate((FunctionalSpecial)added));
			} else {
				// We are creating quality template from given special. 
				return super.register(new QualityTemplate(added)); 
			}
		} else {
			return false; 
		}
	}

	
}