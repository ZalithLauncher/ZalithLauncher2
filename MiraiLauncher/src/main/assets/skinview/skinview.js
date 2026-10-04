const container = document.getElementById("skin-container");

const getWidth = () => container.clientWidth || window.innerWidth || 300;
const getHeight = () => container.clientHeight || window.innerHeight || 400;

const skinViewer = new skinview3d.SkinViewer({
    canvas: document.createElement("canvas"),
    width: getWidth(),
    height: getHeight()
});

container.appendChild(skinViewer.canvas);

//参考 Modrinth 启动器默认的 idle 动画
//https://github.com/modrinth/code/blob/e71a8c10fac3eda05ff9bc34381178f3d11c41de/packages/assets/models/slim-player.gltf#L1653-L1755
class IdleAnimation extends skinview3d.PlayerAnimation {
    constructor() {
        super();
        this.elapsed = 0;
        this.maxDelta = 0.05;
    }

    getCapeRotation(t) {
        const wave1 = Math.sin(t * Math.PI * 2 * 0.3);
        const wave2 = Math.sin(t * Math.PI * 2 * 0.25) * 0.3;

        let normalized = (wave1 + wave2 + 1.3) / 2.6;
        normalized = Math.max(0, Math.min(1, normalized));

        const angleX = 0.15 + normalized * 0.15;

        const waveZ = Math.sin(t * Math.PI * 2 * 0.35);
        const angleZ = 0.03 + (waveZ + 1) / 2 * 0.03;

        return { x: angleX, z: angleZ };
    }

    animate(player, delta) {
        const dt = Math.min(delta, this.maxDelta);
        this.elapsed += dt;

        const t = this.elapsed;

        const breathe = Math.sin(t * 1.2) * 0.04;

        player.skin.body.position.y = Math.sin(t * 2.2) * -0.1 - 6.1;
        player.skin.head.position.y = Math.sin(t * 2.2) * -0.1 - 0.1;

        player.skin.body.rotation.x = Math.sin(t * 2.2) * 0.02;
        player.skin.body.rotation.z = Math.sin(t * 1.5) * 0.01;

        player.skin.head.rotation.y = Math.sin(t * 0.6) * 0.1;
        player.skin.head.rotation.x = Math.sin(t * 2.2) * 0.04;
        player.skin.head.rotation.z = Math.sin(t * 1.5) * 0.01;

        const armX = Math.sin(t * 2.2) * 0.05;
        const armZ = (Math.sin(t * 1.8) + 1) / 2 * -0.05;

        player.skin.rightArm.rotation.x = armX;
        player.skin.leftArm.rotation.x = armX;

        player.skin.rightArm.rotation.z = armZ;
        player.skin.leftArm.rotation.z = -armZ;

        if (player.cape) {
            const capeRot = this.getCapeRotation(t);
            player.cape.rotation.x = capeRot.x;
            player.cape.rotation.z = capeRot.z;
        }
    }

    reset() {
        this.elapsed = 0;
    }
}

function startAnim(name, speed) {
    if (typeof name !== 'string' || name.trim() === '') {
        console.warn('The animation name must be a non-empty string');
        return;
    }

    let speed0 = null;
    if (typeof speed === 'number' && !isNaN(speed) && speed > 0) {
        speed0 = speed;
    }

    let anim;
    switch (name) {
        case "DefaultIdle":
            anim = new skinview3d.IdleAnimation();
            break;
        case "NewIdle":
            anim = new IdleAnimation();
            break;
        case "Walking":
            anim = new skinview3d.WalkingAnimation();
            break;
        case "Running":
            anim = new skinview3d.RunningAnimation();
            break;
        case "Flying":
            anim = new skinview3d.FlyingAnimation();
            break;
        case "Wave":
            anim = new skinview3d.WaveAnimation();
            break;
        case "Crouch":
            anim = new skinview3d.CrouchAnimation();
            break;
        case "Hit":
            anim = new skinview3d.HitAnimation();
            break;
        default:
            return;
    }

    skinViewer.animation = anim;
    if (speed0 !== null && skinViewer.animation) {
        skinViewer.animation.speed = speed0;
    }
}

skinViewer.controls.enableRotate = true;
skinViewer.controls.enableZoom = false;
skinViewer.controls.enablePan = false;

//记录默认的相机位置和控制器目标点
const defaultCameraPos = skinViewer.camera.position.clone();
const defaultControlsTarget = skinViewer.controls.target.clone();

function updateDefaultCameraPosition() {
    defaultCameraPos.copy(skinViewer.camera.position);
    defaultControlsTarget.copy(skinViewer.controls.target);
}

function setAzimuthAndPitch(azimuthDeg, pitchDeg, distance = 60) {
    const controls = skinViewer.controls;
    const target = controls.target;

    const azimuth = azimuthDeg * Math.PI / 180;
    const pitch = pitchDeg * Math.PI / 180;

    const x = distance * Math.cos(pitch) * Math.sin(azimuth);
    const y = distance * Math.sin(pitch);
    const z = distance * Math.cos(pitch) * Math.cos(azimuth);

    skinViewer.camera.position.set(target.x + x, target.y + y, target.z + z);
    controls.update();

    updateDefaultCameraPosition();
}

