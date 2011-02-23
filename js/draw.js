var Settings = {
	canvasId : 'twitter',
	canvasWidth : 400,
	canvasHeight : 400,
	iconSize : 24,
	partyIconSize : 10,
	maxFrameCount : 30,
	updateInterval : 40,
	spawnInterval : 4000,
	scoreX : 0,
	scoreY : 0,
	missLimit : 10,
};

var Twitter = {
	url : 'http://twitter.com/',
	api : 'http://twitter.com/',
};

var Score = {
	hit : 0,
	follower : 0,
	miss : 0,
};

function Character(name, id, x, y, width, height) {
	this.name   	= name;
	this.id     	= id;
	this.icon   	= createIcon(name, 'n');
	this.text    	= null;
	this.x      	= x;
	this.y      	= y;
	this.width      = width;
	this.height     = height;
	this.scaleX     = 1;
	this.scaleY     = 1;
	this.angle      = 0;
	this.frameCount = 0;
	this.action     = null;
	this.animation  = null;
};

var seq = (new Date).getTime();
var since_id = null;

var updateTimer = null;
var spawnTimer = null;

var spawnInterval = Settings.spawnInterval;

var canvas = null;
var ctx = null;

var currentMouse = {x:0, y:0};
var mouseLocus = [];

var statuses = [];

var pc = null;
var npcs = []; 
var party = [];
var miss = {};

var spawn = spawnRandom;

// Using TweetImages services 
// size 'n' is 48x48
function createIcon(name, size) {
	img = new Image();
	img.src = 'http://img.tweetimag.es/i/' + name + '_' + size;
	return img;
}

function xdomain(url, e) {
	if (e && e.parentNode)
		e.parentNode.removeChild(e);

	e = document.createElement('script');
	e.src = url;
	e.type = 'text/javascript';
	document.body.appendChild(e);

	return e;
}

function getFontFamily() {
	var ua = navigator.userAgent;
	if (ua.match(/Win(dows )?/)) {
		return 'MS PGothic';
	} 
	else if (ua.match(/Mac|PPC/)) {
		return 'HiraKakuPro-W6';
	}
	
	return 'aliastt-gothic-iso8859-1';
}

function random(limit) {
	return Math.floor(Math.random() * limit);
}

function rad(degree) {
	return degree*Math.PI/180;
}

function getCanvasFromId(id) {
	return document.getElementById(id);
}
 
function getCanvasContext(canvas) {
	var ctx = null;
	if (canvas && canvas.getContext) {
		ctx = canvas.getContext('2d');
	}
	else {
		alert ('Not supported browser.');
	}

	return ctx;
}

function getCanvasMouseLocation(e) {
	var rect = e.target.getBoundingClientRect();
	var loc = {};
	loc.x = e.clientX - rect.left;
	loc.y = e.clientY - rect.top;
	return loc;
}


function onMouseMove(e) {
	currentMouse = getCanvasMouseLocation(e);
}

function auth() {
	var e = null;
	xdomain(Twitter.api + 'account/verify_credentials.json?callback=authCallback&seq=' + (seq++), e);
}

function updateStatuses() {
	if (!pc) return auth();

	var e = null;
	xdomain(Twitter.api + 'statuses/home_timeline.json?seq=' + (seq++) + '&count=' + 200 +
			'&callback=updateStatusesCallback' + (since_id ? '&since_id=' + since_id : ''), e);
}

function authCallback(res) {
	pc = new Character(res.screen_name, res.id, 0, 0, Settings.iconSize, Settings.iconSize);
	pc.animation = animPulse;

	startGame();
}

function updateStatusesCallback(res) {
	if (res.error) return alert(res.error);

	if (since_id) {
		for (i=0; i<res.length; i++) {
			if (res[i].id <= since_id) res.splice(i--, 1);
		}
	}
 	since_id = res[0].id;
	
	for (i=0; i<res.length; i++) {
		statuses.push(res[i]);
	}
}

