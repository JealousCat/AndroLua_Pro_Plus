package com.luajava;

public interface LuaMetaTable
{
	public Object __call(Object...arg) throws LuaError;
	
	public Object __index(String key); 
	
	public void __newIndex(String key,Object value); 
}
