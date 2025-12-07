import { useEffect, useState } from 'react';
import { createTask, deleteTask, loadTasks, updateTask } from '../services/api';
import { Task } from '../types';
import { useAuth } from '../contexts/AuthContext';

interface Props {
  sessionId: number;
}

const columns: Array<{ id: Task['status']; title: string }> = [
  { id: 'open', title: 'Новые' },
  { id: 'in_progress', title: 'В работе' },
  { id: 'done', title: 'Готово' }
];

const TaskBoard: React.FC<Props> = ({ sessionId }) => {
  const { tokens } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [text, setText] = useState('');

  const reload = () => {
    if (!tokens) return;
    loadTasks(tokens, sessionId).then(setTasks);
  };

  useEffect(() => {
    reload();
  }, [sessionId]);

  const handleCreate = async () => {
    if (!tokens || !text.trim()) return;
    await createTask(tokens, sessionId, { text });
    setText('');
    reload();
  };

  const moveTask = async (task: Task, status: Task['status']) => {
    if (!tokens || task.status === status) return;
    await updateTask(tokens, sessionId, task.id, { status });
    reload();
  };

  const removeTask = async (taskId: number) => {
    if (!tokens) return;
    await deleteTask(tokens, sessionId, taskId);
    reload();
  };

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <h3>Задачи</h3>
      <div style={{ display: 'flex', gap: 8 }}>
        <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Новая задача" style={{ flex: 1 }} />
        <button onClick={handleCreate}>Добавить</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
        {columns.map((column) => (
          <div key={column.id} className="task-column">
            <h4>{column.title}</h4>
            {tasks
              .filter((task) => task.status === column.id)
              .map((task) => (
                <div key={task.id} className="task-card">
                  <p>{task.text}</p>
                  <div className="task-actions">
                    {columns
                      .filter((c) => c.id !== task.status)
                      .map((c) => (
                        <button key={c.id} onClick={() => moveTask(task, c.id)}>
                          → {c.title}
                        </button>
                      ))}
                    <button onClick={() => removeTask(task.id)}>Удалить</button>
                  </div>
                </div>
              ))}
          </div>
        ))}
      </div>
    </div>
  );
};

export default TaskBoard;