function initialize() {
	canvas = getCanvasFromId(Settings.canvasId);
	canvas.addEventListener('mousemove', onMouseMove);

	ctx = getCanvasContext(canvas);
};

function startGame() {
	updateStatuses();
	updateTimer = setInterval(update, Settings.updateInterval);
	spawnTimer = setInterval(function() { if (statuses.length > 0) { spawn(statuses.pop()) } }, spawnInterval);
};

function update() {
	if (!pc) return;

	if (Score.miss >= Settings.missLimit) {
		clearInterval(spawnTimer);
		clearInterval(updateTimer);
		drawGameOver(ctx);
		return;
	}

	pc.x = currentMouse.x; pc.y = currentMouse.y;
	pc.animation(pc);
	updateFrame(pc);

	for (i=0; i<npcs.length; ++i) {
		if (!npcs[i] || npcs[i].y >= Settings.canvasHeight) {
			++Score.miss;
			miss[npcs[i].name] += 1;
			npcs.splice(i--, 1);
		}
	}
	for (i=0; i<npcs.length; ++i) {
		npcs[i].action(npcs[i]);
		npcs[i].animation(npcs[i]);
		updateFrame(npcs[i]);
	}
	for (i=0; i<npcs.length; ++i) {
		if (hasHit(pc, npcs[i])) {
			joinParty(npcs[i]);
			npcs.splice(i--, 1);
			++Score.hit;
		}
	}

	for (i=0; i<party.length; i++) {
		for (j=0; j<party.length; j++) {
			if (i != j && party[i] && party[j] && party[i].name == party[j].name) {
				delete party[j];
			}
		}
	}

	for (i=0; i<party.length; ++i) {
		if (!party[i]) { party.splice(i--, 1); }
	}
	Score.follower = party.length;


	actTracking(party[0], pc);
	for (i=1; i<party.length; ++i) {
		actTracking(party[i], party[i-1]);
		party[i].animation(party[i]);
		updateFrame(party[i]);
	}

	var newSpawnInterval = 0; 
	newSpawnInterval = Math.max(Settings.spawnInterval-Score.hit*10, 10);
	if (newSpawnInterval < spawnInterval - 100) {
		if (spawnTimer) clearInterval(spawnTimer);
		spawnTimer = setInterval(function() { if (statuses.length > 0) { spawn(statuses.pop()) } }, newSpawnInterval);
		spawnInterval = newSpawnInterval;
	}

	if (since_id != null && statuses.length < 10) {
		updateStatuses();
	}

	drawBackGround(ctx);
	drawScore(ctx);
	//drawParty();
	for (i=0; i<party.length; ++i) {
		drawCharacter(ctx, party[i]);
	}
	//drawNpc();
	for (i=0; i<npcs.length; ++i) {
		drawCharacter(ctx, npcs[i]);
		drawStatus(ctx, npcs[i]);
	}
	//drawPc();
	drawCharacter(ctx, pc);
};

function hasHit(ch1, ch2) {
	var result = false;
	// とりあえず平均
	var r1 = (ch1.width+ch1.height)/2/2;
	var r2 = (ch2.width+ch2.height)/2/2;
	var dx = (ch1.x - ch2.x);
	var dy = (ch1.y - ch2.y);

	return dx*dx+dy*dy<=(r1+r2)*(r1+r2);
};

function updateFrame(ch) {
	if (ch.frameCount < Settings.maxFrameCount) {
		ch.frameCount++;
	}
	else {
		ch.frameCount = 0;
	}
}

function joinParty(ch) {
	ch.width = Settings.partyIconSize;
	ch.height = Settings.partyIconSize;

	party.push(ch);
}

