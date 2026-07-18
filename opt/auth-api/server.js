const express = require('express');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const cors = require('cors');

const app = express();

// ====================================================
// 配置区域（根据你的实际情况修改）
// ====================================================

// 数据库配置（同 AuthMe 的数据库）
const DB_CONFIG = {
  host: '127.0.0.1',
  port: 3306,
  user: 'root',
  password: '你的数据库密码',
  database: 'minecraft'       // AuthMe 所在的数据库
};

// JWT 密钥（随便写个复杂字符串）
const JWT_SECRET = '4u4n_2025_auth_secret_xxxxxx';

// 允许跨域的域名列表（你的 GitHub Pages 域名）
const ALLOWED_ORIGINS = [
  'https://你的用户名.github.io',   // 替换为你的 GitHub Pages 地址
  'http://localhost:5500',           // 本地测试
  'http://127.0.0.1:5500'
];

// API 启动端口
const PORT = 3001;

// ====================================================
// 中间件
// ====================================================

app.use(cors({
  origin: function(origin, callback) {
    // 允许无来源的请求（比如直接 curl 测试）
    if (!origin) return callback(null, true);
    if (ALLOWED_ORIGINS.indexOf(origin) !== -1) {
      callback(null, true);
    } else {
      callback(new Error('不允许的来源: ' + origin));
    }
  },
  credentials: true
}));

app.use(express.json());

// ====================================================
// 数据库连接池
// ====================================================

const pool = mysql.createPool({
  host: DB_CONFIG.host,
  port: DB_CONFIG.port,
  user: DB_CONFIG.user,
  password: DB_CONFIG.password,
  database: DB_CONFIG.database,
  waitForConnections: true,
  connectionLimit: 5,
  charset: 'utf8mb4'
});

// ====================================================
// API 接口
// ====================================================

// 1️⃣ 健康检查（用于测试连通性）
app.get('/api/auth/health', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT 1 AS ok');
    res.json({
      status: 'ok',
      db: 'connected',
      server_time: new Date().toISOString(),
      authme_table: await checkAuthMeTable()
    });
  } catch (err) {
    res.status(500).json({
      status: 'error',
      message: '数据库连接失败',
      error: err.message
    });
  }
});

async function checkAuthMeTable() {
  try {
    const [tables] = await pool.query("SHOW TABLES LIKE 'authme'");
    return tables.length > 0 ? '存在' : '不存在（请检查数据库名和表名）';
  } catch {
    return '查询失败';
  }
}

// 2️⃣ 登录接口
app.post('/api/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    // 校验输入
    if (!username || !password) {
      return res.status(400).json({
        success: false,
        error: '请输入用户名和密码'
      });
    }

    const cleanUsername = username.trim();

    // 查询 AuthMe 表（先试 realname，再试 username）
    const [users] = await pool.query(
      'SELECT id, username, realname, password FROM authme WHERE realname = ? OR username = ? LIMIT 1',
      [cleanUsername, cleanUsername.toLowerCase()]
    );

    if (users.length === 0) {
      return res.status(401).json({
        success: false,
        error: '❌ 该玩家在服务器中不存在，请先在游戏内注册'
      });
    }

    const user = users[0];

    // 验证密码（AuthMe 使用 bcrypt 加密）
    const isValid = bcrypt.compareSync(password, user.password);
    if (!isValid) {
      return res.status(401).json({
        success: false,
        error: '❌ 密码错误，请检查后重试'
      });
    }

    // 生成 JWT Token（7天有效）
    const token = jwt.sign(
      {
        id: user.id,
        username: user.realname || user.username,
        login_at: Date.now()
      },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    // 登录成功
    console.log(`✅ ${user.realname || user.username} 登录成功`);
    res.json({
      success: true,
      message: '登录成功',
      data: {
        username: user.realname || user.username
      },
      token
    });

  } catch (err) {
    console.error('❌ 登录异常:', err);
    res.status(500).json({
      success: false,
      error: '服务器内部错误，请稍后重试'
    });
  }
});

// 3️⃣ 验证 Token 获取用户信息
app.get('/api/auth/profile', async (req, res) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ success: false, error: '未登录' });
    }

    const token = authHeader.split(' ')[1];
    const decoded = jwt.verify(token, JWT_SECRET);

    res.json({
      success: true,
      data: {
        id: decoded.id,
        username: decoded.username
      }
    });

  } catch (err) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({ success: false, error: '登录已过期，请重新登录' });
    }
    return res.status(401).json({ success: false, error: 'Token 无效' });
  }
});

// ====================================================
// 启动服务器
// ====================================================

app.listen(PORT, '0.0.0.0', () => {
  console.log('========================================');
  console.log('  ✅ 4U4N Auth API 已启动');
  console.log('  📡 端口: ' + PORT);
  console.log('  🗄️  数据库: ' + DB_CONFIG.host + '/' + DB_CONFIG.database);
  console.log('  🔗 允许的来源:');
  ALLOWED_ORIGINS.forEach(o => console.log('     - ' + o));
  console.log('========================================');
});
