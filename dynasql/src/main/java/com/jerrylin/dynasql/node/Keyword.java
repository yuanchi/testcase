package com.jerrylin.dynasql.node;

public class Keyword extends SqlNode<Keyword> {
	private static final long serialVersionUID = -548635988622266784L;
	public Keyword(){}
	public Keyword(String keyword){
		this.keyword = keyword;
	}
	private String keyword;
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	@Override
	public String genSql() {
		return keyword;
	}
	@Override
	public Keyword copy(SqlNode<?>parent){
		Keyword kw = new Keyword();
		kw.setKeyword(keyword);
		if(parent!=null){
			parent.addCopy(kw);
		} 
		copyTo(kw);
		return kw;
	}
}
