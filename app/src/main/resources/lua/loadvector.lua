local context=activity or service

local LuaBitmap=luajava.bindClass "com.androlua.LuaBitmap"
local function loadvector(path)
  if not path:find("^https*://") and not path:find("%.%a%a%a%a?$") then
    path=path..".xml"
  end
  if not path:find("^/") then
    return LuaBitmap.getVectorDrawable(context,string.format("%s/%s",luajava.luadir,path))
  else
    return LuaBitmap.getVectorDrawable(context,path)
  end
end

return loadvector
