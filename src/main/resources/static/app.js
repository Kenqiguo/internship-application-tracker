const form = document.querySelector('#application-form');
const message = document.querySelector('#message');
const saveButton = document.querySelector('#save-button');
let editingId = null;
let loadVersion = 0;

function showMessage(text, isError = false) {
  message.textContent = text;
  message.classList.toggle('error', isError);
}

async function request(path, options = {}) {
  const sessionResponse = await fetch('/api/session');
  if (sessionResponse.status === 401) { location.href = '/login'; throw new Error('Please sign in.'); }
  const session = await sessionResponse.json();
  options.headers = { ...options.headers };
  if (session.headerName) options.headers[session.headerName] = session.token;
  const response = await fetch('/api/applications' + path, options);
  if (response.status === 401) { location.href = '/login'; throw new Error('Please sign in.'); }
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    const details = error.errors ? Object.values(error.errors).join('; ') : '';
    throw new Error(details || error.message || 'Request failed. Please try again.');
  }
  return response.status === 204 ? null : response.json();
}

function resetForm() {
  editingId = null;
  form.reset();
  document.querySelector('#form-title').textContent = 'Add Application';
  saveButton.textContent = 'Add Application';
  document.querySelector('#cancel-button').hidden = true;
}

async function loadApplications() {
  const version = ++loadVersion;
  const params = new URLSearchParams({ search: document.querySelector('#search').value });
  const status = document.querySelector('#filter-status').value;
  if (status) params.set('status', status);
  try {
    const applications = await request('?' + params);
    if (version !== loadVersion) return;
    const body = document.querySelector('#applications');
    body.replaceChildren();
    document.querySelector('#count').textContent = `${applications.length} application(s)`;
    document.querySelector('#empty').hidden = applications.length !== 0;
    for (const application of applications) {
      const row = document.createElement('tr');
      for (const value of [application.companyName, application.roleTitle, application.status, application.dateApplied || '—', application.notes || '—']) {
        const cell = document.createElement('td');
        cell.textContent = value; // Display user input as text, never as HTML.
        row.append(cell);
      }
      const actions = document.createElement('td');
      const edit = document.createElement('button');
      edit.textContent = 'Edit';
      edit.className = 'secondary';
      edit.setAttribute('aria-label', `Edit ${application.companyName}`);
      edit.addEventListener('click', () => {
        editingId = application.id;
        for (const key of ['companyName', 'roleTitle', 'status', 'dateApplied', 'notes']) {
          form.elements[key].value = application[key] || '';
        }
        document.querySelector('#form-title').textContent = 'Edit Application';
        saveButton.textContent = 'Save Changes';
        document.querySelector('#cancel-button').hidden = false;
        form.elements.companyName.focus();
      });
      const remove = document.createElement('button');
      remove.textContent = 'Delete';
      remove.className = 'danger';
      remove.setAttribute('aria-label', `Delete ${application.companyName}`);
      remove.addEventListener('click', async () => {
        if (!confirm(`Delete the application for ${application.companyName}?`)) return;
        remove.disabled = true;
        try {
          await request('/' + application.id, { method: 'DELETE' });
          if (editingId === application.id) resetForm();
          showMessage('Application deleted.');
          await loadApplications();
        } catch (error) { showMessage(error.message, true); }
        finally { remove.disabled = false; }
      });
      actions.append(edit, remove);
      row.append(actions);
      body.append(row);
    }
  } catch (error) { if (version === loadVersion) showMessage(error.message, true); }
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  const input = Object.fromEntries(new FormData(form));
  input.companyName = input.companyName.trim();
  input.roleTitle = input.roleTitle.trim();
  input.dateApplied = input.dateApplied || null;
  if (!input.companyName || !input.roleTitle) {
    showMessage('Company name and role title are required.', true);
    return;
  }
  saveButton.disabled = true;
  try {
    await request(editingId === null ? '' : '/' + editingId, {
      method: editingId === null ? 'POST' : 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(input)
    });
    resetForm();
    showMessage('Application saved. If it is hidden, check your filters.');
    await loadApplications();
  } catch (error) { showMessage(error.message, true); }
  finally { saveButton.disabled = false; }
});

document.querySelector('#cancel-button').addEventListener('click', resetForm);
document.querySelector('#filter-form').addEventListener('submit', event => {
  event.preventDefault();
  loadApplications();
});
document.querySelector('#filter-status').addEventListener('change', loadApplications);
loadApplications();


async function initializeSession() {
  const response = await fetch('/api/session');
  if (response.status === 401) { location.href = '/login'; return; }
  if (!response.ok) return;
  const session = await response.json();
  if (!session.token) return;
  document.querySelector('#access-notice').textContent = 'Private workspace — only you and your authorized automation can access your records.';
  const logout = document.querySelector('#logout-button');
  logout.hidden = false;
  logout.addEventListener('click', async () => {
    const current = await fetch('/api/session').then(r => r.json());
    const result = await fetch('/logout', { method: 'POST', headers: { [current.headerName]: current.token } });
    if (result.ok || result.redirected) location.href = '/login?logout';
  });
}
initializeSession().catch(() => {});
