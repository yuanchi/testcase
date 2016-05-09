package com.jerrylin.erp.jackson.mixin;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface ParameterIgnoreLocalNames {
	@JsonIgnore public Map<String, String> getLocaleNames(); 
}
