// --- Configuration ---
const BASE_URL = 'http://127.0.0.1:5000'; // Localhost backend for web
let currentUser = JSON.parse(localStorage.getItem('riseNowUser')) || null;

// --- State ---
let alarms = [];

// --- API Service ---
const api = {
    async login(email, password) {
        try {
            const response = await fetch(`${BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            if (!response.ok) throw new Error('Login failed');
            return await response.json();
        } catch (err) {
            console.error(err);
            throw err;
        }
    },

    async register(username, email, password) {
        try {
            const response = await fetch(`${BASE_URL}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, email, password })
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Registration failed');
            }
            return await response.json();
        } catch (err) {
            console.error(err);
            throw err;
        }
    },

    async getAlarms(userId) {
        try {
            const response = await fetch(`${BASE_URL}/api/get_alarms/${userId}`);
            if (response.ok) {
                const backendAlarms = await response.json();
                // Map backend fields to frontend fields
                return backendAlarms.map(a => ({
                    id: a.id,
                    time: a.alarm_time,
                    period: a.period,
                    label: a.label,
                    schedule: a.schedule,
                    isActive: a.is_active === 1 || a.is_active === true
                }));
            }
            throw new Error('Failed to fetch alarms');
        } catch (err) {
            console.error('Fetch error:', err);
            const local = localStorage.getItem(`alarms_${userId}`);
            return local ? JSON.parse(local) : [];
        }
    },

    async addAlarm(userId, alarm) {
        try {
            const response = await fetch(`${BASE_URL}/api/add_alarm`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    user_id: userId,
                    alarm_time: alarm.time,
                    period: alarm.period,
                    label: alarm.label,
                    schedule: alarm.schedule,
                    is_active: alarm.isActive
                })
            });
            return response.ok;
        } catch (err) {
            console.error('Failed to sync with backend:', err);
            return false;
        }
    },

    async forgotPassword(email) {
        try {
            const response = await fetch(`${BASE_URL}/forgot-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email })
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to send OTP');
            }
            return await response.json();
        } catch (err) {
            console.error(err);
            throw err;
        }
    },

    async verifyOtp(email, otp) {
        try {
            const response = await fetch(`${BASE_URL}/api/verify-otp`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, otp })
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Invalid OTP');
            }
            return await response.json();
        } catch (err) {
            console.error(err);
            throw err;
        }
    },

    async resetPassword(email, otp, newPassword) {
        try {
            const response = await fetch(`${BASE_URL}/api/reset-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, otp, new_password: newPassword })
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Reset failed');
            }
            return await response.json();
        } catch (err) {
            console.error(err);
            throw err;
        }
    }
};

// --- Navigation ---
const screens = document.querySelectorAll('.screen');
const navItems = document.querySelectorAll('.nav-item');
const bottomNav = document.getElementById('bottom-nav');

function navigateTo(screenId) {
    screens.forEach(s => s.classList.remove('active'));
    const target = document.getElementById(screenId);
    if (target) {
        target.classList.add('active');
    }

    // Toggle bottom nav visibility
    const showNavScreens = ['home-screen', 'alarms-screen', 'profile-screen', 'stats-screen', 'weekly-reflection-screen'];
    if (showNavScreens.includes(screenId)) {
        bottomNav.style.display = 'flex';
    } else {
        bottomNav.style.display = 'none';
        // Add "back" button handlers for specific non-nav screens if needed
    }

    // Update nav icons
    navItems.forEach(item => {
        if (item.dataset.screen === screenId) {
            item.style.color = 'var(--primary-gradient-end)';
        } else {
            item.style.color = 'var(--text-disabled)';
        }
    });

    if (screenId === 'home-screen') renderDashboard();
    if (screenId === 'alarms-screen') renderAlarms();
    if (screenId === 'profile-screen') renderProfile();
}

