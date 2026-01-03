// Глобальные переменные
let isAuthenticated = false;
let currentUserRole = '';

// DOM элементы
const modal = document.getElementById('auth-modal');
const loginForm = document.getElementById('login-form');
const userInfoSpan = document.getElementById('user-info');
const logoutBtn = document.getElementById('logout-btn');
const changePasswordBtn = document.getElementById('change-password-btn');
const userInfoBtn = document.getElementById('user-info-btn');
const menuButtons = document.querySelectorAll('.menu-btn');
const resultContainer = document.getElementById('result-container');
// Edit sensor modal elements
const editSensorModal = document.getElementById('edit-sensor-modal');
const editSensorForm = document.getElementById('edit-sensor-form');
const editSensorType = document.getElementById('edit-sensor-type');
const editSensorValue = document.getElementById('edit-sensor-value');
const editSensorTimestamp = document.getElementById('edit-sensor-timestamp');
const editSensorBus = document.getElementById('edit-sensor-bus');
const closeEditSpan = document.querySelector('.close-edit');
let editingSensorId = null;

// Открытие модального окна при необходимости
function showAuthModal() {
    modal.style.display = 'flex';
}

// Закрытие модального окна
function closeAuthModal() {
    modal.style.display = 'none';
}

// Проверка аутентификации
async function checkAuthStatus() {
    try {
        const response = await fetch('/api/auth/info', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const userData = await response.json();
            isAuthenticated = true;
            currentUserRole = userData.role;
            updateUIForAuthenticatedUser(userData);
            return true;
        } else {
            isAuthenticated = false;
            currentUserRole = '';
            return false;
        }
    } catch (error) {
        console.error('Ошибка проверки аутентификации:', error);
        isAuthenticated = false;
        currentUserRole = '';
        return false;
    }
}

// Обновление UI для аутентифицированного пользователя
function updateUIForAuthenticatedUser(userData) {
    userInfoSpan.textContent = `Пользователь: ${userData.username} (${userData.role})`;

    // Показываем/скрываем кнопки в зависимости от роли
    const createBusBtn = document.getElementById('create-bus-btn');
    const createSensorBtn = document.getElementById('create-sensor-btn');
    const importCsvBtn = document.getElementById('import-csv-btn');

    if (userData.role === 'ADMIN') {
        if (createBusBtn) createBusBtn.style.display = 'block';
        if (createSensorBtn) createSensorBtn.style.display = 'block';
        if (importCsvBtn) importCsvBtn.style.display = 'block';
    } else {
        if (createBusBtn) createBusBtn.style.display = 'none';
        if (createSensorBtn) createSensorBtn.style.display = 'none';
        if (importCsvBtn) importCsvBtn.style.display = 'none';
    }
}

// Обработка формы логина
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password }),
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            isAuthenticated = data.isLogged;

            // Получаем информацию о пользователе
            const userInfoResponse = await fetch('/api/auth/info', {
                method: 'GET',
                credentials: 'include'
            });

            if (userInfoResponse.ok) {
                const userData = await userInfoResponse.json();
                currentUserRole = userData.role;
                updateUIForAuthenticatedUser(userData);
                closeAuthModal();

                // Показываем сообщение об успешном входе
                showMessage('Успешный вход', 'success');
            }
        } else {
            showMessage('Неверные учетные данные', 'error');
        }
    } catch (error) {
        console.error('Ошибка входа:', error);
        showMessage('Ошибка при входе', 'error');
    }
});

// Обработка выхода
logoutBtn.addEventListener('click', async () => {
    try {
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });

        if (response.ok) {
            isAuthenticated = false;
            currentUserRole = '';
            userInfoSpan.textContent = '';
            userInfoSpan.style.display = 'none';
            showMessage('Вы успешно вышли', 'success');
        }
    } catch (error) {
        console.error('Ошибка выхода:', error);
        showMessage('Ошибка при выходе', 'error');
    }
});

