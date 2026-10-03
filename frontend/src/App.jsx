import { useEffect, useState } from 'react'
import { getTasks, createTask, updateTask, deleteTask } from './api'
import './App.css'

function App() {
  const [tasks, setTasks] = useState([])
  const [title, setTitle] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Load tasks once when the app mounts.
  useEffect(() => {
    getTasks()
      .then(setTasks)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  async function handleAdd(e) {
    e.preventDefault()
    const trimmed = title.trim()
    if (!trimmed) return
    try {
      const created = await createTask(trimmed)
      setTasks([created, ...tasks])
      setTitle('')
      setError(null)
    } catch (e) {
      setError(e.message)
    }
  }

  async function handleToggle(task) {
    try {
      const updated = await updateTask({ ...task, completed: !task.completed })
      setTasks(tasks.map((t) => (t.id === updated.id ? updated : t)))
    } catch (e) {
      setError(e.message)
    }
  }

  async function handleDelete(id) {
    try {
      await deleteTask(id)
      setTasks(tasks.filter((t) => t.id !== id))
    } catch (e) {
      setError(e.message)
    }
  }

  const remaining = tasks.filter((t) => !t.completed).length

  return (
    <div className="app">
      <h1>📋 Task Manager</h1>
      <p className="subtitle">Spring Boot + React + H2</p>

      <form className="add-form" onSubmit={handleAdd}>
        <input
          type="text"
          placeholder="What needs to be done?"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <button type="submit">Add</button>
      </form>

      {error && <p className="error">⚠️ {error}</p>}

      {loading ? (
        <p className="muted">Loading tasks…</p>
      ) : tasks.length === 0 ? (
        <p className="muted">No tasks yet. Add your first one above!</p>
      ) : (
        <ul className="task-list">
          {tasks.map((task) => (
            <li key={task.id} className={task.completed ? 'done' : ''}>
              <label>
                <input
                  type="checkbox"
                  checked={task.completed}
                  onChange={() => handleToggle(task)}
                />
                <span>{task.title}</span>
              </label>
              <button
                className="delete"
                onClick={() => handleDelete(task.id)}
                aria-label="Delete task"
              >
                ✕
              </button>
            </li>
          ))}
        </ul>
      )}

      {!loading && tasks.length > 0 && (
        <p className="muted count">
          {remaining} of {tasks.length} remaining
        </p>
      )}
    </div>
  )
}

export default App