// --- Renderers ---
function renderAlarms() {
    const list = document.getElementById('alarms-list');
    list.innerHTML = '';

    alarms.forEach(alarm => {
        const card = document.createElement('div');
        card.className = 'glass-card';
        card.style.padding = '24px';
        card.style.display = 'flex';
        card.style.justifyContent = 'space-between';
        card.style.alignItems = 'center';

        card.innerHTML = `
            <div>
                <div style="display: flex; align-items: baseline; gap: 4px;">
                    <span style="font-size: 32px; font-weight: 600; color: ${alarm.isActive ? 'var(--text-primary)' : 'var(--text-secondary)'}">${alarm.time}</span>
                    <span style="font-size: 14px; color: var(--text-disabled)">${alarm.period}</span>
                </div>
                <div style="margin-top: 4px; color: ${alarm.isActive ? 'var(--text-primary)' : 'var(--text-secondary)'}">${alarm.label}</div>
                <div style="font-size: 12px; color: var(--text-disabled)">${alarm.schedule}</div>
            </div>
            <div class="toggle" style="width: 48px; height: 24px; background: ${alarm.isActive ? 'var(--primary-gradient-start)' : 'var(--glass-white)'}; border-radius: 12px; position: relative; cursor: pointer; border: 1px solid var(--glass-border);">
                <div style="width: 18px; height: 18px; background: white; border-radius: 50%; position: absolute; top: 2px; ${alarm.isActive ? 'right: 2px' : 'left: 2px'}; transition: all 0.2s;"></div>
            </div>
        `;

        card.querySelector('.toggle').addEventListener('click', () => {
            alarm.isActive = !alarm.isActive;
            saveLocalAlarms();
            renderAlarms();
        });

        list.appendChild(card);
    });
}

function renderDashboard() {
    if (!currentUser) return;

    // Greeting
    const hour = new Date().getHours();
    let greeting = "Good morning";
    if (hour >= 12 && hour < 17) greeting = "Good afternoon";
    else if (hour >= 17 && hour < 21) greeting = "Good evening";
    else if (hour >= 21 || hour < 5) greeting = "Good night";
    
    document.getElementById('home-greeting').textContent = `${greeting}, ${currentUser.username || 'User'}`;
    document.getElementById('home-identity-statement').textContent = `"${currentUser.identity_statement || 'I am a disciplined leader who takes action.'}"`;
    
    // Stats
    document.getElementById('home-streak-val').textContent = currentUser.streak || '0';
    document.getElementById('home-wake-val').textContent = (currentUser.wake_consistency || '0') + '%';

    // Next Alarm Logic
    const nextAlarm = alarms.filter(a => a.isActive).sort((a, b) => a.time.localeCompare(b.time))[0];
    
    if (nextAlarm) {
        document.getElementById('home-next-alarm-time').textContent = nextAlarm.time;
        document.getElementById('home-next-alarm-period').textContent = nextAlarm.period;
        document.getElementById('home-next-alarm-container').style.opacity = '1';
        document.getElementById('home-next-alarm-day').textContent = 'Today'; // Simplified for now
        document.getElementById('home-next-alarm-countdown').textContent = 'Next scheduled wake up';
    } else {
        document.getElementById('home-next-alarm-time').textContent = '--:--';
        document.getElementById('home-next-alarm-period').textContent = '';
        document.getElementById('home-next-alarm-container').style.opacity = '0.5';
        document.getElementById('home-next-alarm-day').textContent = '';
        document.getElementById('home-next-alarm-countdown').textContent = 'No active alarms';
    }
}

function renderProfile() {
    if (currentUser) {
        document.getElementById('user-name-display').textContent = currentUser.username || currentUser.name;
        document.getElementById('user-identity-display').textContent = currentUser.identity || 'Disciplined User';
    }
}

// --- Data Management ---
function saveLocalAlarms() {
    if (currentUser) {
        localStorage.setItem(`alarms_${currentUser.user_id}`, JSON.stringify(alarms));
    }
}

