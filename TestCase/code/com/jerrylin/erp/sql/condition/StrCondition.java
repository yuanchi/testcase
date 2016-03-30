package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.ISqlNode;


public class StrCondition extends SimpleCondition {
	private static final long serialVersionUID = 5371175197617721399L;
	
	private boolean caseInsensitive;
	private MatchMode matchMode;
	
	public boolean isCaseInsensitive() {
		return caseInsensitive;
	}
	public void setCaseInsensitive(boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
	}
	public MatchMode getMatchMode() {
		return matchMode;
	}
	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}
	@Override
	public ISqlNode singleCopy() {
		StrCondition c = new StrCondition();
		c.id(getId());
		c.propertyName(getPropertyName())
		 .operator(getOperator())
		 .type(getType())
		 .value(getValue());
		c.setCaseInsensitive(caseInsensitive);
		c.setMatchMode(matchMode);
		c.instruction(getInstruction());
		c.groupMark(getGroupMark());
		return c;
	}
	public enum MatchMode{
		ANYWHERE {
			@Override
			public String transformer(String input) {
				return "%"+input+"%";
			}
		},
		END {
			@Override
			public String transformer(String input) {
				return "%"+input;
			}
		},
		EXACT {
			@Override
			public String transformer(String input) {
				return input;
			}
		},
		START {
			@Override
			public String transformer(String input) {
				return input+"%";
			}
		};			
		public abstract String transformer(String input);
	}
}
