package com.jerrylin.microservice.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private List<String> origin = new ArrayList<>();
	private Map<String, GroupPos> groups = new HashMap<>();
	public static final String TAG_GRP = "group:";
	public static final int TAG_GRP_COUNT = TAG_GRP.length();
	
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
	 * 回傳一個全新的List
	 * @param k
	 * @param content
	 * @return
	 */
	public List<String> append(TagKey k, String content){
		int end = getGroupPos(k).end;
		List<String> ss = new ArrayList<>();
		int c = origin.size();
		for(int i = 0; i < c; i++){
			String s = origin.get(i);
			String postfix = "";
			if(i == end){
				postfix = " " + content;
			}
			ss.add(s + postfix);
		}
		return ss;
	}
	/**
	 * 在關聯資料表中插入條件；<br>
	 * (在一對一、多對一的情境中)如果在關聯資料表中有設定條件，<br>
	 * 且join類型為LEFT JOIN，<br>
	 * 將LEFT JOIN換為INNER JOIN
	 * @param jcKey
	 * @param conds
	 * @param jKey
	 * @return
	 */
	public List<String> appendJoinConds(TagKey jcKey, String conds, TagKey jKey){
		List<String> ss = append(jcKey, conds);
		int start = getGroupPos(jKey).start;
		String original = ss.get(start);
		String newOne = original.replace("LEFT JOIN", "INNER JOIN");
		ss.set(start, newOne);
		return ss;
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
	/**
	 * 產出SqlCompose<br>
	 * 這個方法也允許連續標記，<br>
	 * 也就是tag下一個元素還是tag的情境
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
