const byId=id=>document.getElementById(id);
const TOKEN_KEY="mydash.adminToken";

function duration(ms){
  const s=Math.max(0,Math.floor(ms/1000));
  const d=Math.floor(s/86400),h=Math.floor(s%86400/3600),m=Math.floor(s%3600/60);
  return d?d+"일 "+h+"시간":h?h+"시간 "+m+"분":m+"분";
}

function token(){return sessionStorage.getItem(TOKEN_KEY)||""}

function showLogin(message){
  byId("auth-gate").hidden=false;
  byId("auth-error").textContent=message||"";
  byId("admin-token").focus();
}

function hideLogin(){
  byId("auth-gate").hidden=true;
  byId("auth-error").textContent="";
}

async function api(path,options){
  const opts=Object.assign({},options||{});
  opts.headers=Object.assign({},opts.headers||{},{Authorization:"Bearer "+token()});
  opts.cache="no-store";
  const response=await fetch(path,opts);
  if(response.status===401){
    sessionStorage.removeItem(TOKEN_KEY);
    showLogin("토큰이 올바르지 않거나 더 이상 사용할 수 없습니다.");
    throw new Error("unauthorized");
  }
  return response;
}

async function refresh(){
  if(!token()){showLogin();return}
  try{
    const r=await api("/api/v1/server");
    if(!r.ok)throw new Error("HTTP "+r.status);
    const d=await r.json();
    byId("platform").textContent=d.platform;
    byId("mc-version").textContent="Minecraft "+d.minecraftVersion;
    byId("players").textContent=d.onlinePlayers;
    byId("max-players").textContent=d.maxPlayers;
    byId("uptime").textContent=duration(d.uptimeMillis);
    const pct=d.maxPlayers?Math.min(100,d.onlinePlayers/d.maxPlayers*100):0;
    byId("player-bar").style.width=pct+"%";
    byId("error").textContent="";
    hideLogin();
  }catch(e){
    if(e.message!=="unauthorized"){
      byId("error").textContent="서버 상태 API에 연결할 수 없습니다: "+e.message;
    }
  }
}

byId("auth-form").addEventListener("submit",async event=>{
  event.preventDefault();
  const value=byId("admin-token").value.trim();
  if(!value)return;
  sessionStorage.setItem(TOKEN_KEY,value);
  byId("admin-token").value="";
  await refresh();
});

byId("lock-button").addEventListener("click",()=>{
  sessionStorage.removeItem(TOKEN_KEY);
  showLogin();
});

byId("command-form").addEventListener("submit",async event=>{
  event.preventDefault();
  const input=byId("command-input");
  const command=input.value.trim();
  if(!command)return;

  const result=byId("command-result");
  result.textContent="명령을 전송하는 중…";

  try{
    const r=await api("/api/v1/console",{
      method:"POST",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({command})
    });
    const body=await r.json();
    if(!r.ok)throw new Error(body.error||("HTTP "+r.status));
    input.value="";
    result.textContent="명령을 서버 스레드에 전달했습니다.";
  }catch(e){
    if(e.message!=="unauthorized"){
      result.textContent="명령 실행 실패: "+e.message;
    }
  }
});

if(token())refresh();else showLogin();
setInterval(()=>{if(token())refresh()},3000);
