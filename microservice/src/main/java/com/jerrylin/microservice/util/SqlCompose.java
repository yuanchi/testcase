package com.jerrylin.microservice.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SqlCompose {
	
	public static class GroupPos{
		public int start;
		public int end;
	}	
	public static class TagKey{
		public TagKey(String key){
			this.key = key;
		}
		public String key;
		@Override
		public String toString(){
			return this.key;
		}
		/**
		 * meaning group key
		 * @return
		 */
		public String gk(){
			return TAG_GRP+key;
		}
	}
	public static class RootAlias extends TagKey{
		public RootAlias(String key){
			super(key);
		}
	}
	public static class RootWhere extends TagKey{
		public RootWhere(String key) {
			super(key);
		}
	}
	public static class RootOrderBy extends TagKey{
		public RootOrderBy(String key) {
			super(key);
		}
	}
	public static class RootLimit extends TagKey{
		public RootLimit(String key) {
			super(key);
		}
	}
	public static class RootOffset extends TagKey{
		public RootOffset(String key) {
			super(key);
		}
	}
	
	private static final String JOIN_PREFIX = "join_"; 
	private static final String JOIN_CONDS_POSTFIX = "_conds";
	public static class JoinTarget extends TagKey{
		private JoinWhere jw;
		/**
		 * suggesting using alias the same as sql
		 * @param alias
		 */
		public JoinTarget(String alias){
			super(JOIN_PREFIX + alias);
		}
		public JoinWhere where(){
			if(jw == null){
				jw = new JoinWhere(key+JOIN_CONDS_POSTFIX);
			}
			jw.jt = this;
			return jw;
		}
	}
	
	public static class JoinWhere extends TagKey{
		private JoinTarget jt;
		public JoinWhere(String key){
			super(key);
		}
		public JoinTarget findTarget(){
			return jt;
		}
	}
	
	public static TagKey tk(String k){
		return new TagKey(k);
	}
	
	public static final String TAG_GRP = "group:";
	private static final int TAG_GRP_COUNT = TAG_GRP.length();
	
	public static final RootAlias R_ALIAS = new RootAlias("root_alias:");
	public static final RootWhere R_WHERE = new RootWhere("root_where");
	public static final RootOrderBy R_ORDER_BY = new RootOrderBy("root_orderBy");
	public static final RootLimit R_LIMIT = new RootLimit("root_limt");
	public static final RootOffset R_OFFSET = new RootOffset("root_offset");
	
	public static final String RA = R_ALIAS.gk();
	public static final String RW = R_WHERE.gk();
	public static final String ROB = R_ORDER_BY.gk();
	public static final String RL = R_LIMIT.gk();
	public static final String RO = R_OFFSET.gk();
	
	public static final String DEFAULT_ROOT_ALIAS = "p";
	
	
	private List<String> origin = new ArrayList<>();
	private Map<String, GroupPos> groups = new HashMap<>();
	private String rootAlias = DEFAULT_ROOT_ALIAS;

	public GroupPos getGroupPos(TagKey k){
		return groups.get(k.key);
	}
	
	public String getGroupRange(TagKey k){
		GroupPos gp = getGroupPos(k);
		List<String> target = origin.subList(gp.start, gp.end+1);
		return String.join("\n", target);
	}
	/**
	 * 這個方法不會改變origin的順序<br>
	 * 只會改變指定範圍最後一個元素內容<br>
	 * 將資料加在後面<br>
	 * @param k
	 * @param content
	 * @return
	 */
	public SqlCompose appendIn(TagKey k, String content){
		int end = getGroupPos(k).end;
		int c = origin.size();
		for(int i = 0; i < c; i++){
			if(i == end){
				origin.set(i, origin.get(i) + " " + content);
			}
		}
		return this;
	}
	private SqlCompose leftJoinToInnerIfCondsExisted(JoinTarget jtKey){
		int start = getGroupPos(jtKey).start;
		String original = origin.get(start);
		String newOne = original.replace("LEFT JOIN", "INNER JOIN");
		origin.set(start, newOne);
		return this;
	}
	/**
	 * 在關聯資料表中插入條件；<br>
	 * (在一對一、多對一的情境中)如果在關聯資料表中有設定條件，<br>
	 * 且join類型為LEFT JOIN，<br>
	 * 將LEFT JOIN換為INNER JOIN
	 * @param jtKey
	 * @param conds
	 * @return
	 */
	public SqlCompose appendInJoinConds(JoinTarget jtKey, String conds){
		appendIn(jtKey.where(), conds);
		return leftJoinToInnerIfCondsExisted(jtKey);
	}
	public SqlCompose replaceExactJoinConds(JoinTarget jtKey, String conds){
		replaceExact(jtKey.where(), conds);
		return leftJoinToInnerIfCondsExisted(jtKey);
	}
	public List<String> remove(Integer... idx){
		Set<Integer> range = new HashSet<>();
		for(Integer i : idx){
			range.add(i);
		}
		LinkedList<String> ss = new LinkedList<>();
		for(int i = origin.size()-1; i >= 0; i--){
			if(range.contains(i)){
				continue;
			}
			ss.addFirst(origin.get(i));
		} 

		return ss;		
	}
	public List<String> remove(TagKey... tks){
		List<Integer> range = new ArrayList<>();
		for(TagKey tk : tks){
			GroupPos gp = getGroupPos(tk);
			int start = gp.start;
			int end = gp.end;
			for(; start <= end; start++){
				range.add(start);
			}
		}
		return remove(range.toArray(new Integer[range.size()]));
	}
	public List<String> calcTotal(String selectReplaced, TagKey... rootToRemoved){
		List<Integer> removed = new ArrayList<>();
		for(TagKey r : rootToRemoved){
			GroupPos gp = getGroupPos(r);
			int start = gp.start;
			int end = gp.end;
			for(;start <= end; start++){
				removed.add(start);
			}
		}
		
		List<String> ss = remove(removed.toArray(new Integer[removed.size()]));
		if(selectReplaced == null){
			selectReplaced = "COUNT(DISTINCT "+ rootAlias +".id)";
		}
		ss.set(0, "SELECT " + selectReplaced);
		return ss;
	}
	public List<String> replaceWith(String content, int... idx){
		TreeSet<Integer> range = new TreeSet<>();
		for(Integer i : idx){
			range.add(i);
		}
		int first = range.first();
		LinkedList<String> ss = new LinkedList<>();
		for(int i = origin.size()-1; i >= 0; i--){
			if(range.contains(i)){
				if(i == first){
					ss.addFirst(content);
				}
				continue;
			}
			ss.addFirst(origin.get(i));
		} 

		return ss;		
	}
	public List<String> replaceWith(String content, TagKey tk){
		GroupPos gp = getGroupPos(tk);
		int start = gp.start;
		int end = gp.end;
		int count = end-start+1;
		int[] idx = new int[count];
		for(int i = 0; i < count; i++){
			idx[i] = start+i;
		}
		return replaceWith(content, idx);
	}
	/**
	 * replace origin content with new<br>
	 * contents length must be the same as line count of TagKey
	 * @param tk
	 * @param contents
	 * @return
	 */
	public SqlCompose replaceExact(TagKey tk, String... contents){
		GroupPos gp = getGroupPos(tk);
		int start = gp.start;
		int end = gp.end;
		int count = end-start+1;
		if(count != contents.length){
			throw new RuntimeException("contents count "+ contents.length +" must be the same as line count "+ count +" of TagKey: " + tk);
		}
		for(int i = start; i <= end; i++){
			origin.set(i, contents[i-start]);
		}
		return this;
	}
	public String joinWithBr(){
		return String.join("\n", origin);
	}
	public SqlCompose clone(){
		SqlCompose sc = new SqlCompose();
		for(String s : this.origin){
			sc.origin.add(s);
		}
		for(Map.Entry<String, GroupPos> m : this.groups.entrySet()){
			sc.groups.put(m.getKey(), m.getValue());
		}
		sc.rootAlias = rootAlias;
		return sc;
	}
	public List<String> getOrigin(){
		return this.origin;
	}
	/**
	 * generating sql to query pagination and calculating total count;<br>
	 * this method doesn't support one to many relations for now.<br>
	 * another issue is join conditions,<br>
	 * that is, if there're conditions in joined tables,<br>
	 * LEFT JOIN should be converted to INNER JOIN.
	 * @param params
	 * @return
	 */
	public String genPagingSql(PageParams params){
		SqlCompose sc = clone();
		sc.replaceExact(R_WHERE, params.get(R_WHERE))
			.replaceExact(R_ORDER_BY, params.get(R_ORDER_BY))
			.replaceExact(R_LIMIT, params.get(R_LIMIT))
			.replaceExact(R_OFFSET, params.get(R_OFFSET));
		
		for(Map.Entry<JoinWhere, String> e : params.joinWheres().entrySet()){
			JoinWhere jw = e.getKey();
			JoinTarget jt = jw.findTarget();
			sc.replaceExactJoinConds(jt, e.getValue());
		}
		
		String query = sc.joinWithBr();
		String calcTotal = String.join("\n", sc.calcTotal(null, R_ORDER_BY, R_LIMIT, R_OFFSET));
		// TODO considering prepared statement parameters
		String sql = query + "\n" + calcTotal;
		return sql;
	}
	/**
	 * responsible for generating SqlCompose.<br>
	 * it also allows to add tag continually,<br>
	 * that is to say, after tag is still a tag
	 * @param ss
	 * @return
	 */
	public static SqlCompose gen(String... ss){
		SqlCompose sc = new SqlCompose();
		int c = -1;
		for(String s : ss){
			if(s.startsWith(TAG_GRP)){
				if(s.startsWith(RA)){
					sc.rootAlias = s.substring(s.lastIndexOf(":")+1, s.length());
					continue;
				}
				String k = s.substring(TAG_GRP_COUNT);
				GroupPos gp = sc.getGroupPos(tk(k));
				if(gp == null){
					gp = new GroupPos();
					gp.start = c + 1;
					sc.groups.put(k, gp);
				}else{
					gp.end = c;
				}
				continue;
			}
			
			sc.origin.add(s);
			c++;
		}
		return sc;
	}

}
