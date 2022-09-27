package com.androlua;

import com.luajava.*;

public class LuaExAdapter extends LuaExpandableListAdapter 
{
	public LuaExAdapter(LuaContext context,  LuaTable groupLayout, LuaTable childLayout) throws LuaError {
		this(context,null,null,groupLayout,childLayout);
	}
	
	public LuaExAdapter(LuaContext context, LuaTable<Integer,LuaTable<String,Object>> groupData, LuaTable<Integer,LuaTable<Integer,LuaTable<String,Object>>> childData, LuaTable groupLayout, LuaTable childLayout) throws LuaError {
		super(context,groupData,childData,groupLayout,childLayout);
	}
}
