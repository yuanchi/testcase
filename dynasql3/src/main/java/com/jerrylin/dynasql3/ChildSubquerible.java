package com.jerrylin.dynasql3;

import java.util.function.Consumer;

import com.jerrylin.dynasql3.node.SelectExpression;
import com.jerrylin.dynasql3.node.SqlNode;

public interface ChildSubquerible<Me extends SqlNode<?>> extends LastChildAliasible<Me>, ChildAddible<Me> {
	default Me subquery(Consumer<SelectExpression<?>> consumer){
		SelectExpression<?> child = createBy(SelectExpression.class);
		consumer.accept(child);
		return add(child);
	}
	default SelectExpression<?> subquery(){
		SelectExpression<?> child = createBy(SelectExpression.class);
		add(child);
		return child;
	}
}