function spawnRandom(stat) {
	var user = stat.user;
	var ch = new Character(user.screen_name, stat.id,
						   random(Settings.canvasWidth - 48*2) + 48,
						   0, Settings.iconSize, Settings.iconSize);
	ch.text = stat.text;
	ch.action = random(4) < 2 ? actFalling : actZigZaging;
	ch.animation = random(4) < 2 ? animPulse : animRotate;

	npcs.push(ch);
}

function animPulse(ch) {
	ch.scaleX = ch.frameCount ? ch.scaleX*1.002 : 1;
	ch.scaleY = ch.frameCount ? ch.scaleY*1.002 : 1;
};

function animRotate(ch) {
	ch.angle = rad(360/Settings.maxFrameCount*ch.frameCount);	
};

function actFalling(ch) {
	if (!ch) return;

	d = Settings.canvasHeight - ch.y;
	if (d < 1) { ch.y = Settings.canvasHeight; }
	else       { ch.y += d/60; }
};

function actZigZaging(ch) {
	if (!ch) return;

	d = {};
	d.y = Settings.canvasHeight - ch.y;

	if (d.y % 120 < 60) {
		d.x = Settings.canvasWidth - ch.x;
	}
	else {
		d.x = 0 - ch.x;
	}
	
	dir = Math.atan2(d.y, d.x);

	v = {};
	v.x = Math.cos(dir)*Math.abs(d.x)/(random(40) + 20);
	v.y = d.y/60;

	if (d.y < 1) { ch.y = Settings.canvasHeight; }
	else { 
		ch.x += v.x;
		ch.y += v.y;
	}
};

function actTracking(chFrom, chTo) {
	if (!chFrom || !chTo) return;

	d = {};
	d.x = chTo.x - chFrom.x;
	d.y = chTo.y - chFrom.y;

	dir = Math.atan2(d.y, d.x);
	
	v = {};
	v.x = Math.cos(dir)*Math.abs(d.x)/5;
	v.y = Math.sin(dir)*Math.abs(d.y)/5; 

	if (Math.abs(d.x) > 0) { chFrom.x += v.x; }
	else                   { chFrom.x  = chTo.x; }
	if (Math.abs(d.y) > 0) { chFrom.y += v.y; }
	else                   { chFrom.y  = chTo.y; }
}

function resetTransform(ctx) {
	ctx.setTransform(1, 0, 0, 1, 0, 0);
}

function roundedRectPath(ctx, x, y, width, height, radius) {
  ctx.moveTo(x, y + radius);
  ctx.lineTo(x, y + height - radius);
  ctx.quadraticCurveTo(x, y + height, x + radius, y + height);
  ctx.lineTo(x + width - radius, y + height);
  ctx.quadraticCurveTo(x + width, y + height, x + width, y + height - radius);
  ctx.lineTo(x + width, y + radius);
  ctx.quadraticCurveTo(x + width, y, x + width - radius, y);
  ctx.lineTo(x + radius, y);
  ctx.quadraticCurveTo(x, y, x, y + radius);
  ctx.closePath();
}

function balloonPath(ctx, x, y, width, height, anchor) {
	roundedRectPath(ctx, x + anchor, y, width, height, height/2);
	ctx.moveTo(x + anchor + height/2, y/3);
	ctx.lineTo(x, y + height/2);
	ctx.lineTo(x + anchor + height/2, y + height - y/3);
	ctx.closePath();
}

function drawBalloon(ctx, x, y, width, height, anchor) {
	ctx.beginPath();
	balloonPath(ctx, x, y, width, height, anchor);
	ctx.fill();
}

function drawBackGround(ctx) {
	var oldStyle = ctx.fillStyle;
	resetTransform(ctx);
	ctx.fillStyle = 'rgb(0, 0, 0)';
	ctx.fillRect(0, 0, Settings.canvasWidth, Settings.canvasHeight);
	ctx.fillStyle = oldStyle;
}

