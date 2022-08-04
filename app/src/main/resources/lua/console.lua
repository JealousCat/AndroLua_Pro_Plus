console = {}

local function _format()
  local p=0
  return function(str)
    str=str:gsub("[ \t]+$","")
    str=string.format('%s%s',string.rep(' ',p),str)
    p=p+ps(str)
    return str
  end
end


console.format = function(Text)
  local t=os.clock()
  local Format=_format()
  Text=Text:gsub('[ \t]*([^\r\n]+)',function(str)return Format(str)end)
  print('操作完成,耗时:'..os.clock()-t)
  return Text
end

console.build = function(path)
  if path then
    local str,st=loadfile(path)
    if st then
      return nil,st
    end
    local path=path..'c'

    local st,str=pcall(string.dump,str,true)
    if st then
      f=io.open(path,'wb')
      f:write(str)
      f:close()
      return path
    else
      os.remove(path)
      return nil,str
    end
  end
end

console.build_aly = function(path2)
  if path2 then
    local f,st=io.open(path2)
    if st then
      return nil,st
    end
    local str=f:read("*a")
    f:close()
    str=string.format("local layout=%s\nreturn layout",str)
    local path=path2..'c'
    str,st=loadstring(str,path2:match("[^/]+/[^/]+$"),"bt")
    if st then
      return nil,st:gsub("%b[]",path2,1)
    end

    local st,str=pcall(string.dump,str,true)
    if st then
      f=io.open(path,'wb')
      f:write(str)
      f:close()
      return path
    else
      os.remove(path)
      return nil,str
    end
  end
end

return console

