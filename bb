<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Heart</title>
<style>
body {
    margin: 0;
    background: black;
    overflow: hidden;
}
canvas {
    position: absolute;
}
.bb {
    position: absolute;
    color: #ff69b4;
    font-size: 26px;
    opacity: 0.9;
    animation: floatBB 18s linear infinite;
}
@keyframes floatBB {
    0% { transform: translateY(100vh) translateX(0); }
    50% { transform: translateY(40vh) translateX(150px); }
    100% { transform: translateY(-100vh) translateX(-150px); }
}
</style>
</head>
<body>

<canvas id="heart"></canvas>

<script>
const canvas = document.getElementById("heart");
const ctx = canvas.getContext("2d");
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

let startTime = Date.now();

/* Shooting stars */
let stars = [];
function createStar(){
    stars.push({
        x: Math.random()*canvas.width,
        y: 0,
        speed: 5 + Math.random()*3,
        len: 120
    });
}
setInterval(createStar, 800);

/* Floating BB */
function createBB() {
    const bb = document.createElement("div");
    bb.className = "bb";
    bb.innerHTML = "BB";
    bb.style.left = Math.random() * 100 + "vw";
    document.body.appendChild(bb);
    setTimeout(() => bb.remove(), 18000);
}
setInterval(createBB, 6000);

/* Heart formula */
function heart(t) {
  return {
    x: 16 * Math.pow(Math.sin(t), 3),
    y: -(13 * Math.cos(t) - 5 * Math.cos(2*t) - 2 * Math.cos(3*t) - Math.cos(4*t))
  };
}

/* Text heart */
let textPoints = [];
for (let i = 0; i < 80; i++) {
  let t = i / 80 * Math.PI * 2;
  textPoints.push({ angle: t });
}

/* Particle heart original style */
var rand = Math.random;
var traceCount = 50;
var pointsOrigin = [];
var dr = 0.1;

function heartPosition(rad) {
    return [
        Math.pow(Math.sin(rad), 3),
        -(15 * Math.cos(rad) - 5 * Math.cos(2 * rad) - 2 * Math.cos(3 * rad) - Math.cos(4 * rad))
    ];
}

function scaleAndTranslate(pos, sx, sy, dx, dy) {
    return [dx + pos[0] * sx, dy + pos[1] * sy];
}

for (var i = 0; i < Math.PI * 2; i += dr)
    pointsOrigin.push(scaleAndTranslate(heartPosition(i), 210, 13, 0, 0));

for (var i = 0; i < Math.PI * 2; i += dr)
    pointsOrigin.push(scaleAndTranslate(heartPosition(i), 150, 9, 0, 0));

for (var i = 0; i < Math.PI * 2; i += dr)
    pointsOrigin.push(scaleAndTranslate(heartPosition(i), 90, 5, 0, 0));

var heartPointsCount = pointsOrigin.length;
var targetPoints = [];

function pulse(kx, ky) {
    for (var i = 0; i < pointsOrigin.length; i++) {
        targetPoints[i] = [];
        targetPoints[i][0] = kx * pointsOrigin[i][0] + canvas.width / 2;
        targetPoints[i][1] = ky * pointsOrigin[i][1] + canvas.height / 2;
    }
}

var e = [];
for (var i = 0; i < heartPointsCount; i++) {
    var x = rand() * canvas.width;
    var y = rand() * canvas.height;
    e[i] = {
        vx: 0,
        vy: 0,
        R: 2,
        speed: rand() + 5,
        q: ~~(rand() * heartPointsCount),
        D: 2 * (i % 2) - 1,
        force: 0.2 * rand() + 0.7,
        f: "hsla(0," + ~~(40 * rand() + 100) + "%," + ~~(60 * rand() + 20) + "%,.3)",
        trace: []
    };
    for (var k = 0; k < traceCount; k++)
        e[i].trace[k] = { x: x, y: y };
}

var config = {
    traceK: 0.4,
    timeDelta: 0.01
};

var time = 0;

function draw(){
    ctx.fillStyle = "rgba(0,0,0,.15)";
    ctx.fillRect(0,0,canvas.width,canvas.height);

    /* Shooting stars */
    ctx.strokeStyle = "white";
    ctx.lineWidth = 2;
    stars.forEach((s,i)=>{
        ctx.beginPath();
        ctx.moveTo(s.x,s.y);
        ctx.lineTo(s.x+s.len,s.y+s.len);
        ctx.stroke();
        s.x += s.speed;
        s.y += s.speed;
        if(s.y > canvas.height) stars.splice(i,1);
    });

    let elapsed = (Date.now() - startTime)/1000;

    /* TEXT HEART */
    if(elapsed > 5 && elapsed < 10){
        let scale = 18 + Math.sin(time) * 1.5;
        ctx.fillStyle = "pink";
        ctx.font = "16px Arial";

        textPoints.forEach(p=>{
            p.angle += 0.003;
            let h = heart(p.angle);
            let x = canvas.width/2 + h.x * scale;
            let y = canvas.height/2 + h.y * scale;
            ctx.fillText("I LOVE YOU", x, y);
        });
    }

    /* PARTICLE HEART ORIGINAL */
    if(elapsed >= 10){
        var n = -Math.cos(time);
        pulse((1 + n) * .5, (1 + n) * .5);
        time += ((Math.sin(time)) < 0 ? 9 : (n > 0.8) ? .2 : 1) * config.timeDelta;

        for (var i = e.length; i--;) {
            var u = e[i];
            var q = targetPoints[u.q];
            var dx = u.trace[0].x - q[0];
            var dy = u.trace[0].y - q[1];
            var length = Math.sqrt(dx * dx + dy * dy);

            u.vx += -dx / length * u.speed;
            u.vy += -dy / length * u.speed;
            u.trace[0].x += u.vx;
            u.trace[0].y += u.vy;
            u.vx *= u.force;
            u.vy *= u.force;

            for (k = 0; k < u.trace.length - 1;) {
                var T = u.trace[k];
                var N = u.trace[++k];
                N.x -= config.traceK * (N.x - T.x);
                N.y -= config.traceK * (N.y - T.y);
            }

            ctx.fillStyle = u.f;
            for (k = 0; k < u.trace.length; k++) {
                ctx.fillRect(u.trace[k].x, u.trace[k].y, 1, 1);
            }
        }
    }

    time += 0.03;
    requestAnimationFrame(draw);
}
draw();
</script>

</body>
</html>
