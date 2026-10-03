// Thin wrapper around the backend REST API.
// Requests go to /api/... which Vite proxies to http://localhost:8081 (see vite.config.js).

const BASE = '/api/tasks'

async function handle(res) {
  if (!res.ok) {
    // The backend returns RFC 9457 Problem Details:
    // { status, title, detail, errors?: { field: message } }
    const problem = await res.json().catch(() => null)
    const fieldErrors = problem?.errors ? Object.values(problem.errors).join(', ') : null
    throw new Error(
      fieldErrors || problem?.detail || `Request failed with status ${res.status}`,
    )
  }
  // 204 No Content (delete) has no body
  return res.status === 204 ? null : res.json()
}

export function getTasks() {
  return fetch(BASE).then(handle)
}

export function createTask(title) {
  return fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title }),
  }).then(handle)
}

export function updateTask(task) {
  return fetch(`${BASE}/${task.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    // Only send the fields the API accepts (see UpdateTaskRequest)
    body: JSON.stringify({ title: task.title, completed: task.completed }),
  }).then(handle)
}

export function deleteTask(id) {
  return fetch(`${BASE}/${id}`, { method: 'DELETE' }).then(handle)
}
