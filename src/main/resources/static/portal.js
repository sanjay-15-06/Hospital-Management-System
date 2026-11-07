const role = document.body.dataset.role;
const roleInfo = {
  ADMIN: {title: 'Admin portal', subtitle: 'Hospital operations overview and records'},
  DOCTOR: {title: 'Doctor portal', subtitle: 'Patient care, appointments and clinical notes'},
  PHARMACY: {title: 'Pharmacy portal', subtitle: 'Prescription lookup and medicine pickup'}
};
const $ = id => document.getElementById(id);
let activeTab = role === 'ADMIN' ? 'dashboard' : role === 'DOCTOR' ? 'patients' : 'pharmacy';
let cache = {patients: [], doctors: [], appointments: [], prescriptions: [], reports: []};
let medicineDraft = [];
let currentRecords = [];

async function api(url, options = {}) {
  const response = await fetch(url, {
    credentials: 'same-origin',
    headers: {'Content-Type': 'application/json', ...(options.headers || {})},
    ...options
  });
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try { message = (await response.json()).error || message; } catch {}
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

function esc(value) {
  return String(value ?? '').replace(/[&<>"']/g, char =>
    ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[char]));
}

function notify(message, error = false) {
  const target = $('notice');
  if (!target) return;
  target.textContent = message;
  target.className = error ? 'notice error' : 'notice';
}

function renderLogin() {
  $('app').innerHTML = `<section class="login-card">
    <p class="eyebrow">${roleInfo[role].title.toUpperCase()}</p>
    <h1>Welcome back</h1>
    <p>Sign in to access your role-specific CarePoint workspace.</p>
    <form class="login-form" id="loginForm">
      <div class="field"><label for="username">Username</label><input id="username" autocomplete="username" required></div>
      <div class="field"><label for="password">Password</label><input id="password" type="password" autocomplete="current-password" required></div>
      <button class="primary" type="submit">Sign in</button>
      <p class="login-error" id="loginError" role="status"></p>
    </form>
    <a class="button-link" href="/">← All portals</a>
  </section>`;
  $('loginForm').addEventListener('submit', async event => {
    event.preventDefault();
    $('loginError').textContent = '';
    try {
      await api('/api/auth/login', {method: 'POST', body: JSON.stringify({
        username: $('username').value, password: $('password').value, role
      })});
      await initializeWorkspace();
    } catch (error) {
      $('loginError').textContent = error.message;
    }
  });
}

function tabButton(id, label) {
  return `<button class="tab ${activeTab === id ? 'active' : ''}" data-tab="${id}">${label}</button>`;
}

function renderShell(user) {
  let tabs = '';
  if (role === 'ADMIN') tabs = tabButton('dashboard', 'Overview') + tabButton('patients', 'Patients')
    + tabButton('doctors', 'Doctors') + tabButton('appointments', 'Appointments');
  if (role === 'DOCTOR') tabs = tabButton('patients', 'Patient records') + tabButton('appointments', 'Appointments')
    + tabButton('reports', 'Visit reports') + tabButton('prescriptions', 'Prescriptions');
  if (role === 'PHARMACY') tabs = tabButton('pharmacy', 'Prescription pickup');
  $('app').innerHTML = `<div class="shell-head">
    <div><p class="eyebrow">${roleInfo[role].title.toUpperCase()}</p><h1>${roleInfo[role].title}</h1>
      <p>${roleInfo[role].subtitle}</p></div>
    <div class="user-actions"><span>Signed in as <strong>${esc(user.username)}</strong></span><button class="secondary" id="logoutBtn">Sign out</button></div>
  </div>
  ${role === 'ADMIN' ? `<section class="hero-band"><div><p class="eyebrow">CAREPOINT HOSPITAL</p><h2>Hospital overview</h2><p>Live totals and operational activity from your JSON records.</p></div><span class="hero-icon">✚</span></section><div id="stats" class="stats-grid"></div>` : ''}
  <nav class="tabs">${tabs}</nav><section id="workspace" class="panel"></section><p id="notice" class="notice" role="status"></p>`;
  $('logoutBtn').addEventListener('click', async () => {
    try { await api('/api/auth/logout', {method: 'POST'}); } finally { renderLogin(); }
  });
  document.querySelectorAll('.tab').forEach(button => button.addEventListener('click', () => {
    activeTab = button.dataset.tab;
    document.querySelectorAll('.tab').forEach(tab => tab.classList.toggle('active', tab === button));
    loadCurrentTab();
  }));
}

async function initializeWorkspace() {
  try {
    const user = await api('/api/auth/me');
    if (user.role !== role) {
      await api('/api/auth/logout', {method: 'POST'});
      renderLogin();
      return;
    }
    renderShell(user);
    if (role === 'ADMIN') await loadDashboard();
    await loadCurrentTab();
  } catch {
    renderLogin();
  }
}

async function loadCurrentTab() {
  try {
    if (role === 'ADMIN' && activeTab === 'dashboard') return await loadDashboard();
    if (activeTab === 'patients') return await loadPatients();
    if (activeTab === 'doctors') return await loadDoctors();
    if (activeTab === 'appointments') return await loadAppointments();
    if (activeTab === 'reports') return await loadReports();
    if (activeTab === 'prescriptions') return await loadPrescriptions();
    if (activeTab === 'pharmacy') return renderPharmacy();
  } catch (error) { notify(error.message, true); }
}

async function loadDashboard() {
  const data = await api('/api/dashboard');
  $('stats').innerHTML = [
    ['Patients', data.patientCount], ['Doctors', data.doctorCount],
    ['Appointments', data.appointmentCount], ["Today's visits", data.todayAppointments],
    ['Scheduled', data.scheduledAppointments], ['Completed', data.completedAppointments],
    ['Prescriptions', data.prescriptionCount], ['Dispensed', data.dispensedPrescriptions]
  ].map(([label, count]) => `<div class="stat-card"><span>${label}</span><strong>${count}</strong></div>`).join('');
  $('workspace').innerHTML = `<div class="panel-head"><div><h2>Recent appointments</h2><p>Latest scheduled visits across the hospital.</p></div></div>
    ${appointmentTable(data.recentAppointments || [], false)}`;
}

function field(name, label, type = 'text', options = [], value = '') {
  const id = `f-${name}`;
  let input;
  if (type === 'select') {
    input = `<select id="${id}" name="${name}" required>${options.map(option =>
      `<option value="${esc(option)}" ${String(value) === String(option) ? 'selected' : ''}>${esc(option)}</option>`).join('')}</select>`;
  } else {
    input = `<input id="${id}" name="${name}" type="${type}" value="${esc(value)}" ${type === 'number' ? 'min="1"' : ''} required>`;
  }
  return `<div class="field"><label for="${id}">${label}</label>${input}</div>`;
}

function formSelect(name, label, records, getValue, getLabel, selected = '') {
  return `<div class="field"><label for="f-${name}">${label}</label><select id="f-${name}" name="${name}" required>
    <option value="">Choose ${label.toLowerCase()}</option>${records.map(record => {
      const value = getValue(record);
      return `<option value="${esc(value)}" ${String(value) === String(selected) ? 'selected' : ''}>${esc(getLabel(record))}</option>`;
    }).join('')}</select></div>`;
}

function buildRecordTable(items, columns, rows, endpoint) {
  currentRecords = items;
  $('workspace').innerHTML = `<div class="panel-head"><div><h2>${columns.title}</h2><p>${columns.description}</p></div>
    <button class="primary" id="addRecord">＋ Add ${columns.singular}</button></div>
    <div id="recordFormWrap"></div>${tableMarkup(items, columns.headers, rows, true)}`;
  $('addRecord').addEventListener('click', () => showRecordForm(null, columns, endpoint));
  $('workspace').onclick = async event => {
    const edit = event.target.dataset.edit;
    const del = event.target.dataset.delete;
    if (edit) showRecordForm(items.find(item => item.id === Number(edit)), columns, endpoint);
    if (del && confirm('Delete this record?')) {
      try {
        await api(`${endpoint}/${del}`, {method: 'DELETE'});
        await loadCurrentTab();
        await refreshDashboardStats();
        notify('Record deleted.');
      } catch (error) { notify(error.message, true); }
    }
  };
}

function tableMarkup(items, headers, rows, actions = false) {
  if (!items.length) return `<div class="empty">No records found.</div>`;
  return `<div class="table-wrap"><table><thead><tr>${headers.map(header => `<th>${esc(header)}</th>`).join('')}${actions ? '<th>Actions</th>' : ''}</tr></thead>
    <tbody>${items.map((item, index) => `<tr>${rows(item).map(value => `<td>${esc(value)}</td>`).join('')}
      ${actions ? `<td><button class="action edit" data-edit="${item.id}">Edit</button><button class="action danger" data-delete="${item.id}">Delete</button></td>` : ''}</tr>`).join('')}</tbody></table></div>`;
}

function showRecordForm(item, config, endpoint) {
  const values = item || {};
  const fields = config.fields.map(([name, label, type, options]) =>
    field(name, label, type, options || [], values[name] ?? '')).join('');
  $('recordFormWrap').innerHTML = `<div class="form-wrap"><h3>${item ? 'Edit' : 'Add'} ${config.singular}</h3>
    <form id="recordForm"><div class="form-grid">${fields}</div><div class="form-actions"><button type="button" class="secondary" id="cancelForm">Cancel</button><button class="primary">Save record</button></div></form></div>`;
  $('cancelForm').addEventListener('click', () => $('recordFormWrap').innerHTML = '');
  $('recordForm').addEventListener('submit', async event => {
    event.preventDefault();
    const payload = {};
    for (const [name, , type] of config.fields) {
      const value = $(`f-${name}`).value;
      payload[name] = type === 'number' ? Number(value) : value;
    }
    try {
      await api(`${endpoint}${item ? `/${item.id}` : ''}`, {
        method: item ? 'PUT' : 'POST', body: JSON.stringify(payload)
      });
      await loadCurrentTab();
      await refreshDashboardStats();
      notify('Record saved successfully.');
    } catch (error) { notify(error.message, true); }
  });
}

async function refreshDashboardStats() {
  if (role !== 'ADMIN') return;
  const data = await api('/api/dashboard');
  $('stats').innerHTML = [
    ['Patients', data.patientCount], ['Doctors', data.doctorCount],
    ['Appointments', data.appointmentCount], ["Today's visits", data.todayAppointments],
    ['Scheduled', data.scheduledAppointments], ['Completed', data.completedAppointments],
    ['Prescriptions', data.prescriptionCount], ['Dispensed', data.dispensedPrescriptions]
  ].map(([label, count]) => `<div class="stat-card"><span>${label}</span><strong>${count}</strong></div>`).join('');
}

async function loadPatients() {
  cache.patients = await api('/api/patients');
  if (role === 'ADMIN') {
    const config = {title: 'Patient records', singular: 'patient', description: 'Add, view, edit and remove patient records.',
      headers: ['ID', 'Name', 'Age', 'Gender', 'Phone', 'Address'],
      fields: [['name', 'Full name', 'text'], ['age', 'Age', 'number'], ['gender', 'Gender', 'select', ['Male', 'Female', 'Other']],
        ['phone', 'Phone', 'tel'], ['address', 'Address', 'text']]};
    buildRecordTable(cache.patients, config, patient => [patient.id, patient.name, patient.age, patient.gender, patient.phone, patient.address], '/api/patients');
  } else {
    $('workspace').innerHTML = `<div class="panel-head"><div><h2>Patient records</h2><p>Review patient demographics before a visit.</p></div></div>
      ${tableMarkup(cache.patients, ['ID', 'Name', 'Age', 'Gender', 'Phone', 'Address'],
        patient => [patient.id, patient.name, patient.age, patient.gender, patient.phone, patient.address])}`;
  }
}

async function loadDoctors() {
  cache.doctors = await api('/api/doctors');
  const config = {title: 'Doctor directory', singular: 'doctor', description: 'Maintain doctor details and specializations.',
    headers: ['ID', 'Name', 'Specialization', 'Phone'],
    fields: [['name', 'Doctor name', 'text'], ['specialization', 'Specialization', 'text'], ['phone', 'Phone', 'tel']]};
  buildRecordTable(cache.doctors, config, doctor => [doctor.id, doctor.name, doctor.specialization, doctor.phone], '/api/doctors');
}

function appointmentTable(items, actions) {
  return tableMarkup(items, ['ID', 'Patient ID', 'Doctor ID', 'Date', 'Reason', 'Status'],
    item => [item.id, item.patientId, item.doctorId, item.appointmentDate, item.reason, item.status], actions);
}

async function loadAppointments() {
  cache.appointments = await api('/api/appointments');
  if (role === 'ADMIN') {
    const config = {title: 'Appointments', singular: 'appointment', description: 'Schedule and track patient appointments.',
      headers: ['ID', 'Patient ID', 'Doctor ID', 'Date', 'Reason', 'Status'],
      fields: [['patientId', 'Patient ID', 'number'], ['doctorId', 'Doctor ID', 'number'],
        ['appointmentDate', 'Date and time', 'datetime-local'], ['reason', 'Reason', 'text'],
        ['status', 'Status', 'select', ['Scheduled', 'Completed', 'Cancelled']] ]};
    buildRecordTable(cache.appointments, config, item =>
      [item.id, item.patientId, item.doctorId, item.appointmentDate, item.reason, item.status], '/api/appointments');
    return;
  }
  $('workspace').innerHTML = `<div class="panel-head"><div><h2>Appointments</h2><p>Review today's schedule and update visit status or details.</p></div></div>
    <div id="appointmentEdit"></div>${appointmentTable(cache.appointments, true)}`;
  $('workspace').onclick = event => {
    const id = event.target.dataset.edit;
    if (id) editAppointment(cache.appointments.find(item => item.id === Number(id)));
  };
}

function editAppointment(item) {
  $('appointmentEdit').innerHTML = `<div class="form-wrap"><h3>Update appointment #${item.id}</h3><form id="appointmentForm">
    <div class="form-grid">${field('appointmentDate', 'Date and time', 'datetime-local', [], item.appointmentDate || '')}
    ${field('reason', 'Reason', 'text', [], item.reason || '')}
    ${field('status', 'Status', 'select', ['Scheduled', 'Completed', 'Cancelled'], item.status || 'Scheduled')}</div>
    <div class="form-actions"><button type="button" class="secondary" id="cancelAppointment">Cancel</button><button class="primary">Save changes</button></div></form></div>`;
  $('cancelAppointment').addEventListener('click', () => $('appointmentEdit').innerHTML = '');
  $('appointmentForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
      await api(`/api/appointments/${item.id}`, {method: 'PUT', body: JSON.stringify({
        ...item, appointmentDate: $('f-appointmentDate').value, reason: $('f-reason').value, status: $('f-status').value
      })});
      await loadAppointments();
      notify('Appointment updated.');
    } catch (error) { notify(error.message, true); }
  });
}

