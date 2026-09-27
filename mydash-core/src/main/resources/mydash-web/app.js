const byId = id => document.getElementById(id);
const TOKEN_KEY = "mydash.adminToken";

let currentView = "overview";
let extensionSignature = "";
let consoleAbortController = null;
let consoleReconnectTimer = null;
let consoleLastId = 0;
const MAX_CONSOLE_DOM_LINES = 500;

function duration(ms) {
  const seconds = Math.max(0, Math.floor(ms / 1000));
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);

  if (days) return days + "일 " + hours + "시간";
  if (hours) return hours + "시간 " + minutes + "분";
  return minutes + "분";
}

function token() {
  return sessionStorage.getItem(TOKEN_KEY) || "";
}

function showLogin(message) {
  byId("auth-gate").hidden = false;
  byId("auth-error").textContent = message || "";
  byId("admin-token").focus();
}

function hideLogin() {
  byId("auth-gate").hidden = true;
  byId("auth-error").textContent = "";
}

async function api(path, options) {
  const request = Object.assign({}, options || {});
  request.headers = Object.assign(
    {},
    request.headers || {},
    { Authorization: "Bearer " + token() }
  );
  request.cache = "no-store";

  const response = await fetch(path, request);

  if (response.status === 401) {
    sessionStorage.removeItem(TOKEN_KEY);
    stopConsoleStream();
    showLogin("토큰이 올바르지 않거나 더 이상 사용할 수 없습니다.");
    throw new Error("unauthorized");
  }

  return response;
}

function setActiveNav(selector) {
  document.querySelectorAll(".sidebar .nav").forEach(link => {
    link.classList.remove("active");
  });

  const active = document.querySelector(selector);
  if (active) active.classList.add("active");
}

function selectView(name) {
  currentView = name;

  document.querySelectorAll("[data-view]").forEach(view => {
    const selected = view.getAttribute("data-view") === name;
    view.hidden = !selected;
    view.classList.toggle("active-view", selected);
  });

  document.querySelectorAll("[data-extension-view]").forEach(view => {
    view.hidden = true;
  });

  setActiveNav('[data-view-link="' + name + '"]');

  if (name === "console") {
    startConsoleStream();
  } else {
    stopConsoleStream();
  }

  if (name === "players") refreshPlayers();
  if (name === "server-config") refreshServerProperties();
  if (name === "settings") refreshSettings();
}

function selectExtension(extension) {
  currentView = "extension:" + extension.id;

  document.querySelectorAll("[data-view]").forEach(view => {
    view.hidden = true;
  });

  document.querySelectorAll("[data-extension-view]").forEach(view => {
    view.hidden = true;
  });

  setActiveNav('[data-extension-link="' + extension.id + '"]');

  let view = document.querySelector(
    '[data-extension-view="' + extension.id + '"]'
  );

  if (!view) {
    view = document.createElement("section");
    view.className = "extension-view";
    view.setAttribute("data-extension-view", extension.id);

    const header = document.createElement("header");
    const titleWrap = document.createElement("div");

    const eyebrow = document.createElement("p");
    eyebrow.className = "eyebrow";
    eyebrow.textContent = "EXTENSION";

    const title = document.createElement("h1");
    title.textContent = extension.displayName;

    const subtitle = document.createElement("p");
    subtitle.textContent = "외부 모드/플러그인이 myDash에 등록한 패널입니다.";

    titleWrap.append(eyebrow, title, subtitle);
    header.appendChild(titleWrap);

    const frameShell = document.createElement("div");
    frameShell.className = "extension-frame-shell";

    const frame = document.createElement("iframe");
    frame.className = "extension-frame";
    frame.src = extension.route;
    frame.title = extension.displayName;

    frameShell.appendChild(frame);
    view.append(header, frameShell);

    const error = byId("error");
    error.parentNode.insertBefore(view, error);
  }

  view.hidden = false;
}

document.querySelectorAll("[data-view-link]").forEach(link => {
  link.addEventListener("click", event => {
    event.preventDefault();
    selectView(link.getAttribute("data-view-link"));
  });
});