// --- Event Listeners ---
document.addEventListener('DOMContentLoaded', () => {
    // Initial splash delay
    setTimeout(() => {
        if (currentUser) {
            initApp();
        } else {
            navigateTo('login-screen');
        }
    }, 2000);

    // Login logic
    document.getElementById('login-btn').addEventListener('click', async () => {
        const email = document.getElementById('login-email').value;
        const pass = document.getElementById('login-password').value;

        if (email && pass) {
            try {
                const result = await api.login(email, pass);
                currentUser = result;
                localStorage.setItem('riseNowUser', JSON.stringify(currentUser));
                initApp();
            } catch (err) {
                alert('Login failed: ' + err.message);
            }
        }
    });

    // Register logic
    document.getElementById('register-btn').addEventListener('click', async () => {
        const user = document.getElementById('register-username').value;
        const email = document.getElementById('register-email').value;
        const pass = document.getElementById('register-password').value;

        if (user && email && pass) {
            try {
                await api.register(user, email, pass);
                alert('Account created! Please log in.');
                navigateTo('login-screen');
            } catch (err) {
                alert('Registration failed: ' + err.message);
            }
        }
    });

    // Forgot password flow
    document.getElementById('go-to-forgot-password').addEventListener('click', () => navigateTo('forgot-password-screen'));
    
    document.getElementById('send-otp-btn').addEventListener('click', async () => {
        const email = document.getElementById('forgot-email').value;
        if (email) {
            try {
                await api.forgotPassword(email);
                alert('OTP sent to your email!');
                localStorage.setItem('resetEmail', email);
                navigateTo('verify-otp-screen');
            } catch (err) {
                alert(err.message);
            }
        } else {
            alert('Please enter your email');
        }
    });

    document.getElementById('verify-otp-btn').addEventListener('click', async () => {
        const email = localStorage.getItem('resetEmail');
        const otp = document.getElementById('otp-input').value;
        if (email && otp) {
            try {
                await api.verifyOtp(email, otp);
                localStorage.setItem('resetOtp', otp);
                navigateTo('reset-password-screen');
            } catch (err) {
                alert(err.message);
            }
        } else {
            alert('Please enter the OTP');
        }
    });

    document.getElementById('update-password-btn').addEventListener('click', async () => {
        const email = localStorage.getItem('resetEmail');
        const otp = localStorage.getItem('resetOtp');
        const newPass = document.getElementById('new-password').value;
        const confirmPass = document.getElementById('confirm-new-password').value;

        if (!newPass || !confirmPass) {
            alert('Please fill in both password fields');
            return;
        }

        if (newPass !== confirmPass) {
            alert('Passwords do not match');
            return;
        }

        if (email && otp && newPass) {
            try {
                await api.resetPassword(email, otp, newPass);
                alert('Password reset successful! Please log in.');
                localStorage.removeItem('resetEmail');
                localStorage.removeItem('resetOtp');
                navigateTo('login-screen');
            } catch (err) {
                alert(err.message);
            }
        }
    });

    // Navigation between login/register
    document.getElementById('go-to-register').addEventListener('click', () => navigateTo('register-screen'));
    document.getElementById('go-to-login').addEventListener('click', () => navigateTo('login-screen'));
    document.querySelector('.forgot-back').addEventListener('click', () => navigateTo('login-screen'));
    document.querySelector('.verify-otp-back').addEventListener('click', () => navigateTo('forgot-password-screen'));

    // Logout logic
    document.getElementById('logout-btn').addEventListener('click', () => {
        localStorage.removeItem('riseNowUser');
        currentUser = null;
        navigateTo('login-screen');
    });

    // Nav logic
    navItems.forEach(item => {
        item.addEventListener('click', () => navigateTo(item.dataset.screen));
    });

    // FAB Add logic
    const showAddScreen = () => navigateTo('create-alarm-screen');
    document.getElementById('add-alarm-fab').addEventListener('click', showAddScreen);
    document.getElementById('add-alarm-header-btn').addEventListener('click', showAddScreen);

    // Back logic
    document.querySelector('.nav-back').addEventListener('click', () => navigateTo('alarms-screen'));
    document.querySelector('.reflection-back').addEventListener('click', () => navigateTo('profile-screen'));
    document.getElementById('nav-to-reflection').addEventListener('click', () => navigateTo('weekly-reflection-screen'));
    document.getElementById('home-profile-btn').addEventListener('click', () => navigateTo('profile-screen'));

    // Reflection Dialog Logic
    const reflectionDialog = document.getElementById('reflection-dialog');
    document.getElementById('open-reflection-btn').addEventListener('click', () => {
        reflectionDialog.style.display = 'flex';
    });

    document.getElementById('cancel-reflection-btn').addEventListener('click', () => {
        reflectionDialog.style.display = 'none';
    });

    document.getElementById('save-reflection-btn').addEventListener('click', () => {
        const text = document.getElementById('reflection-input').value;
        if (text) {
            document.getElementById('reflection-text-display').textContent = `"${text}"`;
            // In a real app, we'd sync this with backend
        }
        reflectionDialog.style.display = 'none';
    });

    // Save alarm logic
    document.getElementById('save-alarm-btn').addEventListener('click', async () => {
        const timeVal = document.getElementById('alarm-time-input').value;
        const labelVal = document.getElementById('alarm-label-input').value || 'Alarm';
        const freqVal = document.getElementById('alarm-frequency-input').value;

        if (timeVal) {
            const [h, m] = timeVal.split(':');
            const hour = parseInt(h);
            const period = hour >= 12 ? 'PM' : 'AM';
            const displayHour = hour % 12 || 12;
            const displayTime = `${displayHour.toString().padStart(2, '0')}:${m}`;

            const newAlarm = {
                id: Date.now(),
                time: displayTime,
                period: period,
                label: labelVal,
                schedule: freqVal,
                isActive: true
            };

            alarms.unshift(newAlarm);
            saveLocalAlarms();
            await api.addAlarm(currentUser.user_id, newAlarm);

            navigateTo('alarms-screen');
        }
    });
});

async function initApp() {
    alarms = await api.getAlarms(currentUser.user_id);
    renderDashboard();
    navigateTo('home-screen');
}