async function loadReports() {
  const [reports, patients, doctors] = await Promise.all([api('/api/reports'), api('/api/patients'), api('/api/doctors')]);
  cache.reports = reports; cache.patients = patients; cache.doctors = doctors;
  $('workspace').innerHTML = `<div class="panel-head"><div><h2>Patient visit reports</h2><p>Document a concise visit summary and follow-up guidance.</p></div></div>
    <div class="form-wrap"><h3>New visit report</h3><form id="reportForm"><div class="form-grid">
      ${formSelect('patientId', 'Patient', patients, p => p.id, p => `${p.name} · #${p.id}`)}
      ${formSelect('doctorId', 'Doctor', doctors, d => d.id, d => `${d.name} · ${d.specialization}`)}
      <div class="field wide"><label for="f-summary">Visit summary</label><textarea id="f-summary" required></textarea></div>
      <div class="field"><label for="f-diagnosis">Diagnosis / assessment</label><textarea id="f-diagnosis"></textarea></div>
      <div class="field"><label for="f-recommendations">Recommendations / follow-up</label><textarea id="f-recommendations"></textarea></div>
    </div><div class="form-actions"><button class="primary">Save report</button></div></form></div>
    <div class="panel-head"><div><h2>Recent reports</h2></div></div>${renderReportCards(reports, patients, doctors)}`;
  $('reportForm').addEventListener('submit', async event => {
    event.preventDefault();
    const body = {patientId: Number($('f-patientId').value), doctorId: Number($('f-doctorId').value),
      summary: $('f-summary').value, diagnosis: $('f-diagnosis').value, recommendations: $('f-recommendations').value};
    try {
      await api('/api/reports', {method: 'POST', body: JSON.stringify(body)});
      await loadReports();
      notify('Visit report saved.');
    } catch (error) { notify(error.message, true); }
  });
}

function renderReportCards(reports, patients, doctors) {
  if (!reports.length) return '<div class="empty">No visit reports yet.</div>';
  return [...reports].reverse().map(report => {
    const patient = patients.find(item => item.id === report.patientId);
    const doctor = doctors.find(item => item.id === report.doctorId);
    return `<article class="report-card"><div class="prescription-card-head"><strong>${esc(patient?.name || `Patient #${report.patientId}`)} · Report #${report.id}</strong><span class="muted">${esc(report.createdAt)}</span></div>
      <p>Dr. ${esc(doctor?.name || report.doctorId)} · ${esc(report.summary)}</p>
      ${report.diagnosis ? `<p><strong>Assessment:</strong> ${esc(report.diagnosis)}</p>` : ''}
      ${report.recommendations ? `<p><strong>Follow-up:</strong> ${esc(report.recommendations)}</p>` : ''}</article>`;
  }).join('');
}

async function loadPrescriptions() {
  const [prescriptions, patients, doctors, appointments] = await Promise.all([
    api('/api/prescriptions'), api('/api/patients'), api('/api/doctors'), api('/api/appointments')
  ]);
  cache.prescriptions = prescriptions; cache.patients = patients; cache.doctors = doctors; cache.appointments = appointments;
  medicineDraft = [];
  $('workspace').innerHTML = `<div class="panel-head"><div><h2>Prescriptions</h2><p>Create a pharmacy-ready medicine plan tied to the patient's hospital ID.</p></div></div>
    <div class="form-wrap"><h3>New prescription</h3><form id="prescriptionForm"><div class="form-grid">
      ${formSelect('patientId', 'Patient', patients, p => p.id, p => `${p.name} · #${p.id}`)}
      ${formSelect('doctorId', 'Prescribing doctor', doctors, d => d.id, d => `${d.name} · ${d.specialization}`)}
      ${formSelect('appointmentId', 'Related appointment (optional)', [{id: 'none', patientId: ''}, ...cache.appointments], a => a.id,
        a => a.id === 'none' ? 'No linked appointment' : `Appointment #${a.id} · patient #${a.patientId}`, 'none')}
      <div class="field wide"><label for="f-patientSummary">Patient report / brief clinical summary</label><textarea id="f-patientSummary" placeholder="Brief context for the pharmacy team"></textarea></div>
      <div class="field wide"><label for="f-diagnosis">Diagnosis / assessment</label><textarea id="f-diagnosis"></textarea></div>
      <div class="field wide"><label for="f-notes">Additional notes</label><textarea id="f-notes"></textarea></div>
    </div><div class="line-form"><h4>Medicine details</h4><div class="form-grid">
      ${field('medicineName', 'Medicine name', 'text')}
      ${field('strength', 'Strength (e.g. 500 mg)', 'text')}
      ${field('dosage', 'Dose (e.g. 1 tablet)', 'text')}
      ${field('durationDays', 'Duration (days)', 'number')}
      ${field('quantity', 'Quantity to dispense', 'number')}
      ${field('foodTiming', 'Food timing', 'select', ['Before food', 'After food', 'With food', 'Any time'])}
      <div class="field wide"><label>Time of day (select all that apply)</label><div class="check-row">${['Morning', 'Afternoon', 'Evening', 'Night'].map(time =>
        `<label><input type="checkbox" name="medicineTime" value="${time}"> ${time}</label>`).join('')}</div></div>
      <div class="field wide"><label for="f-instructions">Additional instructions (optional)</label><input id="f-instructions" placeholder="e.g. Take with a full glass of water"></div>
    </div><div class="form-actions"><button type="button" class="secondary" id="addMedicine">＋ Add medicine to prescription</button></div>
    <div id="medicineDraftList"></div></div><div class="form-actions"><button class="primary">Save prescription</button></div></form></div>
    <div class="panel-head"><div><h2>Prescription history</h2><p>Prescription ID can be used to identify a pickup at the pharmacy.</p></div></div>
    ${renderPrescriptionCards(prescriptions, patients, doctors, false)}`;
  $('addMedicine').addEventListener('click', addMedicineToDraft);
  $('prescriptionForm').addEventListener('submit', async event => {
    event.preventDefault();
    if (!medicineDraft.length) { notify('Add at least one medicine before saving.', true); return; }
    const appointmentId = $('f-appointmentId').value;
    const body = {patientId: Number($('f-patientId').value), doctorId: Number($('f-doctorId').value),
      appointmentId: appointmentId !== 'none' ? Number(appointmentId) : null,
      patientSummary: $('f-patientSummary').value, diagnosis: $('f-diagnosis').value,
      notes: $('f-notes').value, items: medicineDraft};
    try {
      const saved = await api('/api/prescriptions', {method: 'POST', body: JSON.stringify(body)});
      await loadPrescriptions();
      notify(`Prescription #${saved.id} saved and ready for pharmacy pickup.`);
    } catch (error) { notify(error.message, true); }
  });
  $('medicineDraftList').addEventListener('click', event => {
    const remove = event.target.dataset.removeMedicine;
    if (remove !== undefined) {
      medicineDraft.splice(Number(remove), 1);
      renderDraftMedicine();
    }
  });
}

