/**
 * 服务器状态 + 在线人数趋势折线图
 */
(function() {
    'use strict';

    var HISTORY_KEY = '4u4n_player_history';

    document.addEventListener('DOMContentLoaded', function() {
        var pc = document.getElementById('player-count');
        var popup = document.getElementById('player-chart-popup');
        var canvas = document.getElementById('player-chart-canvas');
        var note = document.getElementById('player-chart-note');
        if (!pc || !popup || !canvas) return;

        var chartInst = null, hideTimer = null;

        // ====== 历史数据 ======
        function loadHistory() {
            try { var d = JSON.parse(localStorage.getItem(HISTORY_KEY)); if (d && d.length) return d; } catch(e) {}
            return seed();
        }
        function save(d) { try { localStorage.setItem(HISTORY_KEY, JSON.stringify(d)); } catch(e) {} }
        function seed() {
            var d = [], now = Date.now();
            for (var i = 24; i >= 0; i--) {
                var t = new Date(now - i*3600000);
                d.push({ label: t.getHours().toString().padStart(2,'0')+':00', date: (t.getMonth()+1)+'/'+t.getDate(), count: Math.max(0, 5+Math.floor(Math.sin(i/4)*4)+Math.floor(Math.random()*6)), ts: t.getTime() });
            }
            save(d); return d;
        }
        function addPoint(count) {
            var d = loadHistory(), now = new Date();
            var label = now.getHours().toString().padStart(2,'0')+':'+now.getMinutes().toString().padStart(2,'0');
            var date = (now.getMonth()+1)+'/'+now.getDate();
            if (d.length && d[d.length-1].label === label && d[d.length-1].date === date) { d[d.length-1].count = count; }
            else { d.push({ label:label, date:date, count:count, ts:now.getTime() }); }
            var cut = now.getTime() - 3*86400000;
            d = d.filter(function(p){ return p.ts >= cut; });
            if (d.length > 200) d = d.slice(-200);
            save(d);
        }

        // ====== 折线图 ======
        function renderChart() {
            if (chartInst) { chartInst.destroy(); chartInst = null; }
            var d = loadHistory();
            note.textContent = d.length + ' 个采样点 · 0-100 人区间';
            var ctx = canvas.getContext('2d');
            var g = ctx.createLinearGradient(0,0,0,180);
            g.addColorStop(0,'rgba(6,182,212,0.35)'); g.addColorStop(1,'rgba(6,182,212,0.02)');
            chartInst = new Chart(ctx, {
                type:'line',
                data:{ labels: d.map(function(p){return p.date+' '+p.label;}), datasets:[{ data: d.map(function(p){return p.count;}), borderColor:'#06b6d4', backgroundColor:g, borderWidth:2, pointRadius:0, pointHoverRadius:5, pointHoverBackgroundColor:'#fff', tension:0.3, fill:true }] },
                options:{
                    responsive:true, maintainAspectRatio:false, animation:false,
                    plugins:{ legend:{display:false}, tooltip:{ backgroundColor:'rgba(15,23,42,0.95)', titleColor:'#e2e8f0', bodyColor:'#22d3ee', borderColor:'#334155', borderWidth:1, cornerRadius:8, displayColors:false, callbacks:{ label:function(c){return c.parsed.y+' 人在线';} } } },
                    scales:{ x:{ ticks:{color:'#64748b',font:{size:9},maxTicksLimit:5,maxRotation:0}, grid:{color:'rgba(75,85,99,0.3)'} }, y:{ min:0, max:100, ticks:{color:'#64748b',font:{size:9},stepSize:20}, grid:{color:'rgba(75,85,99,0.3)'} } },
                    interaction:{mode:'index'}
                }
            });
        }

        // ====== 定位弹框 ======
        function positionPopup() {
            var r = pc.getBoundingClientRect();
            var pw = 320, ph = 280;
            var left = r.left + r.width/2 - pw/2;
            var top = r.top - ph - 10;
            if (left < 8) left = 8;
            if (left + pw > window.innerWidth - 8) left = window.innerWidth - pw - 8;
            if (top < 8) top = r.bottom + 10;
            popup.style.left = left + 'px';
            popup.style.top = top + 'px';
        }

        function showPopup() {
            if (hideTimer) clearTimeout(hideTimer);
            positionPopup();
            popup.style.display = 'block';
            if (!chartInst) renderChart();
        }
        function hidePopup() {
            hideTimer = setTimeout(function(){ popup.style.display = 'none'; }, 300);
        }

        pc.addEventListener('mouseenter', showPopup);
        pc.addEventListener('mouseleave', hidePopup);
        popup.addEventListener('mouseenter', function(){ clearTimeout(hideTimer); });
        popup.addEventListener('mouseleave', function(){ popup.style.display = 'none'; });
        window.addEventListener('resize', function(){ if (popup.style.display === 'block') positionPopup(); });
        window.addEventListener('scroll', function(){ if (popup.style.display === 'block') positionPopup(); });

        // ====== 服务器状态 ======
        async function fetchStatus() {
            try {
                var r = await fetch('https://api.mcsrvstat.us/2/4u4n.qiunaruto.top', { cache:'no-store' });
                var d = await r.json();
                var cnt = d.online ? (d.players?.online || 0) : 0;
                pc.childNodes[0].textContent = cnt;
                document.getElementById('max-players').textContent = d.players?.max || '--';
                var st = document.getElementById('server-status');
                st.textContent = d.online ? window.t('hero_status_online') : window.t('hero_status_offline');
                st.className = 'ml-2 px-2 py-0.5 text-xs rounded-full ' + (d.online ? 'bg-green-500' : 'bg-red-500');
                document.getElementById('last-update').textContent = window.t('hero_last_update',{time:new Date().toLocaleTimeString()});
                addPoint(cnt);
                if (popup.style.display === 'block') { setTimeout(renderChart, 100); positionPopup(); }
            } catch(e) { console.error(e); }
        }
        fetchStatus();
        setInterval(fetchStatus, 15000);
    });
})();