// Обработка получения информации о пользователе
userInfoBtn.addEventListener('click', async () => {
    if (!isAuthenticated) {
        showAuthModal();
        return;
    }

    try {
        const response = await fetch('/api/auth/info', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const userData = await response.json();
            const userInfoHTML = `
                <h3>Информация о пользователе</h3>
                <p><strong>Имя пользователя:</strong> ${userData.username}</p>
                <p><strong>Роль:</strong> ${userData.role}</p>
                <p><strong>Разрешения:</strong> ${userData.permissions.join(', ')}</p>
            `;
            resultContainer.innerHTML = userInfoHTML;
        } else {
            showMessage('Ошибка получения информации о пользователе', 'error');
        }
    } catch (error) {
        console.error('Ошибка получения информации о пользователе:', error);
        showMessage('Ошибка получения информации о пользователе', 'error');
    }
});

// Обработка смены пароля
changePasswordBtn.addEventListener('click', () => {
    if (!isAuthenticated) {
        showAuthModal();
        return;
    }

    const newPassword = prompt('Введите новый пароль:');
    if (!newPassword) return;

    const confirmPassword = prompt('Подтвердите новый пароль:');
    if (newPassword !== confirmPassword) {
        showMessage('Пароли не совпадают', 'error');
        return;
    }

    const currentPassword = prompt('Введите текущий пароль:');
    if (!currentPassword) return;

    fetch('/api/auth/change_password', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            currentPassword,
            newPassword,
            confirmPassword
        }),
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            showMessage('Пароль успешно изменен', 'success');
        } else {
            showMessage('Ошибка изменения пароля', 'error');
        }
    })
    .catch(error => {
        console.error('Ошибка изменения пароля:', error);
        showMessage('Ошибка изменения пароля', 'error');
    });
});

// Обработчики для кнопок меню
menuButtons.forEach(button => {
    button.addEventListener('click', async (e) => {
        const action = e.target.getAttribute('data-action');

        // Проверяем аутентификацию
        const authStatus = await checkAuthStatus();
        if (!authStatus) {
            showAuthModal();
            return;
        }

        // Проверяем права доступа
        if (!hasPermission(action)) {
            showMessage('Недостаточно прав для выполнения действия', 'error');
            return;
        }

        // Выполняем действие
        performAction(action);
    });
});

// Проверка прав доступа
function hasPermission(action) {
    // Для администратора разрешены все действия
    if (currentUserRole === 'ADMIN') {
        return true;
    }

    // Для обычного пользователя только чтение и экспорт
    const readActions = [
        'get-all-buses',
        'get-all-sensors',
        'get-alerts',
        'export-csv'
    ];

    return readActions.includes(action);
}

// Выполнение действия
async function performAction(action) {
    switch (action) {
        case 'get-all-buses':
            getAllBuses();
            break;
        case 'create-bus':
            createBus();
            break;
        case 'get-all-sensors':
            getAllSensors();
            break;
        case 'create-sensor':
            createSensor();
            break;
        case 'get-alerts':
            getAlerts();
            break;
        case 'export-csv':
            exportCsv();
            break;
        case 'import-csv':
            importCsv();
            break;
        default:
            showMessage('Неизвестное действие', 'error');
    }
}

// Функции для выполнения действий

async function getAllBuses() {
    try {
        const response = await fetch('/api/buses', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const buses = await response.json();
            displayBuses(buses);
        } else {
            showMessage('Ошибка получения автобусов', 'error');
        }
    } catch (error) {
        console.error('Ошибка получения автобусов:', error);
        showMessage('Ошибка получения автобусов', 'error');
    }
}

function displayBuses(buses) {
    let html = '<h3>Список автобусов</h3>';
    html += '<table><thead><tr><th>ID</th><th>Модель</th><th>Действия</th></tr></thead><tbody>';

    buses.forEach(bus => {
        html += `<tr>
            <td>${bus.id}</td>
            <td>${bus.model}</td>
            <td class="actions-cell">
                <button class="btn-edit" onclick="editBus(${bus.id}, '${bus.model}')" title="Изменить">Изменить</button>
                <button class="btn-delete" onclick="deleteBus(${bus.id})" title="Удалить">Удалить</button>
            </td>
        </tr>`;
    });

    html += '</tbody></table>';
    resultContainer.innerHTML = html;
}