async function refreshOverview() {
  const response = await api("/api/v1/server");
  if (!response.ok) throw new Error("HTTP " + response.status);

  const data = await response.json();

  byId("platform").textContent = data.platform;
  byId("mc-version").textContent = "Minecraft " + data.minecraftVersion;
  byId("players").textContent = data.onlinePlayers;
  byId("max-players").textContent = data.maxPlayers;
  byId("uptime").textContent = duration(data.uptimeMillis);

  const percent = data.maxPlayers
    ? Math.min(100, (data.onlinePlayers / data.maxPlayers) * 100)
    : 0;

  byId("player-bar").style.width = percent + "%";
}

async function refresh() {
  if (!token()) {
    showLogin();
    return;
  }

  try {
    await refreshOverview();
    await refreshExtensions();

    byId("error").textContent = "";
    hideLogin();

    if (currentView === "players") await refreshPlayers();
  } catch (error) {
    if (error.message !== "unauthorized") {
      byId("error").textContent =
        "서버 상태 API에 연결할 수 없습니다: " + error.message;
    }
  }
}

async function refreshExtensions() {
  if (!token()) return;

  const response = await api("/api/v1/extensions");
  const extensions = await response.json();

  if (!response.ok) {
    throw new Error(extensions.error || ("HTTP " + response.status));
  }

  const signature = JSON.stringify(
    extensions.map(extension => [
      extension.id,
      extension.displayName,
      extension.route
    ])
  );

  if (signature === extensionSignature) return;
  extensionSignature = signature;

  document.querySelectorAll("[data-extension-link]").forEach(link => {
    link.remove();
  });

  document.querySelectorAll("[data-extension-view]").forEach(view => {
    view.remove();
  });

  const nav = document.querySelector(".sidebar nav");

  extensions.forEach(extension => {
    const link = document.createElement("a");
    link.className = "nav extension-nav";
    link.href = "#extension-" + extension.id;
    link.setAttribute("data-extension-link", extension.id);

    const icon = document.createElement("span");
    icon.textContent = "◇";

    const label = document.createElement("span");
    label.textContent = extension.displayName;

    const badge = document.createElement("em");
    badge.textContent = "EXT";

    link.append(icon, label, badge);

    link.addEventListener("click", event => {
      event.preventDefault();
      selectExtension(extension);
    });

    nav.appendChild(link);
  });
}

async function refreshServerProperties() {
  if (!token()) return;

  const result = byId("server-properties-result");

  try {
    const response = await api("/api/v1/server-properties");
    const settings = await response.json();

    if (!response.ok) {
      throw new Error(settings.error || ("HTTP " + response.status));
    }

    byId("server-motd").value = settings.motd;
    byId("server-game-port").value = String(settings.serverPort);
    byId("server-max-players").value = String(settings.maxPlayers);
    byId("server-online-mode").checked = Boolean(settings.onlineMode);
    byId("server-whitelist").checked = Boolean(settings.whiteList);
    byId("server-difficulty").value = settings.difficulty;
    byId("server-gamemode").value = settings.gamemode;
    byId("server-hardcore").checked = Boolean(settings.hardcore);
    result.textContent = "";
  } catch (error) {
    if (error.message !== "unauthorized") {
      result.textContent =
        "server.properties를 불러오지 못했습니다: " + error.message;
    }
  }
}

async function refreshSettings() {
  if (!token()) return;

  const result = byId("settings-result");

  try {
    const response = await api("/api/v1/settings");
    const settings = await response.json();

    if (!response.ok) {
      throw new Error(settings.error || ("HTTP " + response.status));
    }

    byId("setting-bind").value = settings.bindAddress;
    byId("setting-port").value = String(settings.port);
    byId("setting-remote").checked = Boolean(settings.allowRemote);
    result.textContent = "";
  } catch (error) {
    if (error.message !== "unauthorized") {
      result.textContent =
        "설정을 불러오지 못했습니다: " + error.message;
    }
  }
}

