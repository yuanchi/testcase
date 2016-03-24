package com.jerrylin.erp.component;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.jerrylin.erp.query.PageNavigator;
@XmlRootElement
public class ConditionConfig<T> implements Serializable {
	private static final long serialVersionUID = 7981995302742567824L;
	private PageNavigator pageNavigator;
	private Map<String, Object> conds = new LinkedHashMap<>();
	private List<T> results = Collections.emptyList();
	private Map<String, String> msgs = new HashMap<>();
	
	@XmlAttribute
	public PageNavigator getPageNavigator() {
		return pageNavigator;
	}
	public void setPageNavigator(PageNavigator pageNavigator) {
		this.pageNavigator = pageNavigator;
	}
	@XmlAttribute
	public Map<String, Object> getConds() {
		return conds;
	}
	public void setConds(Map<String, Object> conds) {
		this.conds = conds;
	}
	@XmlAttribute
	public List<T> getResults() {
		return results;
	}
	public void setResults(List<T> results) {
		this.results = results;
	}
	@XmlAttribute
	public Map<String, String> getMsgs() {
		return msgs;
	}
	public void setMsgs(Map<String, String> msgs) {
		this.msgs = msgs;
	}
}
