<<<<<<< HEAD
package com.luajava;

public interface LuaMetaTable
{
	public Object __call(Object...arg) throws LuaException;
	
	public Object __index(String key); 
	
	public void __newIndex(String key,Object value); 
}
=======
package com.luajava;

public interface LuaMetaTable
{
	public Object __call(Object...arg) throws LuaException;
	
	public Object __index(String key); 
	
	public void __newIndex(String key,Object value); 
}
>>>>>>> d5ebc43 (Lua 5.5.0)