async function refreshPlayers() {
  if (!token()) return;

  const status = byId("player-list-status");
  status.textContent = "불러오는 중";

  try {
    const response = await api("/api/v1/players");
    const players = await response.json();

    if (!response.ok) {
      throw new Error(players.error || ("HTTP " + response.status));
    }

    byId("player-count-detail").textContent = String(players.length);
    byId("operator-count").textContent = String(
      players.filter(player => player.operator).length
    );

    renderPlayers(players);
    status.textContent = players.length + "명 온라인";
  } catch (error) {
    if (error.message !== "unauthorized") {
      status.textContent = "오류";
      byId("error").textContent =
        "플레이어 목록을 불러오지 못했습니다: " + error.message;
    }
  }
}

function renderPlayers(players) {
  const list = byId("player-list");
  const empty = byId("player-empty");

  list.replaceChildren();
  empty.hidden = players.length !== 0;

  players.forEach(player => {
    const row = document.createElement("div");
    row.className = "player-row";

    const identity = document.createElement("div");
    identity.className = "player-id";

    const avatar = document.createElement("div");
    avatar.className = "player-avatar";
    avatar.textContent = (player.name || "?").slice(0, 1).toUpperCase();

    const meta = document.createElement("div");
    meta.className = "player-meta";

    const name = document.createElement("div");
    name.className = "player-name";

    const nameText = document.createElement("span");
    nameText.textContent = player.name;
    name.appendChild(nameText);

    if (player.operator) {
      const badge = document.createElement("span");
      badge.className = "op-badge";
      badge.textContent = "OP";
      name.appendChild(badge);
    }

    const uuid = document.createElement("span");
    uuid.className = "player-uuid";
    uuid.textContent = player.uuid;

    meta.append(name, uuid);
    identity.append(avatar, meta);

    const actions = document.createElement("div");
    actions.className = "player-actions";

    const kick = document.createElement("button");
    kick.className = "danger-button";
    kick.type = "button";
    kick.textContent = "킥";
    kick.addEventListener("click", () => kickPlayer(player));

    actions.appendChild(kick);
    row.append(identity, actions);
    list.appendChild(row);
  });
}

async function kickPlayer(player) {
  const reason = window.prompt(
    player.name + " 플레이어를 킥할 이유를 입력하세요.",
    "Kicked by myDash"
  );

  if (reason === null) return;

  try {
    const response = await api(
      "/api/v1/players/" + encodeURIComponent(player.uuid) + "/kick",
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason })
      }
    );

    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.error || ("HTTP " + response.status));
    }

    await refreshPlayers();
    await refreshOverview();
  } catch (error) {
    if (error.message !== "unauthorized") {
      byId("error").textContent =
        "플레이어 킥 실패: " + error.message;
    }
  }
}

byId("auth-form").addEventListener("submit", async event => {
  event.preventDefault();

  const value = byId("admin-token").value.trim();
  if (!value) return;

  sessionStorage.setItem(TOKEN_KEY, value);
  byId("admin-token").value = "";
  await refresh();
});

byId("lock-button").addEventListener("click", () => {
  sessionStorage.removeItem(TOKEN_KEY);
  stopConsoleStream();
  showLogin();
});

byId("clear-console").addEventListener("click", () => {
  const output = byId("console-output");
  output.replaceChildren();
  byId("console-empty").hidden = false;
});

byId("refresh-players").addEventListener("click", refreshPlayers);
byId("reload-server-properties").addEventListener("click", refreshServerProperties);

byId("server-properties-form").addEventListener("submit", async event => {
  event.preventDefault();

  const motd = byId("server-motd").value;
  const serverPort = Number(byId("server-game-port").value);
  const maxPlayers = Number(byId("server-max-players").value);
  const onlineMode = byId("server-online-mode").checked;
  const whiteList = byId("server-whitelist").checked;
  const difficulty = byId("server-difficulty").value;
  const gamemode = byId("server-gamemode").value;
  const hardcore = byId("server-hardcore").checked;
  const result = byId("server-properties-result");

  if (
    motd.length > 512 ||
    motd.includes("\n") ||
    motd.includes("\r") ||
    !Number.isInteger(serverPort) ||
    serverPort < 1 ||
    serverPort > 65535 ||
    !Number.isInteger(maxPlayers) ||
    maxPlayers < 1 ||
    maxPlayers > 100000
  ) {
    result.textContent = "입력한 서버 설정 값을 확인하세요.";
    return;
  }

  try {
    const response = await api("/api/v1/server-properties", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        motd,
        serverPort,
        maxPlayers,
        onlineMode,
        whiteList,
        difficulty,
        gamemode,
        hardcore
      })
    });

    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.error || ("HTTP " + response.status));
    }

    result.textContent = body.restartRequired
      ? "저장됨 · 서버 재시작 후 적용"
      : "저장됨";
  } catch (error) {
    if (error.message !== "unauthorized") {
      result.textContent =
        "server.properties 저장 실패: " + error.message;
    }
  }
});

