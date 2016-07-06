package com.jerrylin.query;
import static com.jerrylin.query.Expression.TARGET_ROLE;

public abstract class TargetSelectable extends SqlNode{
	private String feature;
	public TargetSelectable(String feature){
		this.feature = feature;
	}
	public SelectExpression startSelect(String alias){
		String previousIndent = toStart().getIndent();
		SelectExpression expression = new SelectExpression();
		expression.setIndent(previousIndent + baseIndent);
		expression.attr(TARGET_ROLE, feature);
		expression.setAlias(alias);
		addChildren(expression);
		return expression;
	}
	public SelectExpression startSelect(){
		return startSelect(null);
	}
	public TargetSelectable t(String expression, String alias){
		SimpleExpression se = new SimpleExpression();
		se.setExpression(expression);
		se.setAlias(alias);
		addChildren(se);
		return this;
	}
	public TargetSelectable t(String expression){
		t(expression, null);
		return this;
	}
	/**
	 * see the method {@link GroupConditions#removeNotRequiredSiblings(GroupConditions gc)}.
	 * @return
	 */
	public void removeNotRequiredSiblings(){
		findByType(SelectExpression.class)
		.getFound(SelectExpression.class)
		.forEach(se->se.removeNotRequiredSiblings());
	}
}
