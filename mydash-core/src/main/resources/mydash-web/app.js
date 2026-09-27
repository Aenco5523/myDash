const byId=id=>document.getElementById(id);
const TOKEN_KEY="mydash.adminToken";
let currentView="overview";

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

function selectView(name){
  currentView=name;
  document.querySelectorAll("[data-view]").forEach(view=>{
    view.hidden=view.getAttribute("data-view")!==name;
    view.classList.toggle("active-view",!view.hidden);
  });
  document.querySelectorAll("[data-view-link]").forEach(link=>{
    link.classList.toggle("active",link.getAttribute("data-view-link")===name);
  });
  if(name==="players") refreshPlayers();
}

document.querySelectorAll("[data-view-link]").forEach(link=>{
  link.addEventListener("click",event=>{
    event.preventDefault();
    selectView(link.getAttribute("data-view-link"));
  });
});

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
    if(currentView==="players") refreshPlayers();
  }catch(e){
    if(e.message!=="unauthorized"){
      byId("error").textContent="서버 상태 API에 연결할 수 없습니다: "+e.message;
    }
  }
}

async function refreshPlayers(){
  if(!token())return;
  const status=byId("player-list-status");
  status.textContent="불러오는 중";
  try{
    const r=await api("/api/v1/players");
    const players=await r.json();
    if(!r.ok)throw new Error(players.error||("HTTP "+r.status));

    byId("player-count-detail").textContent=String(players.length);
    byId("operator-count").textContent=String(players.filter(player=>player.operator).length);
    renderPlayers(players);
    status.textContent=players.length+"명 온라인";
  }catch(e){
    if(e.message!=="unauthorized"){
      status.textContent="오류";
      byId("error").textContent="플레이어 목록을 불러오지 못했습니다: "+e.message;
    }
  }
}

function renderPlayers(players){
  const list=byId("player-list");
  const empty=byId("player-empty");
  list.replaceChildren();
  empty.hidden=players.length!==0;

  players.forEach(player=>{
    const row=document.createElement("div");
    row.className="player-row";

    const identity=document.createElement("div");
    identity.className="player-id";

    const avatar=document.createElement("div");
    avatar.className="player-avatar";
    avatar.textContent=(player.name||"?").slice(0,1).toUpperCase();

    const meta=document.createElement("div");
    meta.className="player-meta";

    const name=document.createElement("div");
    name.className="player-name";
    const text=document.createElement("span");
    text.textContent=player.name;
    name.appendChild(text);

    if(player.operator){
      const badge=document.createElement("span");
      badge.className="op-badge";
      badge.textContent="OP";
      name.appendChild(badge);
    }

    const uuid=document.createElement("span");
    uuid.className="player-uuid";
    uuid.textContent=player.uuid;

    meta.append(name,uuid);
    identity.append(avatar,meta);

    const actions=document.createElement("div");
    actions.className="player-actions";
    const kick=document.createElement("button");
    kick.className="danger-button";
    kick.type="button";
    kick.textContent="킥";
    kick.addEventListener("click",()=>kickPlayer(player));
    actions.appendChild(kick);

    row.append(identity,actions);
    list.appendChild(row);
  });
}

async function kickPlayer(player){
  const reason=window.prompt(player.name+" 플레이어를 킥할 이유를 입력하세요.","Kicked by myDash");
  if(reason===null)return;

  try{
    const r=await api("/api/v1/players/"+encodeURIComponent(player.uuid)+"/kick",{
      method:"POST",
      headers:{"Content-Type":"application/json"},
      body:JSON.stringify({reason})
    });
    const body=await r.json();
    if(!r.ok)throw new Error(body.error||("HTTP "+r.status));
    await refreshPlayers();
    await refresh();
  }catch(e){
    if(e.message!=="unauthorized"){
      byId("error").textContent="플레이어 킥 실패: "+e.message;
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

byId("refresh-players").addEventListener("click",refreshPlayers);

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
    appendConsole(command);
    input.value="";
    result.textContent="명령을 서버 스레드에 전달했습니다.";
  }catch(e){
    if(e.message!=="unauthorized"){
      result.textContent="명령 실행 실패: "+e.message;
    }
  }
});

function appendConsole(command){
  const line=document.createElement("div");
  line.className="console-line";
  const source=document.createElement("span");
  source.textContent="admin";
  const message=document.createElement("p");
  message.textContent="> "+command;
  line.append(source,message);
  byId("console-output").appendChild(line);
  byId("console-output").scrollTop=byId("console-output").scrollHeight;
}

if(token())refresh();else showLogin();
setInterval(()=>{if(token())refresh()},3000);
