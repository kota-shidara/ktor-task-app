import { useState } from "react";
import { TaskPriority } from "../pages/Dashboard";
import client from "../api/client";

// const NewTaskForm: React.FC<{ onCreateTask: () => void }> = ({ onCreateTask }) => {
const NewTaskForm = ({ onCreateTask }: { onCreateTask: () => void }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')

  const handleCreate = async () => {
    try {
      await client.post('/tasks', { title, description, priority })
      setTitle('')
      setDescription('')
      setPriority('MEDIUM')
      onCreateTask()
    } catch (e) { console.log(`タスクの作成に失敗しました ${e}`)}
  }

  return (
    <div className="bg-white p-6 rounded shadow mb-8">
      <h2 className="text-xl font-semibold mb-4">タスクを追加</h2>
      <div className="flex gap-4 mb-2">
        <input
          className="border p-2 flex-1 rounded"
          value={title}
          placeholder="タイトル"
          onChange={(e) => setTitle(e.target.value)}
        />
        <input
          className="border p-2 flex-1 rounded"
          value={description}
          placeholder="詳細"
          onChange={(e) => setDescription(e.target.value)}
        />
        <select
          className="border p-2 rounded"
          value={priority}
          onChange={(e) => setPriority(e.target.value as TaskPriority)}
        >
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
        <button
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
          onClick={handleCreate}
        >
          作成
        </button>
      </div>
    </div>
  )
}

export default NewTaskForm;