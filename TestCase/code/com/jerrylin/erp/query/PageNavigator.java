package com.jerrylin.erp.query;

import java.io.Serializable;

public class PageNavigator implements Serializable {
	private static final long serialVersionUID = 509438435876622214L;

	private int totalCount = 0;
	private int previousPage = 1;
	private int nextPage = 1;
	private int currentPage = 1;
	private int totalPageCount = 1;
	private int countPerPage = 10;
	
	public PageNavigator(){}
	public PageNavigator(int totalCount, int countPerPage){
		this.totalCount = totalCount;
		this.countPerPage = countPerPage;
		countPageSize();
		setCurrentPage(currentPage);
	}
	
	public void countPageSize(){
		if(totalCount==0){
			totalPageCount = 1;
		}else{
			totalPageCount = totalCount / countPerPage;
			if(totalCount%countPerPage!=0){
				totalPageCount++;
			}
		}
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getPreviousPage() {
		return previousPage;
	}
	public void setPreviousPage(int previousPage) {
		this.previousPage = previousPage;
	}
	public int getNextPage() {
		return nextPage;
	}
	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		if(currentPage <= 1){
			previousPage = 1;
			currentPage = 1;
		}else{
			previousPage = currentPage - 1;
		}
		if(currentPage >= totalPageCount){
			nextPage = totalPageCount;
			currentPage = totalPageCount;
		}else{
			nextPage = currentPage + 1;
		}
		this.currentPage = currentPage;
	}
	public int getTotalPageCount() {
		return totalPageCount;
	}
	public void setTotalPageCount(int totalPageCount) {
		this.totalPageCount = totalPageCount;
	}
	public int getCountPerPage() {
		return countPerPage;
	}
	public void setCountPerPage(int countPerPage) {
		this.countPerPage = countPerPage;
	}
	public int countFirstResultIndex(){
		return countPerPage * (currentPage-1);
	}
}
