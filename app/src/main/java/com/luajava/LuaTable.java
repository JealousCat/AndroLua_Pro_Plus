package com.luajava;

import com.androlua.LuaActivity;

import java.io.IOException;
import java.util.*;

public class LuaTable <K, V>extends LuaObject implements Map <K,V>{

	@Override
	public void clear() {
		// TODO: Implement this method
		push();
		L.pushNil();
		while (L.next(-2) != 0) {
			L.pop(1);
			L.pushValue(-1);
			L.pushNil();
			L.setTable(-4);
		}
		L.pop(1);
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO: Implement this method
		boolean b=false;
		push();
		try {
			L.pushObjectValue(key);
			b = L.getTable(-2) != LuaState.LUA_TNIL;
			L.pop(1);
		}
		catch (LuaError e) {
			return false;
		}
		L.pop(1);
		return b;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO: Implement this method
		return false;
	}

	@Override
	public Set<Entry<K,V>> entrySet() {
		// TODO: Implement this method
		HashSet<Entry<K,V>> sets=new HashSet<Entry<K,V>>();
		push();
		L.pushNil();
		while (L.next(-2) != 0) {
			try {
				sets.add(new LuaEntry<K,V>((K)L.toJavaObject(-2), (V)L.toJavaObject(-1)));
			}
			catch (LuaError e) {}
			L.pop(1);
		}
		L.pop(1);
		return sets;
	}

	@Override
	public V get(Object key) {
		// TODO: Implement this method
		push();
		V obj=null;
		try {
			L.pushObjectValue(key);
			L.getTable(-2);
			obj = (V) L.toJavaObject(-1);
			L.pop(1);
		}
		catch (LuaError e) {}
		L.pop(1);
		return obj;
	}


	@Override
	public boolean isEmpty() {
		// TODO: Implement this method
		push();
		L.pushNil();
		boolean b=L.next(-2) == 0;
		if (b)
			L.pop(1);
		else
			L.pop(3);
		return b;
	}

	@Override
	public Set<K> keySet() {
		// TODO: Implement this method
		HashSet<K> sets=new HashSet<K>();
		push();
		L.pushNil();
		while (L.next(-2) != 0) {
			try {
				sets.add((K)L.toJavaObject(-2));
			}
			catch (LuaError e) {}
			L.pop(1);
		}
		L.pop(1);
		return sets;
	}

	@Override
	public V put(K key, V value) {
		// TODO: Implement this method
		push();
		try {
			L.pushObjectValue(key);
			L.pushObjectValue(value);
			L.setTable(-3);
		}
		catch (LuaError e) {}
		L.pop(1);
		return null;
	}

	@Override
	public void putAll(Map p1) {
		// TODO: Implement this method
	}

	@Override
	public V remove(Object key) {
		// TODO: Implement this method
		push();
		try {
			L.pushObjectValue(key);
			L.setTable(-2);
		}
		catch (LuaError e) {}
		L.pop(1);
		return null;
	}

	public boolean isList() {
		push();
		int len=L.rawLen(-1);
		if(len!=0){
			pop();
			return true;
		}
		L.pushNil();
		boolean b=L.next(-2) == 0;
		if (b)
			L.pop(1);
		else
			L.pop(3);
		return b;
	}

	public int length() {
		// TODO: Implement this method
		push();
		int len=L.rawLen(-1);
		pop();
		return len;
	}
	
	@Override
	public int size() {
		// TODO: Implement this method
		int n=0;
		push();
		L.pushNil();
		while (L.next(-2) != 0) {
			n++;
			L.pop(1);
		}
		L.pop(1);
		return n;
	}

	@Override
	public Collection<V> values() {
		ArrayList<V> sets=new ArrayList<>();
		push();
		L.pushNil();
		while (L.next(-2) != 0) {
			try {
				sets.add((V)L.toJavaObject(-1));
			}
			catch (LuaError e) {}
			L.pop(1);
		}
		L.pop(1);
		return sets;
	}



	protected LuaTable(LuaState L, String globalName) {
		super(L, globalName);
	}

	protected LuaTable(LuaState L, int index) {
		super(L, index);
	}

	public LuaTable(LuaState L) {
		super(L);
		L.newTable();
		registerValue(-1);
	}

	public class LuaEntry <K,V> implements Entry <K,V>{

		private K mKey;

		private V mValue;

