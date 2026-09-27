(() => {
  "use strict";
  const STORAGE_KEY="mydash.locale";
  const english={"관리자 인증":"Administrator authentication","서버 콘솔에 최초 1회 표시된 관리자 토큰을 입력하세요.":"Enter the administrator token shown once in the server console.","관리자 토큰":"Administrator token","myDash 열기":"Open myDash","토큰은 이 브라우저 탭의 세션에만 보관됩니다.":"The token is stored only for this browser tab session.","개요":"Overview","실시간 콘솔":"Live console","플레이어 관리":"Player management","서버 환경 및 파일 설정":"Server environment & files","설정":"Settings","Bearer 인증 활성화":"Bearer authentication enabled","API v1 보호됨":"API v1 protected","서버 개요":"Server overview","서버 상태와 관리 기능을 한눈에 확인하세요.":"View server status and management tools at a glance.","잠금":"Lock","온라인":"Online","연결 중":"Connecting","myDash가 서버 런타임과 연결되어 있습니다.":"myDash is connected to the server runtime.","접속 플레이어":"Online players","업타임":"Uptime","myDash가 감지한 서버 실행 시간":"Server runtime detected by myDash","· Bearer 인증":"· Bearer authentication","멀티로더 공통 코어":"Shared multi-loader core","동일한 인증·API·웹 UI를 NeoForge와 Fabric 어댑터가 공유합니다.":"The same authentication, API, and web UI are shared across platform adapters.","서버 로그를 실시간으로 확인하고 명령을 바로 실행합니다.":"View server logs in real time and run commands immediately.","로그 지우기":"Clear logs","연결 대기":"Waiting for connection","Minecraft 서버 스레드로 전달":"Sent to the Minecraft server thread","콘솔 탭을 열면 최근 로그와 실시간 로그가 표시됩니다.":"Recent and live logs appear when the console tab is open.","실행":"Run","플레이어 목록 및 제재 관리":"Players & moderation","현재 접속 중인 플레이어를 확인하고 관리합니다.":"View and manage currently connected players.","새로고침":"Refresh","현재 접속자":"Current players","실시간 접속 목록":"Live player list","관리자(OP)":"Operators (OP)","현재 접속자 기준":"Among currently connected players","접속 중인 플레이어":"Online players","불러오는 중":"Loading","현재 접속 중인 플레이어가 없습니다.":"No players are currently online.","자주 사용하는 server.properties 항목을 안전하게 관리합니다.":"Safely manage commonly used server.properties values.","서버 환경 설정":"Server environment settings","포트, 플레이어 정원, 인증, 난이도 등 서버 기본 구성을 변경합니다.":"Change core server settings such as port, player limit, authentication, and difficulty.","원래대로":"Reset","설정 저장":"Save settings","기본 네트워크 및 접속 설정":"Basic network and connection settings","서버 설명 문구 (MOTD)":"Server description (MOTD)","서버 목록에 표시되는 설명입니다.":"Description shown in the server list.","서버 포트":"Server port","최대 접속자":"Maximum players","온라인 모드 (정품 인증)":"Online mode (account authentication)","Mojang/Microsoft 계정 인증을 사용합니다.":"Use Mojang/Microsoft account authentication.","화이트리스트 사용":"Enable whitelist","등록된 플레이어만 접속하도록 제한합니다.":"Allow only registered players to connect.","게임플레이 환경 설정":"Gameplay settings","기본 난이도":"Default difficulty","평화로움 (Peaceful)":"Peaceful","쉬움 (Easy)":"Easy","보통 (Normal)":"Normal","어려움 (Hard)":"Hard","기본 게임 모드":"Default game mode","서바이벌 (Survival)":"Survival","크리에이티브 (Creative)":"Creative","모험 (Adventure)":"Adventure","관전 (Spectator)":"Spectator","하드코어 모드":"Hardcore mode","hardcore=true 설정을 사용합니다.":"Use the hardcore=true setting.","서버 파일 탐색기":"Server file browser","허용된 설정 경로와 모드 파일 목록만 표시합니다.":"Shows only approved configuration paths and mod files.","상위 폴더":"Parent folder","경로":"Path","이름":"Name","크기":"Size","표시할 파일이 없습니다.":"No files to display.","파일을 선택하세요":"Select a file","파일 저장":"Save file","JAR 등 바이너리 파일은 목록 확인만 가능합니다.":"Binary files such as JARs can only be listed.","최대 편집 크기 1 MB":"Maximum editable size: 1 MB","변경 내용은 서버 재시작 후 완전히 적용됩니다.":"Changes are fully applied after a server restart.","myDash는 지정된 항목만 수정하며, 나머지 server.properties 값은 유지합니다.":"myDash changes only selected fields and preserves the remaining server.properties values.","myDash 설정":"myDash settings","웹 서버 연결 설정과 관리자 인증을 관리합니다.":"Manage web server connectivity and administrator authentication.","재시작 적용":"Restart required","웹 서버 연결":"Web server connection","바인드 주소":"Bind address","기본값은 로컬 접속만 허용하는 127.0.0.1입니다.":"The default 127.0.0.1 allows local access only.","포트":"Port","변경 후 Minecraft 서버를 재시작해야 적용됩니다.":"Restart the Minecraft server after changing this setting.","외부 접속 허용":"Allow remote access","0.0.0.0 또는 외부 인터페이스 사용 시에만 켜세요.":"Enable only when using 0.0.0.0 or another external interface.","현재 토큰은 서버에 원문으로 저장되지 않습니다. 재발급하면 기존 토큰은 즉시 사용할 수 없게 됩니다.":"The current token is not stored in plaintext. Rotating it immediately invalidates the old token.","관리자 토큰 재발급":"Rotate administrator token","새 관리자 토큰":"New administrator token","이 값은 지금 한 번만 확인하고 안전한 곳에 보관하세요.":"This value is shown once. Store it securely now.","인터넷에 직접 노출하지 않는 것을 권장합니다.":"Direct internet exposure is not recommended.","외부 접속이 필요하면 HTTPS가 적용된 리버스 프록시나 터널 뒤에서 myDash를 운영하세요.":"For remote access, run myDash behind an HTTPS reverse proxy or secure tunnel.","서버 명령":"Server command","편집 가능한 텍스트 파일을 선택하면 내용이 표시됩니다.":"Select an editable text file to view its contents.","토큰이 올바르지 않거나 더 이상 사용할 수 없습니다.":"The token is invalid or no longer usable.","외부 모드/플러그인이 myDash에 등록한 패널입니다.":"A panel registered in myDash by an external mod/plugin.","서버 상태 API에 연결할 수 없습니다: ":"Could not connect to the server status API: ","편집 가능한 텍스트 파일을 선택하세요.":"Select an editable text file.","차단됨":"Blocked","폴더":"Folder","편집":"Edit","읽기 전용":"Read only","심볼릭 링크는 보안상 열 수 없습니다.":"Symbolic links cannot be opened for security reasons.","파일 목록을 불러오지 못했습니다.":"Could not load the file list.","파일 탐색기 오류: ":"File browser error: ","편집 중 · 저장 시 기존 파일은 .mydash-backups에 백업됩니다.":"Editing · The existing file will be backed up to .mydash-backups when saved.","파일을 열지 못했습니다: ":"Could not open file: ","저장하는 중…":"Saving…","저장 완료 · 기존 파일은 .mydash-backups에 백업되었습니다.":"Saved · The previous file was backed up to .mydash-backups.","파일 저장 실패: ":"File save failed: ","server.properties를 불러오지 못했습니다: ":"Could not load server.properties: ","설정을 불러오지 못했습니다: ":"Could not load settings: ","오류":"Error","플레이어 목록을 불러오지 못했습니다: ":"Could not load the player list: ","OP 해제":"Remove OP","OP 부여":"Grant OP","화이트리스트 +":"Whitelist +","화이트리스트 −":"Whitelist −","킥":"Kick","밴":"Ban","플레이어 관리 작업 실패: ":"Player management action failed: ","새 로그를 기다리는 중입니다.":"Waiting for new logs.","입력한 서버 설정 값을 확인하세요.":"Check the server setting values you entered.","저장됨 · 서버 재시작 후 적용":"Saved · Applied after server restart","저장됨":"Saved","server.properties 저장 실패: ":"Failed to save server.properties: ","바인드 주소와 포트를 확인하세요.":"Check the bind address and port.","설정 저장 실패: ":"Failed to save settings: ","관리자 토큰을 재발급하면 기존 토큰은 즉시 폐기됩니다. 계속할까요?":"Rotating the administrator token immediately invalidates the old token. Continue?","토큰 재발급 실패: ":"Token rotation failed: ","재연결 중":"Reconnecting","실시간 연결됨":"Live connection active","명령을 전송하는 중…":"Sending command…","명령을 서버 스레드에 전달했습니다.":"Command sent to the server thread.","명령 실행 실패: ":"Command failed: ","시작":"Start","재시작":"Restart","종료":"Stop","서버 관리자":"Server administrator","서버 런타임 정보":"Server runtime information","정상 가동 중":"Running normally","연속 가동 시간:":"Continuous uptime:","틱 상태 (TPS)":"Tick status (TPS)","텔레메트리 대기 중":"Waiting for telemetry","메모리 할당량":"Memory allocation","JVM 지표 연결 대기":"Waiting for JVM metrics","프로세서 점유율":"Processor usage","실시간 접속 목록과 연결됨":"Connected to live player list","성능 원격 측정 (텔레메트리)":"Performance telemetry","실시간 서버 지표 수집 모듈을 연결할 수 있습니다.":"A live server metrics collector can be connected.","24시간":"24 hours","텔레메트리 API 연결 대기":"Waiting for telemetry API","현재 데이터 없음":"No current data","실시간 이벤트 로그":"Live event log","실시간 피드":"Live feed","서버 런타임 API가 연결되었습니다.":"Server runtime API is connected.","실시간 로그 스트림을 사용할 수 있습니다.":"Live log streaming is available.","플레이어 관리 API가 준비되었습니다.":"Player management API is ready.","안전한 파일 편집 샌드박스가 활성화되어 있습니다.":"Safe file editing sandbox is active.","실시간 콘솔 및 터미널":"Live console & terminal","화면 비우기":"Clear screen","필터":"Filter","전체":"All","정보":"Info","채팅":"Chat","경고":"Warning","실시간 추적":"Live tracking","추천 명령어:":"Suggested commands:","명령어 또는 채팅 메시지를 입력하세요...":"Enter a command or chat message...","전송":"Send","명령어 내역":"Command history","자동완성":"Autocomplete","화이트리스트":"Whitelist","화이트리스트 등록":"Whitelisted players","API 확장 예정":"API extension planned","서버 기록 기반":"Based on server records","전체 플레이어":"All players","관리자":"Operators","상태 / 관리":"Status / Management","플레이어 상세 정보":"Player details","플레이어를 선택하세요":"Select a player","목록의 관리 버튼으로 OP, 화이트리스트, 킥, 밴 작업을 실행할 수 있습니다.":"Use the management buttons in the list for OP, whitelist, kick, and ban actions.","추방 (Kick)":"Kick","접속 차단 (Ban)":"Ban","파일 설정":"File settings","일반 설정":"General settings","파일 편집기":"File editor","게임규칙":"Game rules","월드 백업":"World backups","설정 저장 및 적용":"Save & apply settings","변경 사항":"Changes","서버 재시작 후 완전히 적용":"Fully applied after server restart","웹 대시보드 포트":"Web dashboard port","웹 인증 및 관리자 보안":"Web authentication & administrator security","웹 권한 및 제어 모듈":"Web permissions & control module","웹 RCON 원격 콘솔 명령":"Web remote console commands","Bearer 보호":"Bearer protected","플레이어 제어":"Player control","인증 필요":"Authentication required","웹 파일 편집기":"Web file editor","동일 출처 정책":"Same-origin policy","활성화":"Enabled","네트워크 보안":"Network security","마인크래프트 서버 내장 웹 서버, 관리자 웹 인증, 포트 바인딩 및 외부 연동 옵션을 설정합니다.":"Configure the embedded Minecraft web server, administrator web authentication, port binding, and remote integration options.","포트, 플레이어 정원, 난이도, 화이트리스트 등 서버 기본 구성을 직관적으로 설정합니다.":"Configure core server settings such as port, player limit, difficulty, and whitelist.","동작 중":"Running"};
  const localized={"ja":{"관리자 인증":"管理者認証","관리자 토큰":"管理者トークン","myDash 열기":"myDashを開く","개요":"概要","실시간 콘솔":"ライブコンソール","플레이어 관리":"プレイヤー管理","서버 환경 및 파일 설정":"サーバー環境とファイル","설정":"設定","서버 개요":"サーバー概要","잠금":"ロック","온라인":"オンライン","연결 중":"接続中","접속 플레이어":"オンラインプレイヤー","업타임":"稼働時間","로그 지우기":"ログを消去","연결 대기":"接続待機","실행":"実行","새로고침":"更新","관리자(OP)":"管理者(OP)","불러오는 중":"読み込み中","서버 환경 설정":"サーバー環境設定","설정 저장":"設定を保存","서버 파일 탐색기":"サーバーファイルブラウザー","파일을 선택하세요":"ファイルを選択","파일 저장":"ファイルを保存","myDash 설정":"myDash設定","웹 서버 연결":"Webサーバー接続","외부 접속 허용":"外部アクセスを許可","관리자 토큰 재발급":"管理者トークンを再発行","저장하는 중…":"保存中…","저장됨":"保存済み","오류":"エラー","OP 해제":"OP解除","OP 부여":"OP付与","킥":"キック","밴":"BAN","재연결 중":"再接続中","실시간 연결됨":"ライブ接続中"},"zh-CN":{"관리자 인증":"管理员认证","관리자 토큰":"管理员令牌","myDash 열기":"打开 myDash","개요":"概览","실시간 콘솔":"实时控制台","플레이어 관리":"玩家管理","서버 환경 및 파일 설정":"服务器环境与文件","설정":"设置","서버 개요":"服务器概览","잠금":"锁定","온라인":"在线","연결 중":"连接中","접속 플레이어":"在线玩家","업타임":"运行时间","로그 지우기":"清除日志","연결 대기":"等待连接","실행":"执行","새로고침":"刷新","관리자(OP)":"管理员(OP)","불러오는 중":"加载中","서버 환경 설정":"服务器环境设置","설정 저장":"保存设置","서버 파일 탐색기":"服务器文件浏览器","파일을 선택하세요":"选择文件","파일 저장":"保存文件","myDash 설정":"myDash 设置","웹 서버 연결":"Web 服务器连接","외부 접속 허용":"允许远程访问","관리자 토큰 재발급":"轮换管理员令牌","저장하는 중…":"正在保存…","저장됨":"已保存","오류":"错误","OP 해제":"取消 OP","OP 부여":"授予 OP","킥":"踢出","밴":"封禁","재연결 중":"正在重连","실시간 연결됨":"实时连接已建立"},"zh-TW":{"관리자 인증":"管理員驗證","관리자 토큰":"管理員權杖","myDash 열기":"開啟 myDash","개요":"總覽","실시간 콘솔":"即時主控台","플레이어 관리":"玩家管理","서버 환경 및 파일 설정":"伺服器環境與檔案","설정":"設定","서버 개요":"伺服器總覽","잠금":"鎖定","온라인":"線上","연결 중":"連線中","접속 플레이어":"線上玩家","업타임":"運行時間","로그 지우기":"清除日誌","연결 대기":"等待連線","실행":"執行","새로고침":"重新整理","관리자(OP)":"管理員(OP)","불러오는 중":"載入中","서버 환경 설정":"伺服器環境設定","설정 저장":"儲存設定","서버 파일 탐색기":"伺服器檔案瀏覽器","파일을 선택하세요":"選擇檔案","파일 저장":"儲存檔案","myDash 설정":"myDash 設定","웹 서버 연결":"Web 伺服器連線","외부 접속 허용":"允許遠端存取","관리자 토큰 재발급":"輪替管理員權杖","저장하는 중…":"儲存中…","저장됨":"已儲存","오류":"錯誤","OP 해제":"取消 OP","OP 부여":"授予 OP","킥":"踢出","밴":"封禁","재연결 중":"重新連線中","실시간 연결됨":"即時連線已建立"},"es":{"관리자 인증":"Autenticación de administrador","관리자 토큰":"Token de administrador","myDash 열기":"Abrir myDash","개요":"Resumen","실시간 콘솔":"Consola en vivo","플레이어 관리":"Gestión de jugadores","서버 환경 및 파일 설정":"Entorno y archivos","설정":"Ajustes","서버 개요":"Resumen del servidor","잠금":"Bloquear","온라인":"En línea","연결 중":"Conectando","접속 플레이어":"Jugadores en línea","업타임":"Tiempo activo","로그 지우기":"Borrar registros","연결 대기":"Esperando conexión","실행":"Ejecutar","새로고침":"Actualizar","관리자(OP)":"Operadores (OP)","불러오는 중":"Cargando","서버 환경 설정":"Configuración del servidor","설정 저장":"Guardar ajustes","서버 파일 탐색기":"Explorador de archivos","파일을 선택하세요":"Selecciona un archivo","파일 저장":"Guardar archivo","myDash 설정":"Ajustes de myDash","웹 서버 연결":"Conexión del servidor web","외부 접속 허용":"Permitir acceso remoto","관리자 토큰 재발급":"Rotar token de administrador","저장하는 중…":"Guardando…","저장됨":"Guardado","오류":"Error","OP 해제":"Quitar OP","OP 부여":"Dar OP","킥":"Expulsar","밴":"Banear","재연결 중":"Reconectando","실시간 연결됨":"Conexión en vivo activa"},"fr":{"관리자 인증":"Authentification administrateur","관리자 토큰":"Jeton administrateur","myDash 열기":"Ouvrir myDash","개요":"Aperçu","실시간 콘솔":"Console en direct","플레이어 관리":"Gestion des joueurs","서버 환경 및 파일 설정":"Environnement et fichiers","설정":"Paramètres","서버 개요":"Aperçu du serveur","잠금":"Verrouiller","온라인":"En ligne","연결 중":"Connexion","접속 플레이어":"Joueurs en ligne","업타임":"Temps de fonctionnement","로그 지우기":"Effacer les journaux","연결 대기":"En attente de connexion","실행":"Exécuter","새로고침":"Actualiser","관리자(OP)":"Opérateurs (OP)","불러오는 중":"Chargement","서버 환경 설정":"Paramètres du serveur","설정 저장":"Enregistrer","서버 파일 탐색기":"Explorateur de fichiers","파일을 선택하세요":"Sélectionnez un fichier","파일 저장":"Enregistrer le fichier","myDash 설정":"Paramètres myDash","웹 서버 연결":"Connexion du serveur web","외부 접속 허용":"Autoriser l'accès distant","관리자 토큰 재발급":"Renouveler le jeton administrateur","저장하는 중…":"Enregistrement…","저장됨":"Enregistré","오류":"Erreur","OP 해제":"Retirer OP","OP 부여":"Donner OP","킥":"Expulser","밴":"Bannir","재연결 중":"Reconnexion","실시간 연결됨":"Connexion en direct active"},"de":{"관리자 인증":"Administrator-Authentifizierung","관리자 토큰":"Administrator-Token","myDash 열기":"myDash öffnen","개요":"Übersicht","실시간 콘솔":"Live-Konsole","플레이어 관리":"Spielerverwaltung","서버 환경 및 파일 설정":"Serverumgebung & Dateien","설정":"Einstellungen","서버 개요":"Serverübersicht","잠금":"Sperren","온라인":"Online","연결 중":"Verbinden","접속 플레이어":"Online-Spieler","업타임":"Laufzeit","로그 지우기":"Logs löschen","연결 대기":"Warte auf Verbindung","실행":"Ausführen","새로고침":"Aktualisieren","관리자(OP)":"Operatoren (OP)","불러오는 중":"Laden","서버 환경 설정":"Servereinstellungen","설정 저장":"Einstellungen speichern","서버 파일 탐색기":"Server-Dateibrowser","파일을 선택하세요":"Datei auswählen","파일 저장":"Datei speichern","myDash 설정":"myDash-Einstellungen","웹 서버 연결":"Webserver-Verbindung","외부 접속 허용":"Remotezugriff erlauben","관리자 토큰 재발급":"Administrator-Token erneuern","저장하는 중…":"Speichern…","저장됨":"Gespeichert","오류":"Fehler","OP 해제":"OP entfernen","OP 부여":"OP geben","킥":"Kicken","밴":"Bannen","재연결 중":"Neu verbinden","실시간 연결됨":"Live-Verbindung aktiv"},"pt-BR":{"관리자 인증":"Autenticação de administrador","관리자 토큰":"Token de administrador","myDash 열기":"Abrir myDash","개요":"Visão geral","실시간 콘솔":"Console ao vivo","플레이어 관리":"Gerenciamento de jogadores","서버 환경 및 파일 설정":"Ambiente e arquivos","설정":"Configurações","서버 개요":"Visão geral do servidor","잠금":"Bloquear","온라인":"Online","연결 중":"Conectando","접속 플레이어":"Jogadores online","업타임":"Tempo ativo","로그 지우기":"Limpar logs","연결 대기":"Aguardando conexão","실행":"Executar","새로고침":"Atualizar","관리자(OP)":"Operadores (OP)","불러오는 중":"Carregando","서버 환경 설정":"Configurações do servidor","설정 저장":"Salvar configurações","서버 파일 탐색기":"Navegador de arquivos","파일을 선택하세요":"Selecione um arquivo","파일 저장":"Salvar arquivo","myDash 설정":"Configurações do myDash","웹 서버 연결":"Conexão do servidor web","외부 접속 허용":"Permitir acesso remoto","관리자 토큰 재발급":"Rotacionar token de administrador","저장하는 중…":"Salvando…","저장됨":"Salvo","오류":"Erro","OP 해제":"Remover OP","OP 부여":"Conceder OP","킥":"Expulsar","밴":"Banir","재연결 중":"Reconectando","실시간 연결됨":"Conexão ao vivo ativa"}};
  const meta=[["en","English"],["ko","한국어"],["ja","日本語"],["zh-CN","简体中文"],["zh-TW","繁體中文"],["es","Español"],["fr","Français"],["de","Deutsch"],["pt-BR","Português (Brasil)"]];
  const prefixes=["서버 상태 API에 연결할 수 없습니다: ","파일 탐색기 오류: ","파일을 열지 못했습니다: ","파일 저장 실패: ","server.properties를 불러오지 못했습니다: ","설정을 불러오지 못했습니다: ","플레이어 목록을 불러오지 못했습니다: ","플레이어 관리 작업 실패: ","server.properties 저장 실패: ","설정 저장 실패: ","토큰 재발급 실패: ","명령 실행 실패: "];

  function normalize(value){
    const raw=String(value||"").replace("_","-").toLowerCase();
    if(raw.startsWith("ko")) return "ko";
    if(raw.startsWith("ja")) return "ja";
    if(raw==="zh-tw"||raw==="zh-hk"||raw.includes("hant")) return "zh-TW";
    if(raw.startsWith("zh")) return "zh-CN";
    if(raw.startsWith("es")) return "es";
    if(raw.startsWith("fr")) return "fr";
    if(raw.startsWith("de")) return "de";
    if(raw.startsWith("pt")) return "pt-BR";
    return "en";
  }

  const locale=normalize(localStorage.getItem(STORAGE_KEY)||(navigator.languages&&navigator.languages[0])||navigator.language);

  function exact(text){
    if(locale==="ko") return text;
    return (localized[locale]&&localized[locale][text])||english[text]||text;
  }

  function translateCore(text){
    if(locale==="ko") return text;
    if(Object.prototype.hasOwnProperty.call(english,text)) return exact(text);

    let m=text.match(/^(\d+)일 (\d+)시간$/);
    if(m) return locale==="ja"?m[1]+"日 "+m[2]+"時間":locale.startsWith("zh")?m[1]+"天 "+m[2]+(locale==="zh-TW"?"小時":"小时"):m[1]+"d "+m[2]+"h";
    m=text.match(/^(\d+)시간 (\d+)분$/);
    if(m) return locale==="ja"?m[1]+"時間 "+m[2]+"分":locale.startsWith("zh")?m[1]+(locale==="zh-TW"?"小時 ":"小时 ")+m[2]+(locale==="zh-TW"?"分鐘":"分钟"):m[1]+"h "+m[2]+"m";
    m=text.match(/^(\d+)분$/);
    if(m) return locale==="ja"?m[1]+"分":locale.startsWith("zh")?m[1]+(locale==="zh-TW"?"分鐘":"分钟"):m[1]+"m";
    m=text.match(/^(\d+)명 온라인$/);
    if(m) return locale==="ja"?"オンライン "+m[1]+"人":locale.startsWith("zh")?m[1]+(locale==="zh-TW"?" 人線上":" 人在线"):m[1]+" "+(locale==="es"?"en línea":locale==="fr"?"en ligne":"online");

    m=text.match(/^(.+) 플레이어의 (킥|밴) 사유를 입력하세요\.$/);
    if(m){
      const action=exact(m[2]);
      if(locale==="ja") return m[1]+"を"+action+"する理由を入力してください。";
      if(locale==="zh-CN") return "请输入对 "+m[1]+" 执行“"+action+"”的原因。";
      if(locale==="zh-TW") return "請輸入對 "+m[1]+" 執行「"+action+"」的原因。";
      if(locale==="es") return "Introduce un motivo para "+action+" a "+m[1]+".";
      if(locale==="fr") return "Saisissez une raison pour "+action+" "+m[1]+".";
      if(locale==="de") return "Grund für "+action+" bei "+m[1]+" eingeben.";
      if(locale==="pt-BR") return "Digite um motivo para "+action+" "+m[1]+".";
      return "Enter a reason to "+action+" "+m[1]+".";
    }

    m=text.match(/^(.+) 플레이어의 OP 권한을 해제할까요\?$/);
    if(m){
      if(locale==="ja") return m[1]+"のOP権限を解除しますか？";
      if(locale==="zh-CN") return "要取消 "+m[1]+" 的 OP 权限吗？";
      if(locale==="zh-TW") return "要取消 "+m[1]+" 的 OP 權限嗎？";
      if(locale==="es") return "¿Quitar OP a "+m[1]+"?";
      if(locale==="fr") return "Retirer OP à "+m[1]+" ?";
      if(locale==="de") return "OP von "+m[1]+" entfernen?";
      if(locale==="pt-BR") return "Remover OP de "+m[1]+"?";
      return "Remove OP from "+m[1]+"?";
    }

    m=text.match(/^(.+) 파일은 목록 확인만 가능하며 웹에서 편집할 수 없습니다\.$/);
    if(m) return (locale==="en"?"":m[1]+" · ")+ (english["JAR 등 바이너리 파일은 목록 확인만 가능합니다."]||"Binary file") + (locale==="en"?"":"");

    for(const prefix of prefixes){
      if(text.startsWith(prefix)) return exact(prefix)+text.slice(prefix.length);
    }
    return text;
  }

  function translateText(text){
    const original=String(text==null?"":text);
    const trimmed=original.trim();
    if(!trimmed) return original;
    const translated=translateCore(trimmed);
    if(translated===trimmed) return original;
    return original.slice(0,original.indexOf(trimmed))+translated+original.slice(original.indexOf(trimmed)+trimmed.length);
  }

  function translateElement(element){
    if(!element||element.nodeType!==Node.ELEMENT_NODE) return;
    ["placeholder","title","aria-label"].forEach(name=>{
      if(element.hasAttribute(name)){
        const current=element.getAttribute(name);
        const translated=translateCore(current);
        if(translated!==current) element.setAttribute(name,translated);
      }
    });
  }

  function translateTree(root){
    if(locale==="ko"||!root) return;
    if(root.nodeType===Node.TEXT_NODE){
      const parent=root.parentElement;
      if(parent&&!["SCRIPT","STYLE","CODE","PRE","TEXTAREA"].includes(parent.tagName)){
        const next=translateText(root.nodeValue);
        if(next!==root.nodeValue) root.nodeValue=next;
      }
      return;
    }
    if(root.nodeType===Node.ELEMENT_NODE) translateElement(root);
    const walker=document.createTreeWalker(root,NodeFilter.SHOW_TEXT|NodeFilter.SHOW_ELEMENT);
    const nodes=[];
    while(walker.nextNode()) nodes.push(walker.currentNode);
    nodes.forEach(node=>{
      if(node.nodeType===Node.TEXT_NODE){
        const parent=node.parentElement;
        if(parent&&!["SCRIPT","STYLE","CODE","PRE","TEXTAREA"].includes(parent.tagName)){
          const next=translateText(node.nodeValue);
          if(next!==node.nodeValue) node.nodeValue=next;
        }
      } else translateElement(node);
    });
  }

  const nativeConfirm=window.confirm.bind(window);
  window.confirm=message=>nativeConfirm(translateCore(String(message)));
  const nativePrompt=window.prompt.bind(window);
  window.prompt=(message,defaultValue)=>nativePrompt(translateCore(String(message)),defaultValue);

  document.documentElement.lang=locale;
  translateTree(document.body);

  const observer=new MutationObserver(records=>{
    records.forEach(record=>{
      if(record.type==="characterData") translateTree(record.target);
      record.addedNodes.forEach(node=>translateTree(node));
    });
  });
  observer.observe(document.body,{subtree:true,childList:true,characterData:true});

  const picker=document.createElement("label");
  picker.className="language-picker";
  picker.id="mydash-language-picker";
  const select=document.createElement("select");
  select.setAttribute("aria-label","Language");
  meta.forEach(([code,label])=>{
    const option=document.createElement("option");
    option.value=code;
    option.textContent=label;
    option.selected=code===locale;
    select.appendChild(option);
  });
  select.addEventListener("change",()=>{
    localStorage.setItem(STORAGE_KEY,select.value);
    location.reload();
  });
  picker.appendChild(select);
  document.body.appendChild(picker);

  window.myDashI18n={locale,supportedLocales:meta.map(item=>item[0]),translate:translateCore};
})();