setAzimuthAndPitch(0, 10);

// 确保 OrbitControls 也有相同的目标点，覆盖默认的 lookAt
if (skinViewer.controls) {
    skinViewer.controls.update();
} else if (skinViewer.camera.lookAt) {
    skinViewer.camera.lookAt(0, 16, 0);
}

let resetAnimationId = null;

// 监听容器的双击事件
container.addEventListener("dblclick", () => {
    // 如果已经在回正动画中，先取消之前的动画
    if (resetAnimationId) {
        cancelAnimationFrame(resetAnimationId);
    }

    // 指数衰减速率（与帧率无关）
    // 物理含义：每秒缩短至剩余距离的 e^(-DECAY)，DECAY=8 时约 0.03%
    const DECAY = 6;

    let lastTime = performance.now();

    const animateReset = (now) => {
        // 计算真实帧间隔（秒），并钳制防止页面切换后跳帧
        const rawDt = (now - lastTime) / 1000;
        const dt = Math.min(rawDt, 0.1);
        lastTime = now;

        // 时间驱动的指数衰减系数，与帧率无关
        const alpha = 1 - Math.exp(-DECAY * dt);

        // Target lerp（直接操作 xyz）
        const t = skinViewer.controls.target;
        const dst = defaultControlsTarget;
        t.x += (dst.x - t.x) * alpha;
        t.y += (dst.y - t.y) * alpha;
        t.z += (dst.z - t.z) * alpha;

        // 当前相机相对 target 的偏移
        const cam = skinViewer.camera.position;
        const ox = cam.x - t.x;
        const oy = cam.y - t.y;
        const oz = cam.z - t.z;

        const dx = defaultCameraPos.x - dst.x;
        const dy = defaultCameraPos.y - dst.y;
        const dz = defaultCameraPos.z - dst.z;

        // 转球坐标
        const curR   = Math.sqrt(ox*ox + oy*oy + oz*oz);
        const defR   = Math.sqrt(dx*dx + dy*dy + dz*dz);
        const curPhi = Math.asin(Math.max(-1, Math.min(1, oy / curR)));
        const defPhi = Math.asin(Math.max(-1, Math.min(1, dy / defR)));
        const curTheta = Math.atan2(oz, ox);
        const defTheta = Math.atan2(dz, dx);

        // 方位角走最短路径
        let dTheta = defTheta - curTheta;
        if (dTheta >  Math.PI) dTheta -= 2 * Math.PI;
        if (dTheta < -Math.PI) dTheta += 2 * Math.PI;

        // 插值
        const nextR     = curR   + (defR   - curR)   * alpha;
        const nextPhi   = curPhi + (defPhi - curPhi)  * alpha;
        const nextTheta = curTheta + dTheta * alpha;

        // 转回笛卡尔坐标
        const cosPhi = Math.cos(nextPhi);
        cam.x = t.x + nextR * cosPhi * Math.cos(nextTheta);
        cam.y = t.y + nextR * Math.sin(nextPhi);
        cam.z = t.z + nextR * cosPhi * Math.sin(nextTheta);

        skinViewer.controls.update();

        // 终止判断
        const dpx = cam.x - defaultCameraPos.x;
        const dpy = cam.y - defaultCameraPos.y;
        const dpz = cam.z - defaultCameraPos.z;
        const distPos = Math.sqrt(dpx*dpx + dpy*dpy + dpz*dpz);

        const ttx = t.x - dst.x;
        const tty = t.y - dst.y;
        const ttz = t.z - dst.z;
        const distTarget = Math.sqrt(ttx*ttx + tty*tty + ttz*ttz);

        if (distPos > 0.05 || distTarget > 0.05) {
            resetAnimationId = requestAnimationFrame(animateReset);
        } else {
            cam.x = defaultCameraPos.x;
            cam.y = defaultCameraPos.y;
            cam.z = defaultCameraPos.z;
            t.x = dst.x; t.y = dst.y; t.z = dst.z;
            skinViewer.controls.update();
            resetAnimationId = null;
        }
    };

    // 启动动画（rAF 传入的时间戳与 performance.now() 同源）
    resetAnimationId = requestAnimationFrame(animateReset);
});

// 如果用户在回正动画播放时主动拖拽了模型，打断回正动画
if (skinViewer.controls) {
    skinViewer.controls.addEventListener("start", () => {
        if (resetAnimationId) {
            cancelAnimationFrame(resetAnimationId);
            resetAnimationId = null;
        }
    });
}

function resize() {
    const w = getWidth();
    const h = getHeight();
    if (w > 0 && h > 0) {
        skinViewer.width = w;
        skinViewer.height = h;
    }
}

window.addEventListener('resize', resize);
setTimeout(resize, 100);
setTimeout(resize, 500);

function loadSkin(skinUrl, model = "auto-detect") {
    skinViewer.loadSkin(skinUrl, { model: model });
}

function loadCape(capeUrl) {
    skinViewer.loadCape(capeUrl);
}

function setInteractionEnabled(enabled) {
    const interactive = enabled !== false;
    skinViewer.controls.enableRotate = interactive;
    container.style.pointerEvents = interactive ? 'auto' : 'none';
}

setInteractionEnabled(true);