byId("settings-form").addEventListener("submit", async event => {
  event.preventDefault();

  const bindAddress = byId("setting-bind").value.trim();
  const port = Number(byId("setting-port").value);
  const allowRemote = byId("setting-remote").checked;
  const result = byId("settings-result");

  if (!bindAddress || !Number.isInteger(port) || port < 1 || port > 65535) {
    result.textContent = "바인드 주소와 포트를 확인하세요.";
    return;
  }

  try {
    const response = await api("/api/v1/settings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ bindAddress, port, allowRemote })
    });

    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.error || ("HTTP " + response.status));
    }

    result.textContent = body.restartRequired
      ? "저장됨 · 서버 재시작 후 적용"
      : "저장됨";
  } catch (error) {
    if (error.message !== "unauthorized") {
      result.textContent = "설정 저장 실패: " + error.message;
    }
  }
});

byId("rotate-token").addEventListener("click", async () => {
  const confirmed = window.confirm(
    "관리자 토큰을 재발급하면 기존 토큰은 즉시 폐기됩니다. 계속할까요?"
  );

  if (!confirmed) return;

  try {
    const response = await api("/api/v1/auth/rotate", {
      method: "POST"
    });

    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.error || ("HTTP " + response.status));
    }

    sessionStorage.setItem(TOKEN_KEY, body.token);
    byId("new-token").textContent = body.token;
    byId("token-result").hidden = false;
  } catch (error) {
    if (error.message !== "unauthorized") {
      byId("error").textContent =
        "토큰 재발급 실패: " + error.message;
    }
  }
});

function setConsoleStreamStatus(text, active) {
  byId("console-stream-status").textContent = text;
  byId("console-stream-pulse").classList.toggle("inactive", !active);
}

function stopConsoleStream() {
  if (consoleReconnectTimer !== null) {
    clearTimeout(consoleReconnectTimer);
    consoleReconnectTimer = null;
  }

  if (consoleAbortController !== null) {
    consoleAbortController.abort();
    consoleAbortController = null;
  }

  if (currentView !== "console") {
    setConsoleStreamStatus("연결 대기", false);
  }
}

function scheduleConsoleReconnect() {
  if (currentView !== "console" || !token()) return;
  if (consoleReconnectTimer !== null) return;

  setConsoleStreamStatus("재연결 중", false);
  consoleReconnectTimer = setTimeout(() => {
    consoleReconnectTimer = null;
    startConsoleStream();
  }, 1500);
}

async function startConsoleStream() {
  if (currentView !== "console" || !token()) return;

  stopConsoleStream();
  setConsoleStreamStatus("연결 중", false);

  const controller = new AbortController();
  consoleAbortController = controller;

  const suffix = consoleLastId > 0
    ? "?after=" + encodeURIComponent(String(consoleLastId))
    : "";

  try {
    const response = await fetch("/api/v1/console/stream" + suffix, {
      method: "GET",
      headers: {
        Authorization: "Bearer " + token(),
        Accept: "text/event-stream"
      },
      cache: "no-store",
      signal: controller.signal
    });

    if (response.status === 401) {
      sessionStorage.removeItem(TOKEN_KEY);
      showLogin("토큰이 올바르지 않거나 더 이상 사용할 수 없습니다.");
      return;
    }

    if (!response.ok || !response.body) {
      throw new Error("HTTP " + response.status);
    }

    setConsoleStreamStatus("실시간 연결됨", true);

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let pending = "";

    while (true) {
      const chunk = await reader.read();
      if (chunk.done) break;

      pending += decoder.decode(chunk.value, { stream: true });
      pending = consumeSseFrames(pending);
    }

    pending += decoder.decode();
    consumeSseFrames(pending);

    if (!controller.signal.aborted) {
      scheduleConsoleReconnect();
    }
  } catch (error) {
    if (error.name !== "AbortError") {
      scheduleConsoleReconnect();
    }
  } finally {
    if (consoleAbortController === controller) {
      consoleAbortController = null;
    }
  }
}

