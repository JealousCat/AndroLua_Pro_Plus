require "import"
import "android.widget.*"
import "android.view.*"
import "android.app.*"
import "android.net.*"
import "android.content.*"
import "autotheme"

help=[===[
@关于@
@AndroLua是基于LuaJava开发的安卓平台轻量级脚本编程语言工具，既具有Lua简洁优雅的特质，又支持绝大部分安卓API，可以使你在手机上快速编写小型应用。

]===]
activity.setTitle("帮助")
activity.setTheme(autotheme())


list={}
for t,c in help:gmatch("(%b@@)\n*(%b@@)") do
    --print(t)
    t=t:sub(2,-2)
    c=c:sub(2,-2)
    list[t]=c
    list[#list+1]=t
    end

function show(v)
    local s=v.getText()
    local c=list[s]
    if c then
        help_dlg.setTitle(s)
        help_tv.setText(c)
        help_dlg.show()
        --  local adapter=ArrayAdapter(activity,android.R.layout.simple_list_item_1, String({c}))
        -- listview.setAdapter(adapter)
        end
    end



listview=ListView(activity)
listview.setOnItemClickListener(AdapterView.OnItemClickListener{
    onItemClick=function(parent, v, pos,id)
        show(v)
        end
    })
local adapter=ArrayAdapter(activity,android.R.layout.simple_list_item_1, String(list))
listview.setAdapter(adapter)
activity.setContentView(listview)

help_dlg=Dialog(activity,autotheme())
help_sv=ScrollView(activity)
help_tv=TextView(activity)
help_tv.setTextSize(20)
help_tv.TextIsSelectable=true
help_sv.addView(help_tv)
help_dlg.setContentView(help_sv)

func={}
func["捐赠"]=function()
    intent = Intent();
    intent.setAction("android.intent.action.VIEW");
    content_url = Uri.parse("https://qr.alipay.com/fkx18836ugaohvlja7yg5ef");
    intent.setData(content_url);
    activity.startActivity(intent);
    end
func["返回"]=function()
    activity.finish()
    end

items={"捐赠","返回"}
function onCreateOptionsMenu(menu)
    for k,v in ipairs(items) do
        m=menu.add(v)
        m.setShowAsActionFlags(1)
        end
    end

function onMenuItemSelected(id,item)
    func[item.getTitle()]()
    end