		@Override
		public K getKey() {
			// TODO: Implement this method
			return mKey;
		}

		@Override
		public V getValue() {
			// TODO: Implement this method
			return mValue;
		}

		public V setValue(V value) {
			// TODO: Implement this method
			V old=mValue;
			mValue = value;
			return old;
		}

		public LuaEntry(K k, V v) {
			mKey = k;
			mValue = v;
		}
	}

	public Iterator iterator(){
		return new Iterator();
	}

	public class Iterator{
		private HashSet<LuaEntry<K,V>> set = null;
		public Iterator(){
			HashSet<LuaEntry<K,V>> sets=new HashSet<LuaEntry<K,V>>();
			push();
			L.pushNil();
			while (L.next(-2) != 0) {
				try {
					sets.add(new LuaEntry<K,V>((K)L.toJavaObject(-2), (V)L.toJavaObject(-1)));
				}
				catch (LuaError e) {}
				L.pop(1);
			}
			L.pop(1);
			set = sets;
		}
	}

	private static volatile Set<String> dumped = new HashSet<String>();

	public Object runFunc(String funcName, Object...args) {
		if (L != null) {
			try {
				L.setTop(0);
				L.getGlobal(funcName);
				if (L.isFunction(-1)) {
					L.getGlobal("debug");
					L.getField(-1, "traceback");
					L.remove(-2);
					L.insert(-2);

					int l=0;
					if (args != null)
						l = args.length;
					for (int i=0;i < l;i++) {
						L.pushObjectValue(args[i]);
					}

					int ok = L.pcall(l, 1, -2 - l);
					if (ok == 0) {
						return L.toJavaObject(-1);
					}
					throw new LuaError( "LuaTable error: " + L.toString(-1));
				}
			}
			catch (LuaError e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getcode(LuaTable t){
		if(t!=null) {
			return runFunc("tostring",t).toString();
		}
		return "0";
	}
	public Appendable dump(Appendable appendable,int i) throws IOException {
		Set<String> set = dumped;
		String code1 = getcode(this);
		if(set!=null){
			set.add(code1);
		}
		appendable.append("{");
		appendable.append("--(");
		appendable.append(code1);
		appendable.append(")\n");
		ArrayList<String> arrayList = new ArrayList<>(); // other key
		Map<Long,String> LongList = new HashMap<>(); //long key
		Map<Double,String> DbList = new HashMap<>(); //double key
		ArrayList<String> BoolList = new ArrayList<>(); //boolean key
		HashSet<LuaEntry<K,V>> it = iterator().set;
		for (LuaEntry le:it) {
			Object ke = le.getKey();
			Object valu = le.getValue();
			Appendable sb = new StringBuffer();
			Long islong = null;
			Double isdouble = null;
			boolean isboolean = false;
			if (ke != null) {
				if(ke instanceof LuaObject) {
					LuaObject key = (LuaObject) ke;
					if (!key.isNil()) {
						sb = concat(sb, i);
						sb.append("[");
						if (key.isInteger()) {
							long k = key.getInteger();
							sb.append(String.valueOf(k));
							islong = k;
						} else if (key.isNumber()) {
							double k = key.getNumber();
							sb.append(String.valueOf(k));
							isdouble = k;
						} else if (key.isBoolean()) {
							boolean k = key.checkboolean();
							sb.append(String.valueOf(k));
							isboolean = true;
						} else if (key.isString()) {
							sb.append('"');
							sb.append(key.getString());
							sb.append('"');
						} else if (key.isTable()) {
							LuaTable t = (LuaTable) key;
							String code = getcode(t);
							if (dumped.contains(code)) {
								sb.append("{ -- (");
								sb.append(code);
								sb.append(")\n");
								sb = concat(sb, i);
								sb.append("\t\t-- *** RECURSION *** --\n");
								sb = concat(sb, i);
								sb.append("}");
							} else {
								dumped.add(code);
								sb.append(t.dump(new StringBuffer(), i + 1).toString());
							}
						} else {
							sb.append('"');
							sb.append(key.toString());
							sb.append('"');
						}
						sb.append("] = ");
					}
				}else {
					sb = concat(sb, i);
					sb.append("[");
					if(ke instanceof Integer){
						long k = (long)ke;
						sb.append(String.valueOf(k));
						islong = k;
					}else if(ke instanceof Long){
						long k = (long)ke;
						sb.append(String.valueOf(k));
						islong = k;
					}else if(ke instanceof Double) {
						double k = (double)ke;
						sb.append(String.valueOf(k));
						isdouble = k;
					}else if(ke instanceof Boolean){
						boolean k = (boolean)ke;
						sb.append(String.valueOf(k));
						isboolean = true;
					}else if(ke instanceof String){
						sb.append('"');
						sb.append(ke.toString());
						sb.append('"');
					}else{
						sb.append('"');
						sb.append(ke.toString());
						sb.append('"');
					}
					sb.append("] = ");
				}
			}
			if(valu==null){
				sb.append("nil,\n");
			}else {
				if(valu instanceof LuaObject) {
					LuaObject value = (LuaObject) valu;
					if (value.isNil()) {
						sb.append("nil,\n");
					}else{
						if (value.isInteger()) {
							sb.append(String.valueOf(value.getInteger()));
						} else if (value.isNumber()) {
							sb.append(String.valueOf(value.getNumber()));
						} else if (value.isBoolean()) {
							sb.append(String.valueOf(value.checkboolean()));
						} else if (value.isString()) {
							sb.append('"');
							sb.append(value.getString());
							sb.append('"');
						} else if (value.isTable()) {
							LuaTable t = (LuaTable) value;
							String code = getcode(t);
							if (dumped.contains(code)) {
								sb.append("{ -- (");
								sb.append(code);
								sb.append(")\n");
								sb = concat(sb, i);
								sb.append("\t\t-- *** RECURSION *** --\n");
								sb = concat(sb, i);
								sb.append("}");
							} else {
								dumped.add(code);
								sb.append(t.dump(new StringBuffer(), i + 1).toString());
							}
						} else {
							sb.append('"');
							sb.append(value.toString());
							sb.append('"');
						}
						sb.append(",\n");
					}
				}else {
					if(valu instanceof Integer){
						sb.append(String.valueOf((int)valu));
					}else if(valu instanceof Long){
						sb.append(String.valueOf((long)valu));
					}else if(valu instanceof Double) {
						sb.append(String.valueOf((double)valu));
					}else if(valu instanceof Boolean){
						sb.append(String.valueOf((boolean)valu));
					}else if(valu instanceof String){
						sb.append('"');
						sb.append(valu.toString());
						sb.append('"');
					}else{
						sb.append('"');
						sb.append(valu.toString());
						sb.append('"');
					}
					sb.append(",\n");
				}
			}
			if(islong!=null){
				LongList.put(islong,sb.toString());
			}else if(isboolean){
				BoolList.add(sb.toString());
			}else if(isdouble!=null){
				DbList.put(isdouble,sb.toString());
			}else {
				arrayList.add(sb.toString());
			}
		}
		if(BoolList!=null&&!BoolList.isEmpty()){
			Collections.sort(BoolList);
			for(String str:BoolList){
				appendable.append(str);
			}
		}
		if(LongList!=null&&!LongList.isEmpty()){
			List<Long> list = new ArrayList<Long>();
			java.util.Iterator<Long> item = LongList.keySet().iterator();
			while(item.hasNext()){
				list.add(item.next());
			}
			Collections.sort(list);
			java.util.Iterator<Long> item2 = list.iterator();
			while(item2.hasNext()){
				Long key = item2.next();
				appendable.append(LongList.get(key));
			}
		}
		if(DbList!=null&&!DbList.isEmpty()){
			List<Double> list = new ArrayList<Double>();
			java.util.Iterator<Double> item = DbList.keySet().iterator();
			while(item.hasNext()){
				list.add(item.next());
			}
			Collections.sort(list);
			java.util.Iterator<Double> item2 = list.iterator();
			while(item2.hasNext()){
				Double key = item2.next();
				appendable.append(DbList.get(key));
			}
		}
		if(arrayList!=null&&!arrayList.isEmpty()){
			Collections.sort(arrayList);
			for(String str:arrayList){
				appendable.append(str);
			}
		}
		appendable = concat(appendable,i-1);
		appendable.append("}");
		return appendable;
	}

	public Appendable concat(Appendable appendable,int i){
		if(i<=0){return appendable;};
		try {
			for(int a=0;a<i;a++){
				appendable.append("\t");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return appendable;
	}

	public String tostring(){
		dumped = new HashSet<String>();
		String o = null;
		try {
			o = dump(new StringBuffer(),1).toString();
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		}
		dumped = null;
		return o;
	}
}
