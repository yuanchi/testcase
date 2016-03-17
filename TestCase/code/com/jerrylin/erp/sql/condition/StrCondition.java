package com.jerrylin.erp.sql.condition;


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