async function createBus() {
    const model = prompt('Введите модель автобуса:');
    if (!model) return;

    try {
        const response = await fetch('/api/buses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ model }),
            credentials: 'include'
        });

        if (response.ok) {
            const newBus = await response.json();
            showMessage(`Автобус создан: ${newBus.model}`, 'success');
            getAllBuses(); // Обновляем список
        } else {
            showMessage('Ошибка создания автобуса', 'error');
        }
    } catch (error) {
        console.error('Ошибка создания автобуса:', error);
        showMessage('Ошибка создания автобуса', 'error');
    }
}

// Функция для редактирования автобуса
async function editBus(id, currentModel) {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может редактировать автобусы', 'error');
        return;
    }

    const newModel = prompt('Введите новую модель автобуса:', currentModel);
    if (!newModel) return;

    try {
        const response = await fetch(`/api/buses/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ model: newModel }),
            credentials: 'include'
        });

        if (response.ok) {
            showMessage('Автобус обновлен', 'success');
            getAllBuses(); // Обновляем список
        } else {
            showMessage('Ошибка обновления автобуса', 'error');
        }
    } catch (error) {
        console.error('Ошибка обновления автобуса:', error);
        showMessage('Ошибка обновления автобуса', 'error');
    }
}

// Функция для удаления автобуса
async function deleteBus(id) {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может удалять автобусы', 'error');
        return;
    }

    if (!confirm('Вы уверены, что хотите удалить этот автобус?')) {
        return;
    }

    try {
        const response = await fetch(`/api/buses/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            showMessage('Автобус удален', 'success');
            getAllBuses(); // Обновляем список
        } else {
            showMessage('Ошибка удаления автобуса', 'error');
        }
    } catch (error) {
        console.error('Ошибка удаления автобуса:', error);
        showMessage('Ошибка удаления автобуса', 'error');
    }
}

async function getAllSensors() {
    try {
        const response = await fetch('/api/sensors', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const sensors = await response.json();
            displaySensors(sensors);
        } else {
            showMessage('Ошибка получения датчиков', 'error');
        }
    } catch (error) {
        console.error('Ошибка получения датчиков:', error);
        showMessage('Ошибка получения датчиков', 'error');
    }
}

function displaySensors(sensors) {
    let html = '<h3>Данные датчиков</h3>';
    html += '<table><thead><tr><th>ID</th><th>Bus ID</th><th>Тип датчика</th><th>Значение</th><th>Время</th><th>Аномалия</th><th>Действия</th></tr></thead><tbody>';

    sensors.forEach(sensor => {
        // Check if bus object exists
        const busId = sensor.bus && sensor.bus.id ? sensor.bus.id : sensor.busId || '';
        html += `<tr id="sensor-row-${sensor.id}">
            <td>${sensor.id}</td>
            <td>${busId}</td>
            <td>${sensor.sensorType}</td>
            <td>${sensor.value}</td>
            <td>${sensor.timestamp}</td>
            <td>${sensor.anomaly ? 'Да' : 'Нет'}</td>
            <td class="actions-cell">
                <button class="btn-edit" onclick="openEditSensorModal(${sensor.id})" title="Изменить">Изменить</button>
                <button class="btn-delete" onclick="deleteSensor(${sensor.id})" title="Удалить">Удалить</button>
            </td>
        </tr>`;
    });

    html += '</tbody></table>';
    resultContainer.innerHTML = html;
}

// Open edit modal and populate fields from server
async function openEditSensorModal(id) {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может редактировать датчики', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/sensors/${id}`, {
            method: 'GET',
            credentials: 'include'
        });

        if (!response.ok) {
            showMessage('Ошибка получения данных датчика', 'error');
            return;
        }

        const sensor = await response.json();
        editingSensorId = id;

        // Populate form fields
        editSensorType.value = sensor.sensorType || 'ENGINE_TEMP';
        editSensorValue.value = sensor.value != null ? sensor.value : '';
        // Attempt to populate bus id
        const busId = sensor.bus && sensor.bus.id ? sensor.bus.id : sensor.busId || '';
        editSensorBus.value = busId;

        // Convert timestamp to input[type=datetime-local] value if present
        if (sensor.timestamp) {
            const dt = new Date(sensor.timestamp);
            // format to yyyy-MM-ddTHH:mm
            const tzOffset = dt.getTimezoneOffset() * 60000;
            const localISO = new Date(dt - tzOffset).toISOString().slice(0,16);
            editSensorTimestamp.value = localISO;
        } else {
            editSensorTimestamp.value = '';
        }

        // Show modal (use flex to center)
        editSensorModal.style.display = 'flex';
    } catch (error) {
        console.error('Ошибка при открытии модального окна редактирования:', error);
        showMessage('Ошибка при получении данных датчика', 'error');
    }
}