function drawScore(ctx) {
	var oldStyle = ctx.strokeStyle;
	resetTransform(ctx);
	ctx.font = "bold 18px '" + getFontFamily() + "'";
	ctx.fillStyle = 'rgb(212, 234, 255)';
	ctx.fillText('Hit', Settings.scoreX+5, Settings.scoreY+18);
	ctx.fillText(Score.hit, Settings.scoreX+100, Settings.scoreY+18);
	ctx.fillStyle = 'rgb(212, 234, 255)';
	ctx.fillText('Follower', Settings.scoreX+5, Settings.scoreY+18*2);
	ctx.fillText(Score.follower, Settings.scoreX+100, Settings.scoreY+18*2);
	ctx.fillStyle = 'rgb(255, 234, 212)';
	ctx.fillText('Miss', Settings.scoreX+5, Settings.scoreY+18*3);
	ctx.fillText(Score.miss + ' / ' + Settings.missLimit, Settings.scoreX+100, Settings.scoreY+18*3);
	ctx.strokeStyle = oldStyle;
}

function drawGameOver(ctx) {
	var oldStyle = ctx.fillStyle;
	resetTransform(ctx);
	ctx.fillStyle = 'rgb(255, 100, 100)';
	ctx.fillRect(0, 0, Settings.canvasWidth, Settings.canvasHeight);
	ctx.font = "bold 32px '" + getFontFamily() + "'";
	ctx.fillStyle = 'rgb(255, 255, 255)';
	ctx.fillText('Game Over', Settings.scoreX+5, Settings.scoreY+32);
	ctx.fillStyle = oldStyle;
}

function drawCharacter(ctx, ch) {
	if (!ch) return;

	resetTransform(ctx);
	ctx.translate(ch.x, ch.y);
	ctx.scale(ch.scaleX, ch.scaleY);
	ctx.rotate(ch.angle);
	ctx.translate(-ch.width/2, -ch.height/2);

	if (ch.icon.complete) {
		var oldStyle = ctx.fillStyle;
		ctx.fillStyle = 'rgb(230, 230, 230)';
		ctx.fillRect(-2, -2, ch.width+4, ch.height+4);
		ctx.fillStyle = oldStyle;

		ctx.drawImage(ch.icon, 0, 0, ch.width, ch.height);
	}
	else {
		var oldStyle = ctx.fillStyle;
		ctx.fillStyle = 'rgb(230, 230, 230)';
		ctx.fillRect(0, 0, ch.width, ch.height);
		ctx.fillStyle = oldStyle;
	}
}

function drawStatus(ctx, ch) {
	if (!ch) return;
	
	var width = 300, anchor = 6;

	var angle, textX;
	if (ch.x - ch.width/2 > Settings.canvasWidth/2) {
		angle = rad(180);
		textX = ch.x - ch.width/2 - width - anchor;
	}
	else {
		angle = 0;
		textX = ch.x + ch.width + anchor + ch.height/2;
	}

	resetTransform(ctx);
	ctx.translate(ch.x, ch.y);
	ctx.rotate(angle);
	ctx.translate(ch.width, -ch.height/2);

	var oldStyle = ctx.fillStyle;
		
	var alpha = 1-(1/Settings.canvasHeight*ch.y);
	alpha = alpha < 0 ? 1 : alpha;
	ctx.fillStyle = 'rgba(252, 252, 220, ' + alpha + ')';
	drawBalloon(ctx, 0, 0, width, ch.height, anchor);

	resetTransform(ctx);
	ctx.fillStyle = 'rgb(0, 0, 0)';
	ctx.font = "12px '" + getFontFamily() + "'";
	var drawText = null;
	if (ch.text.length > (width - ch.height - anchor)/12) {
		drawText = ch.text.substring(0, (width - ch.height - anchor)/12);
		drawText += '...';
	}
	else {
		drawText = ch.text;
	}
	ctx.fillText(drawText, textX, ch.y); 
	ctx.fillStyle = oldStyle;
}

function execute() {
	initialize();
	setTimeout(auth(), 0);
}