function addMedicineToDraft() {
  const times = [...document.querySelectorAll('input[name="medicineTime"]:checked')].map(input => input.value);
  const item = {medicineName: $('f-medicineName').value.trim(), strength: $('f-strength').value.trim(),
    dosage: $('f-dosage').value.trim(), durationDays: Number($('f-durationDays').value),
    quantity: Number($('f-quantity').value), foodTiming: $('f-foodTiming').value, times,
    instructions: $('f-instructions').value.trim()};
  if (!item.medicineName || !item.durationDays || !item.quantity || !item.dosage || !times.length) {
    notify('Enter the medicine, dose, duration and quantity, then select at least one time.', true);
    return;
  }
  medicineDraft.push(item);
  ['medicineName', 'strength', 'dosage', 'durationDays', 'quantity', 'instructions'].forEach(name => $(`f-${name}`).value = '');
  document.querySelectorAll('input[name="medicineTime"]').forEach(input => input.checked = false);
  renderDraftMedicine();
  notify('Medicine added to this prescription.');
}

function renderDraftMedicine() {
  $('medicineDraftList').innerHTML = medicineDraft.map((item, index) =>
    `<div class="medicine-line"><strong>${esc(item.medicineName)}</strong> ${esc(item.strength)} · ${esc(item.dosage)} · ${item.durationDays} days · ${esc(item.foodTiming)} · ${item.times.map(esc).join(', ')} · Qty ${item.quantity}
      <button class="action danger" type="button" data-remove-medicine="${index}">Remove</button></div>`).join('');
}