// Close edit modal
function closeEditSensorModal() {
    editingSensorId = null;
    editSensorForm.reset();
    editSensorModal.style.display = 'none';
}

// Handle modal close events
closeEditSpan.addEventListener('click', closeEditSensorModal);
document.getElementById('cancel-edit-sensor').addEventListener('click', closeEditSensorModal);
window.addEventListener('click', (e) => {
    if (e.target === editSensorModal) {
        closeEditSensorModal();
    }
});

// Save edited sensor from modal
editSensorForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!editingSensorId) return;

    const newType = editSensorType.value;
    const newValue = parseFloat(editSensorValue.value);
    const newBusId = parseInt(editSensorBus.value);

    if (isNaN(newValue)) {
        showMessage('Неверное значение датчика', 'error');
        return;
    }
    if (isNaN(newBusId)) {
        showMessage('Неверный ID автобуса', 'error');
        return;
    }

    // Convert datetime-local to ISO string with Z
    let timestampIso = null;
    if (editSensorTimestamp.value) {
        const local = editSensorTimestamp.value; // yyyy-MM-ddTHH:mm
        const dt = new Date(local);
        timestampIso = dt.toISOString();
    }

    try {
        const body = {
            sensorType: newType,
            value: newValue,
            busId: newBusId
        };
        if (timestampIso) body.timestamp = timestampIso;

        const response = await fetch(`/api/sensors/${editingSensorId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body),
            credentials: 'include'
        });

        if (response.ok) {
            showMessage('Датчик обновлен', 'success');
            closeEditSensorModal();
            getAllSensors();
        } else {
            const text = await response.text();
            console.error('Ошибка при обновлении датчика:', text);
            showMessage('Ошибка обновления датчика', 'error');
        }
    } catch (error) {
        console.error('Ошибка при обновлении датчика:', error);
        showMessage('Ошибка обновления датчика', 'error');
    }
});

// Функция для начала редактирования датчика
async function startEditSensor(id, currentType, currentValue, currentBusId) {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может редактировать датчики', 'error');
        return;
    }

    // Получаем текущие данные датчика для отображения времени и аномалии
    try {
        const response = await fetch(`/api/sensors/${id}`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const sensor = await response.json();
            // Replace table row with edit form
            const row = document.getElementById(`sensor-row-${id}`);
            const sensorTypes = ['ENGINE_TEMP', 'TIRE_PRESSURE', 'FUEL_LEVEL'];

            let typeOptions = '';
            sensorTypes.forEach(type => {
                const selected = type === currentType ? 'selected' : '';
                typeOptions += `<option value="${type}" ${selected}>${type}</option>`;
            });

            row.innerHTML = `
                <td>${id}</td>
                <td><select id="edit-type-${id}">${typeOptions}</select></td>
                <td><input type="number" id="edit-value-${id}" value="${currentValue}" step="any"></td>
                <td>${sensor.timestamp}</td>
                <td>${sensor.anomaly ? 'Да' : 'Нет'}</td>
                <td>
                    <button onclick="saveEditSensor(${id}, ${currentBusId})">Сохранить</button>
                    <button onclick="cancelEditSensor(${id}, '${currentType}', ${currentValue}, ${currentBusId})">Отмена</button>
                </td>
            `;
        } else {
            showMessage('Ошибка получения данных датчика', 'error');
        }
    } catch (error) {
        console.error('Ошибка получения данных датчика:', error);
        showMessage('Ошибка получения данных датчика', 'error');
    }
}

// Функция для сохранения изменений датчика
async function saveEditSensor(id, currentBusId) {
    const newType = document.getElementById(`edit-type-${id}`).value;
    const newValue = parseFloat(document.getElementById(`edit-value-${id}`).value);

    if (isNaN(newValue)) {
        showMessage('Неверное значение датчика', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/sensors/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sensorType: newType,
                value: newValue,
                busId: currentBusId
                // Don't send timestamp to preserve original time
            }),
            credentials: 'include'
        });

        if (response.ok) {
            showMessage('Датчик обновлен', 'success');
            getAllSensors(); // Обновляем список
        } else {
            showMessage('Ошибка обновления датчика', 'error');
        }
    } catch (error) {
        console.error('Ошибка обновления датчика:', error);
        showMessage('Ошибка обновления датчика', 'error');
    }
}

// Function to cancel sensor editing
function cancelEditSensor(id, originalType, originalValue, originalBusId) {
    // Restore original row
    getAllSensors(); // Easier to just refresh the whole list
}

// Функция для редактирования датчика
async function editSensor(id, currentType, currentValue, currentBusId) {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может редактировать датчики', 'error');
        return;
    }

    const sensorTypes = ['ENGINE_TEMP', 'TIRE_PRESSURE', 'FUEL_LEVEL'];
    const newType = prompt(`Введите новый тип датчика (${sensorTypes.join(', ')}):`, currentType);
    if (!newType || !sensorTypes.includes(newType.toUpperCase())) {
        showMessage('Неверный тип датчика', 'error');
        return;
    }

    const newValue = parseFloat(prompt('Введите новое значение датчика:', currentValue));
    if (isNaN(newValue)) {
        showMessage('Неверное значение датчика', 'error');
        return;
    }

    const newBusId = parseInt(prompt('Введите новый ID автобуса:', currentBusId));
    if (isNaN(newBusId)) {
        showMessage('Неверный ID автобуса', 'error');
        return;
    }

    try {
        const response = await fetch(`/api/sensors/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sensorType: newType.toUpperCase(),
                value: newValue,
                busId: newBusId
                // Не передаем timestamp, чтобы сохранить оригинальное время
            }),
            credentials: 'include'
        });

        if (response.ok) {
            showMessage('Датчик обновлен', 'success');
            getAllSensors(); // Обновляем список
        } else {
            showMessage('Ошибка обновления датчика', 'error');
        }
    } catch (error) {
        console.error('Ошибка обновления датчика:', error);
        showMessage('Ошибка обновления датчика', 'error');
    }
}

