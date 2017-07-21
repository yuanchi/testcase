package com.jerrylin.dynasql3;

import java.util.Arrays;
import java.util.List;

public class Pk {
	private List<String> compositeColumns;
	public List<String> getCompositeColumns() {
		return compositeColumns;
	}
	public void setCompositeColumns(List<String> compositeColumns) {
		this.compositeColumns = compositeColumns;
	}
	public static Pk init(List<String> compositeColumns){
		Pk pk = new Pk();
		pk.compositeColumns = compositeColumns;
		return pk;
	}
	public static Pk init(String column){
		Pk pk = init(Arrays.asList(column));
		return pk;
	}
	public static Pk init(String column1, String column2){
		Pk pk = init(Arrays.asList(column1, column2));
		return pk;
	}
}
