<<<<<<< HEAD
package com.luajava;
import java.util.*;
import com.luajava.*;

public class LuaStack
{
	private final static HashMap<String,LuaState> luaStack=new HashMap<String,LuaState>();
	
	public static void put(String name,LuaState L){
		luaStack.put(name,L);
	}
	
	public static LuaState get(String name){
		return luaStack.get(name);
	}
	
	public static Object call(String name,String func,Object[] arg) throws LuaException{
		return new LuaFunction(get(name),func).call(arg);
	}
	
}
=======
package com.luajava;
import java.util.*;
import com.luajava.*;

public class LuaStack
{
	private final static HashMap<String,LuaState> luaStack=new HashMap<String,LuaState>();
	
	public static void put(String name,LuaState L){
		luaStack.put(name,L);
	}
	
	public static LuaState get(String name){
		return luaStack.get(name);
	}
	
	public static Object call(String name,String func,Object[] arg) throws LuaException{
		return new LuaFunction(get(name),func).call(arg);
	}
	
}
>>>>>>> d5ebc43 (Lua 5.5.0)