// Функция для удаления датчика
async function deleteSensor(id) {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может удалять датчики', 'error');
        return;
    }

    if (!confirm('Вы уверены, что хотите удалить этот датчик?')) {
        return;
    }

    try {
        const response = await fetch(`/api/sensors/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok) {
            showMessage('Датчик удален', 'success');
            getAllSensors(); // Обновляем список
        } else {
            showMessage('Ошибка удаления датчика', 'error');
        }
    } catch (error) {
        console.error('Ошибка удаления датчика:', error);
        showMessage('Ошибка удаления датчика', 'error');
    }
}

async function createSensor() {
    const sensorTypes = ['ENGINE_TEMP', 'TIRE_PRESSURE', 'FUEL_LEVEL'];
    const sensorType = prompt(`Введите тип датчика (${sensorTypes.join(', ')}):`);
    if (!sensorType || !sensorTypes.includes(sensorType.toUpperCase())) {
        showMessage('Неверный тип датчика', 'error');
        return;
    }

    const value = parseFloat(prompt('Введите значение датчика:'));
    if (isNaN(value)) {
        showMessage('Неверное значение датчика', 'error');
        return;
    }

    const busId = parseInt(prompt('Введите ID автобуса:'));
    if (isNaN(busId)) {
        showMessage('Неверный ID автобуса', 'error');
        return;
    }

    try {
        const response = await fetch('/api/sensors', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sensorType: sensorType.toUpperCase(),
                value,
                busId,
                timestamp: new Date().toISOString().slice(0, 19).replace('T', ' ') + '.000000' // Add current time in a format suitable for PostgreSQL
            }),
            credentials: 'include'
        });

        if (response.ok) {
            const newSensor = await response.json();
            showMessage(`Датчик создан: ${newSensor.id}`, 'success');
            getAllSensors(); // Обновляем список
        } else {
            showMessage('Ошибка создания датчика', 'error');
        }
    } catch (error) {
        console.error('Ошибка создания датчика:', error);
        showMessage('Ошибка создания датчика', 'error');
    }
}

async function getAlerts() {
    try {
        const response = await fetch('/api/sensors/alerts', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const alerts = await response.json();
            displayAlerts(alerts);
        } else {
            showMessage('Ошибка получения тревог', 'error');
        }
    } catch (error) {
        console.error('Ошибка получения тревог:', error);
        showMessage('Ошибка получения тревог', 'error');
    }
}

function displayAlerts(alerts) {
    let html = '<h3>Тревоги</h3>';

    if (alerts.length === 0) {
        html += '<p>Нет активных тревог</p>';
    } else {
        html += '<table><thead><tr><th>ID</th><th>Тип датчика</th><th>Значение</th><th>Статус</th></tr></thead><tbody>';

        alerts.forEach(alert => {
            // Determine status based on anomaly
            let status = alert.anomaly ? 'ERROR' : 'OK';

            // Also determine status based on value and sensor type
            if (alert.sensorType === 'ENGINE_TEMP' && alert.value > 110) {
                status = 'ERROR';
            } else if (alert.sensorType === 'TIRE_PRESSURE' && (alert.value > 5.0 || alert.value < 1.5)) {
                status = 'ERROR';
            } else if (alert.sensorType === 'FUEL_LEVEL' && alert.value < 3.0) {
                status = 'ERROR';
            } else if (
                (alert.sensorType === 'ENGINE_TEMP' && (alert.value < 70 || alert.value > 110)) ||
                (alert.sensorType === 'TIRE_PRESSURE' && Math.abs(alert.value - 4.5) > 0.5 && Math.abs(alert.value - 4.5) <= 1.0) ||
                (alert.sensorType === 'FUEL_LEVEL' && alert.value >= 3.0 && alert.value < 20.0)
            ) {
                status = 'WARNING';
            }

            html += `<tr>
                <td>${alert.id}</td>
                <td>${alert.sensorType}</td>
                <td>${alert.value}</td>
                <td>${status}</td>
            </tr>`;
        });

        html += '</tbody></table>';
    }

    resultContainer.innerHTML = html;
}

function exportCsv() {
    // Create link for downloading CSV file
    const link = document.createElement('a');
    link.href = '/api/sensors/export-csv';
    link.download = 'sensor_data.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function importCsv() {
    if (currentUserRole !== 'ADMIN') {
        showMessage('Только администратор может импортировать CSV', 'error');
        return;
    }

    // Для простоты используем стандартный input для файла
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.csv';

    input.onchange = (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        fetch('/api/sensors/import-csv', {
            method: 'POST',
            body: formData,
            credentials: 'include'
        })
        .then(response => response.json())
        .then(data => {
            if (data.errors && data.errors.length > 0) {
                showMessage(`Импорт завершен с ошибками: ${data.errors.join(', ')}`, 'error');
            } else {
                showMessage(`Импорт завершен успешно: ${data.successCount} записей`, 'success');
            }
        })
        .catch(error => {
            console.error('Ошибка импорта CSV:', error);
            showMessage('Ошибка импорта CSV', 'error');
        });
    };

    input.click();
}

// Close modal on click
document.querySelector('.close').addEventListener('click', closeAuthModal);

// Close modal outside click
window.addEventListener('click', (e) => {
    if (e.target === modal) {
        closeAuthModal();
    }
});

// Show modal if not authenticated
window.addEventListener('DOMContentLoaded', async () => {
    const authStatus = await checkAuthStatus();
    if (!authStatus) {
        showAuthModal();
    }
});

// Show message function
function showMessage(message, type) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `alert-${type}`;
    messageDiv.textContent = message;

    // Add to beginning of results container
    resultContainer.insertBefore(messageDiv, resultContainer.firstChild);

    // Auto remove message after 5 seconds
    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.parentNode.removeChild(messageDiv);
        }
    }, 5000);
}