function renderPrescriptionCards(prescriptions, patients, doctors, pharmacyMode) {
  if (!prescriptions.length) return '<div class="empty">No prescriptions found.</div>';
  return [...prescriptions].reverse().map(prescription => {
    const patient = patients?.find(item => item.id === prescription.patientId);
    const doctor = doctors?.find(item => item.id === prescription.doctorId);
    return `<article class="prescription-card"><div class="prescription-card-head"><div>
      <h3>Prescription #${prescription.id} · ${esc(prescription.patientName || patient?.name || `Patient #${prescription.patientId}`)}</h3>
      <p>Patient ID: <strong>${prescription.patientId}</strong> · ${esc(doctor ? `Dr. ${doctor.name}` : `Doctor #${prescription.doctorId}`)} · ${esc(prescription.createdAt)}</p>
      </div><span class="badge ${prescription.status === 'Dispensed' ? 'done' : ''}">${esc(prescription.status)}</span></div>
      ${prescription.patientSummary ? `<p><strong>Patient summary:</strong> ${esc(prescription.patientSummary)}</p>` : ''}
      ${prescription.diagnosis ? `<p><strong>Assessment:</strong> ${esc(prescription.diagnosis)}</p>` : ''}
      ${(prescription.items || []).map(item => `<div class="medicine-line"><strong>${esc(item.medicineName)}</strong>${item.strength ? ` · ${esc(item.strength)}` : ''} · ${esc(item.dosage)} · ${item.durationDays} days · ${esc(item.foodTiming)} · ${item.times.map(esc).join(', ')} · Qty ${item.quantity}${item.instructions ? `<br>${esc(item.instructions)}` : ''}</div>`).join('')}
      ${pharmacyMode && prescription.status !== 'Dispensed' ? `<button class="primary" data-dispense="${prescription.id}">Confirm medicine pickup</button>` : ''}
      ${prescription.dispensedAt ? `<p class="muted">Dispensed at ${esc(prescription.dispensedAt)}</p>` : ''}</article>`;
  }).join('');
}

function renderPharmacy() {
  $('workspace').innerHTML = `<div class="panel-head"><div><h2>Prescription pickup</h2><p>Search by patient ID or full/partial patient name to retrieve active and completed prescriptions.</p></div></div>
    <form id="pharmacySearch" class="search-row"><input id="patientSearch" placeholder="Enter patient ID or name" aria-label="Patient ID or name" required><button class="primary">Find prescriptions</button></form>
    <div id="pharmacyResults" style="margin-top:18px"></div>`;
  $('pharmacySearch').addEventListener('submit', async event => {
    event.preventDefault();
    const results = $('pharmacyResults');
    results.innerHTML = '<div class="empty">Searching records…</div>';
    try {
      const records = await api(`/api/pharmacy/prescriptions?search=${encodeURIComponent($('patientSearch').value.trim())}`);
      cache.prescriptions = records;
      results.innerHTML = renderPrescriptionCards(records, [], [], true);
      notify(records.length ? `${records.length} prescription(s) found.` : 'No prescriptions found for that patient.');
    } catch (error) { results.innerHTML = ''; notify(error.message, true); }
  });
  $('workspace').onclick = async event => {
    const id = event.target.dataset.dispense;
    if (!id || !confirm(`Confirm pickup and mark prescription #${id} as dispensed?`)) return;
    try {
      await api(`/api/pharmacy/prescriptions/${id}/dispense`, {method: 'POST'});
      $('pharmacySearch').requestSubmit();
      notify(`Prescription #${id} marked as dispensed.`);
    } catch (error) { notify(error.message, true); }
  };
}

initializeWorkspace();
