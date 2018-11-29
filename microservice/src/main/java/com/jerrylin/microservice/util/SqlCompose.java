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
	}
	
	public static TagKey tk(String k){
		return new TagKey(k);
	}
	
	public static final String TAG_GRP = "group:";
	private static final int TAG_GRP_COUNT = TAG_GRP.length();
	
	public static final String TAG_ALI = "alias:";
	private static final int TAG_ALI_COUNT = TAG_ALI.length();
	
	private List<String> origin = new ArrayList<>();
	private Map<String, GroupPos> groups = new HashMap<>();

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
	/**
	 * 在關聯資料表中插入條件；<br>
	 * (在一對一、多對一的情境中)如果在關聯資料表中有設定條件，<br>
	 * 且join類型為LEFT JOIN，<br>
	 * 將LEFT JOIN換為INNER JOIN
	 * TODO considering distinguish between jcKey and jKey with subclass
	 * @param jcKey
	 * @param conds
	 * @param jKey
	 * @return
	 */
	public SqlCompose appendInJoinConds(TagKey jcKey, String conds, TagKey jKey){
		appendIn(jcKey, conds);
		int start = getGroupPos(jKey).start;
		String original = origin.get(start);
		String newOne = original.replace("LEFT JOIN", "INNER JOIN");
		origin.set(start, newOne);
		return this;
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
			selectReplaced = "COUNT(DISTINCT id)";
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
		for(int i = start; i <= end; start++){
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
		return sc;
	}
	public List<String> getOrigin(){
		return this.origin;
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
