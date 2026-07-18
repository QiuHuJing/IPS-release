/**
 * 设置面板 v2 — 修复导航/关闭提示/开发者密钥
 */
(function() {
    'use strict';

    var DEV_KEY_HASH = 'NHU0bmZ1bjEyMzQ1Ng=='; // base64("4u4nfun123456")
    var modal, hasChanges = false, isDevMode = false;
    var SETTINGS_KEY = '4u4n_settings';

    // 与 HTML 中 data-action / href 的映射
    var NAV_MAP = {
        features:   'a[href="#features"]',
        ip:         'a[href="#ip"]',
        seed:       'a[href="#seed"]',
        advantages: 'a[href="#advantages"]',
        rules:      'a[href="#rules"]',
        team:       'a[href="#team"]',
        faq:        'a[href="#faq"]',
        social:     'a[href="#social"]',
        help:       'a[data-action="help"]',
        announce:   'a[data-action="announcement"]'
    };

    var DEFAULT_NAV = [
        { id:'features',   icon:'fa-star',            label:'nav_features' },
        { id:'ip',         icon:'fa-network-wired',   label:'nav_ip' },
        { id:'seed',       icon:'fa-seedling',         label:'nav_seed' },
        { id:'advantages', icon:'fa-trophy',           label:'nav_advantages' },
        { id:'rules',      icon:'fa-gavel',            label:'nav_rules' },
        { id:'team',       icon:'fa-users',            label:'nav_team' },
        { id:'faq',        icon:'fa-question-circle',  label:'nav_faq' },
        { id:'social',     icon:'fa-share-alt',        label:'nav_social' },
        { id:'help',       icon:'fa-life-ring',        label:'nav_help' },
        { id:'announce',   icon:'fa-bullhorn',         label:'nav_announce' }
    ];

    function loadSettings() {
        try { var s = JSON.parse(localStorage.getItem(SETTINGS_KEY)); if (s && s._v === 2) return s; } catch(e) {}
        return {
            _v: 2,
            navItems: DEFAULT_NAV.map(function(n) { return { id:n.id, visible:true }; }),
            navOrder: DEFAULT_NAV.map(function(n) { return n.id; }),
            hideMusic: false,
            playlistId: '2144281377',
            cacheEnabled: true
        };
    }
    var settings = loadSettings();

    function init() {
        modal = document.getElementById('settings-modal');
        if (!modal) return;

        document.getElementById('settings-toggle-btn').addEventListener('click', openSettings);
        document.getElementById('settings-close').addEventListener('click', tryClose);
        document.getElementById('settings-cancel').addEventListener('click', tryClose);
        document.getElementById('settings-confirm').addEventListener('click', saveAndClose);

        // 点击遮罩层关闭
        var overlayBg = document.getElementById('settings-overlay-bg');
        if (overlayBg) overlayBg.addEventListener('click', tryClose);

        renderNavList();
        document.getElementById('settings-hide-music').checked = settings.hideMusic;
        document.getElementById('settings-playlist-id').value = settings.playlistId;
        document.getElementById('settings-cache').checked = settings.cacheEnabled;
        document.getElementById('settings-lang').value = localStorage.getItem('4u4n_lang') || 'zh';

        updateThemeBtns(localStorage.getItem('4u4n_theme') || 'light');
        document.querySelectorAll('.settings-theme-btn').forEach(function(b) {
            b.addEventListener('click', function() { updateThemeBtns(this.dataset.theme); markChanged(); });
        });

        modal.querySelectorAll('input, select').forEach(function(el) {
            el.addEventListener('change', markChanged);
            el.addEventListener('input', markChanged);
        });

        document.getElementById('settings-dev-key').addEventListener('input', checkDevKey);
        document.getElementById('settings-dev-btn').addEventListener('click', unlockDev);

        isDevMode = localStorage.getItem('4u4n_devmode') === '1';
        updateDevUI();
        // 如果开发者模式已开启，立即移除登录遮罩
        if (isDevMode) {
            var ov = document.getElementById('auth-dev-overlay');
            if (ov) ov.remove();
        }

        // 初始应用导航设置
        applyNavSettings();
        // 初始应用音乐按钮
        if (settings.hideMusic) {
            var mb = document.getElementById('music-toggle-btn');
            if (mb) mb.style.display = 'none';
        }
    }

    function renderNavList() {
        var container = document.getElementById('settings-nav-list');
        if (!container) return;
        container.innerHTML = '';
        var order = settings.navOrder || DEFAULT_NAV.map(function(n){return n.id;});
        var items = settings.navItems || [];

        order.forEach(function(id) {
            var item = items.find(function(n) { return n.id === id; });
            if (!item) return;
            var def = DEFAULT_NAV.find(function(n) { return n.id === id; });
            var label = def ? (window.t(def.label) || id) : id;
            var icon = def ? def.icon : 'fa-link';

            var div = document.createElement('div');
            div.className = 'settings-nav-item';
            div.dataset.id = id;
            div.innerHTML =
                '<span class="nav-drag">⠿</span>' +
                '<i class="fas ' + icon + ' text-xs text-gray-400" style="width:16px;text-align:center"></i>' +
                '<span class="nav-label">' + label + '</span>' +
                '<div class="nav-btns">' +
                    '<button class="nav-up">▲</button>' +
                    '<button class="nav-down">▼</button>' +
                '</div>' +
                '<label style="cursor:pointer;display:inline-flex;align-items:center">' +
                    '<input type="checkbox" class="sr-only nav-check" style="position:absolute;opacity:0;width:0;height:0"' + (item.visible ? ' checked' : '') + '>' +
                    '<div style="width:34px;height:20px;background:' + (item.visible ? '#06b6d4' : '#d1d5db') + ';border-radius:999px;position:relative;transition:background 0.2s">' +
                        '<div style="position:absolute;top:2px;left:2px;width:16px;height:16px;background:#fff;border-radius:50%;transition:transform 0.2s;transform:translateX(' + (item.visible ? '14px' : '0') + ')"></div>' +
                    '</div>' +
                '</label>';
            container.appendChild(div);

            div.querySelector('.nav-up').addEventListener('click', function(e) {
                e.stopPropagation();
                var all = container.querySelectorAll('.settings-nav-item');
                var idx = Array.from(all).indexOf(div);
                if (idx > 0) { container.insertBefore(div, all[idx-1]); markChanged(); }
            });
            div.querySelector('.nav-down').addEventListener('click', function(e) {
                e.stopPropagation();
                var all = container.querySelectorAll('.settings-nav-item');
                var idx = Array.from(all).indexOf(div);
                if (idx < all.length-1) { container.insertBefore(all[idx+1], div); markChanged(); }
            });
            div.querySelector('.nav-check').addEventListener('change', function() {
                var toggle = div.querySelector('.nav-check + div');
                if (toggle) {
                    toggle.style.background = this.checked ? '#06b6d4' : '#d1d5db';
                    toggle.firstElementChild.style.transform = this.checked ? 'translateX(14px)' : 'translateX(0)';
                }
                markChanged();
            });
        });
    }

    function updateThemeBtns(theme) {
        document.querySelectorAll('.settings-theme-btn').forEach(function(b) {
            b.classList.toggle('active', b.dataset.theme === theme);
        });
    }

    function markChanged() { hasChanges = true; }

    function openSettings() {
        hasChanges = false;
        settings = loadSettings();
        document.getElementById('settings-hide-music').checked = settings.hideMusic;
        document.getElementById('settings-playlist-id').value = settings.playlistId;
        document.getElementById('settings-cache').checked = settings.cacheEnabled;
        document.getElementById('settings-lang').value = localStorage.getItem('4u4n_lang') || 'zh';
        updateThemeBtns(localStorage.getItem('4u4n_theme') || 'light');
        renderNavList();
        updateDevUI();
        modal.classList.add('show');
        hasChanges = false;
    }

    function tryClose() {
        if (hasChanges) {
            if (confirm(window.t('settings_unsaved') || '有未保存的更改，是否保存？')) {
                saveAndClose();
            } else {
                closeSettings();
            }
        } else {
            closeSettings();
        }
    }

    function closeSettings() {
        modal.classList.remove('show');
        hasChanges = false;
    }

    function saveAndClose() {
        collectSettings();
        if (settings.cacheEnabled) {
            try { localStorage.setItem(SETTINGS_KEY, JSON.stringify(settings)); } catch(e) {}
        }
        applyNavSettings();
        // 音乐按钮
        var mb = document.getElementById('music-toggle-btn');
        if (mb) mb.style.display = settings.hideMusic ? 'none' : '';
        // 云歌单 ID
        var iframe = document.querySelector('#music-cloud-box iframe');
        if (iframe) iframe.src = 'https://music.163.com/outchain/player?type=0&id=' + settings.playlistId + '&auto=0&height=430';
        closeSettings();
    }

    function collectSettings() {
        settings.hideMusic = document.getElementById('settings-hide-music').checked;
        settings.playlistId = document.getElementById('settings-playlist-id').value.trim() || '2144281377';
        settings.cacheEnabled = document.getElementById('settings-cache').checked;

        var container = document.getElementById('settings-nav-list');
        var items = container.querySelectorAll('.settings-nav-item');
        var newOrder = [], newItems = [];
        items.forEach(function(item) {
            var id = item.dataset.id;
            newOrder.push(id);
            newItems.push({ id:id, visible: item.querySelector('.nav-check').checked });
        });
        settings.navOrder = newOrder;
        settings.navItems = newItems;

        // 语言
        var lang = document.getElementById('settings-lang').value;
        if (typeof window.switchLangTo === 'function') window.switchLangTo(lang);

        // 主题
        var themeBtn = modal.querySelector('.settings-theme-btn.active');
        if (themeBtn) {
            var theme = themeBtn.dataset.theme;
            if (theme === 'dark') {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('4u4n_theme', 'dark');
            } else {
                document.documentElement.removeAttribute('data-theme');
                localStorage.setItem('4u4n_theme', 'light');
            }
            if (typeof window.updateThemeIcon === 'function') window.updateThemeIcon();
        }
    }

    function applyNavSettings() {
        var order = settings.navOrder || [];
        var items = settings.navItems || [];

        // 找到所有 nav-link 并按父容器分组
        var groups = {};
        document.querySelectorAll('.nav-link, a[data-action]').forEach(function(a) {
            // 只处理在 glass-nav 内的
            if (!a.closest('.glass-nav')) return;
            var parent = a.parentElement;
            if (!groups[parent]) groups[parent] = { el: parent, links: [] };
            // 避免重复（同一元素可能匹配两个选择器）
            if (groups[parent].links.indexOf(a) === -1) groups[parent].links.push(a);
        });

        Object.keys(groups).forEach(function(key) {
            var group = groups[key];
            var parentEl = group.el;
            var links = group.links;
            if (links.length === 0) return;

            // 插入标记
            var marker = document.createElement('span');
            marker.style.display = 'none';
            parentEl.insertBefore(marker, links[0]);

            // id→element 映射
            var linkMap = {};
            links.forEach(function(a) {
                for (var id in NAV_MAP) {
                    if (a.matches(NAV_MAP[id])) { linkMap[id] = a; break; }
                }
            });

            // 移除所有
            Object.keys(linkMap).forEach(function(id) {
                var el = linkMap[id];
                if (el.parentElement) el.parentElement.removeChild(el);
            });

            // 按 order 倒序插入（保证正序）
            for (var i = order.length - 1; i >= 0; i--) {
                var id = order[i];
                var item = items.find(function(n) { return n.id === id; });
                var link = linkMap[id];
                if (link && item) {
                    link.style.display = item.visible ? '' : 'none';
                    parentEl.insertBefore(link, marker);
                }
            }

            marker.remove();
        });
    }

    // ====== 开发者 ======
    function checkDevKey() {
        var input = document.getElementById('settings-dev-key');
        var btn = document.getElementById('settings-dev-btn');
        if (input.value.length >= 6) {
            btn.disabled = false;
            btn.className = 'px-3 py-1.5 bg-cyan-500 text-white rounded-lg text-sm font-medium cursor-pointer hover:bg-cyan-600 transition-all';
        } else {
            btn.disabled = true;
            btn.className = 'px-3 py-1.5 bg-gray-200 text-gray-500 rounded-lg text-sm font-medium cursor-not-allowed transition-all';
        }
    }

    function unlockDev() {
        var input = document.getElementById('settings-dev-key').value;
        var msg = document.getElementById('settings-dev-msg');
        var encoded = btoa(input);
        if (encoded === DEV_KEY_HASH) {
            isDevMode = true;
            localStorage.setItem('4u4n_devmode', '1');
            msg.textContent = '✅ ' + (window.t('settings_dev_ok') || '开发者模式已开启');
            msg.className = 'text-xs mt-1 success';
            msg.style.display = '';
            updateDevUI();
            // 移除登录遮罩
            var overlay = document.getElementById('auth-dev-overlay');
            if (overlay) { overlay.remove(); }
            // 如果 auth.js 有 enableAuth 就调用
            if (typeof window.enableAuth === 'function') window.enableAuth();
        } else {
            msg.textContent = '❌ ' + (window.t('settings_dev_fail') || '密钥错误');
            msg.className = 'text-xs mt-1 error';
            msg.style.display = '';
        }
        setTimeout(function() { msg.style.display = 'none'; }, 2500);
    }

    function updateDevUI() {
        var btn = document.getElementById('settings-dev-btn');
        var input = document.getElementById('settings-dev-key');
        if (isDevMode) {
            if (btn) { btn.textContent = '✓'; btn.className = 'px-3 py-1.5 bg-green-500 text-white rounded-lg text-sm font-medium cursor-default transition-all'; btn.disabled = true; }
            if (input) { input.value = ''; input.placeholder = '已解锁'; input.disabled = true; }
        }
    }

    window.setCloudPlaylistId = function(id) {
        var iframe = document.querySelector('#music-cloud-box iframe');
        if (iframe) iframe.src = 'https://music.163.com/outchain/player?type=0&id=' + id + '&auto=0&height=430';
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