function consumeSseFrames(buffer) {
  let separator;

  while ((separator = buffer.indexOf("\n\n")) >= 0) {
    const frame = buffer.slice(0, separator);
    buffer = buffer.slice(separator + 2);

    if (!frame || frame.startsWith(":")) continue;

    let eventId = 0;
    let eventName = "";
    const dataParts = [];

    frame.split("\n").forEach(line => {
      if (line.startsWith("id:")) {
        eventId = Number(line.slice(3).trim()) || 0;
      } else if (line.startsWith("event:")) {
        eventName = line.slice(6).trim();
      } else if (line.startsWith("data:")) {
        dataParts.push(line.slice(5).trimStart());
      }
    });

    if (eventName !== "line" || dataParts.length === 0) continue;
    if (eventId > 0 && eventId <= consoleLastId) continue;

    try {
      const log = JSON.parse(dataParts.join("\n"));
      appendServerConsoleLine(log);
      if (eventId > 0) consoleLastId = eventId;
    } catch (ignored) {
      // Ignore malformed stream records and continue with the next event.
    }
  }

  return buffer;
}

function appendServerConsoleLine(log) {
  const output = byId("console-output");
  const empty = byId("console-empty");
  if (empty) empty.hidden = true;

  const line = document.createElement("div");
  line.className = "console-line server-log";

  const meta = document.createElement("span");
  meta.className = "console-level console-level-" +
    String(log.level || "info").toLowerCase();

  const date = new Date(Number(log.timestamp) || Date.now());
  const time = date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit"
  });

  meta.textContent = time + " " + String(log.level || "INFO");

  const body = document.createElement("div");
  body.className = "console-message";

  const logger = document.createElement("small");
  logger.textContent = String(log.logger || "Minecraft");

  const message = document.createElement("pre");
  message.textContent = String(log.message || "");

  body.append(logger, message);
  line.append(meta, body);
  output.appendChild(line);

  while (output.querySelectorAll(".console-line").length > MAX_CONSOLE_DOM_LINES) {
    const first = output.querySelector(".console-line");
    if (!first) break;
    first.remove();
  }

  output.scrollTop = output.scrollHeight;
}

byId("command-form").addEventListener("submit", async event => {
  event.preventDefault();

  const input = byId("command-input");
  const command = input.value.trim();
  if (!command) return;

  const result = byId("command-result");
  result.textContent = "명령을 전송하는 중…";

  try {
    const response = await api("/api/v1/console", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ command })
    });

    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.error || ("HTTP " + response.status));
    }

    appendConsole(command);
    input.value = "";
    result.textContent = "명령을 서버 스레드에 전달했습니다.";
  } catch (error) {
    if (error.message !== "unauthorized") {
      result.textContent = "명령 실행 실패: " + error.message;
    }
  }
});

function appendConsole(command) {
  const output = byId("console-output");
  const empty = byId("console-empty");
  if (empty) empty.hidden = true;

  const line = document.createElement("div");
  line.className = "console-line local-command";

  const source = document.createElement("span");
  source.className = "console-level";
  source.textContent = "ADMIN";

  const body = document.createElement("div");
  body.className = "console-message";

  const message = document.createElement("pre");
  message.textContent = "> " + command;

  body.appendChild(message);
  line.append(source, body);
  output.appendChild(line);
  output.scrollTop = output.scrollHeight;
}

if (token()) {
  refresh();
} else {
  showLogin();
}

setInterval(() => {
  if (token()) refresh();
}, 3000);
