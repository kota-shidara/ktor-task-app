import { useEffect, useState } from "react";
import { useAuth } from "../contexts/AuthContext"
import client from "../api/client";
import NewTaskForm from "../contexts/NewTaskForm";

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

interface Task {
  id: number;
  title: string;
  description: string;
  priority: TaskPriority;
  status: TaskStatus;
}

const dashboard = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const { name, logout, deleteAccount } = useAuth()

  useEffect(() => {
    fetchTasks()
  }, []);

  const fetchTasks = async () => {
    try {
      const res = await client.get('/api/tasks');
      setTasks(res.data);
    } catch (e: any) {
      console.log(`タスク読み込み失敗 ${e}`);
    }
  };

  const handleStatusChange = async (id: number, title: string, description: string, priority: TaskPriority, status: TaskStatus) => {
    try {
      await client.put(`/api/tasks/${id}`, { id, title, description, priority, status })
      fetchTasks()
    } catch (e) { console.log(`タスク更新失敗 ${e}`)}
  }

  const handleDelete = async (id: number) => {
    try {
      await client.delete(`/api/tasks/${id}`)
      fetchTasks()
    } catch (e) { console.log(`削除失敗 ${e}`)}
  }

  const handleDeleteAccount = async () => {
    setIsDeleting(true)
    setDeleteError(null)
    try {
      await deleteAccount()
    } catch (e) {
      setIsDeleting(false)
      setDeleteError('退会処理に失敗しました。もう一度お試しください。')
    }
  }

  return (
    <div className="p-8 bg-gray-50 min-h-screen">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-3xl font-bold">タスク一覧</h1>
          <p className="text-sm text-gray-500">{name}</p>
        </div>
        <div className="flex gap-2">
          <button
            className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded"
            onClick={logout}
          >
            ログアウト
          </button>
          <button
            className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
            onClick={() => setShowDeleteModal(true)}
          >
            退会
          </button>
        </div>
      </div>
      

      <NewTaskForm onCreateTask={fetchTasks} />
      
      <div className="grid gap-4">
        {tasks.map(task => {
          return (
            <div key={task.id} className="bg-white p-4 rounded shadow flex justify-between items-center border-l-4 border-blue-500">
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="font-bold text-lg">{task.title}</h3>
                  <span className="text-xs px-2 py-0.5 rounded bg-red-100 text-red-800">{task.priority}</span>
                </div>
                <p className="text-gray-600">{task.description}</p>
                <span className={`inline-block px-2 py-1 text-xs rounded mt-2 ${task.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                  {task.status}
                  </span>
              </div>
              <div className="flex gap-2">
                { task.status !== 'COMPLETED' && (
                  <button className="text-sm bg-gray-200 px-3 py-1 rounded hover:bg-gray-300"
                    onClick={() => handleStatusChange(task.id, task.title, task.description, task.priority, 'COMPLETED')}>完了にする</button>
                )}
                <button className="text-sm bg-red-100 text-red-600 px-3 py-1 rounded hover:bg-red-200"
                  onClick={() => handleDelete(task.id)}>削除する</button>
              </div>
            </div>
          )
        })}
      </div>

      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h2 className="text-xl font-bold text-red-600 mb-4">退会確認</h2>
            <p className="text-gray-700 mb-6">
              退会すると、全てのタスクも削除されます。この操作は取り消せません。本当に退会しますか？
            </p>
            {deleteError && (
              <p className="text-red-600 text-sm mb-4">{deleteError}</p>
            )}
            <div className="flex justify-end gap-3">
              <button
                className="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded"
                onClick={() => { setShowDeleteModal(false); setDeleteError(null) }}
                disabled={isDeleting}
              >
                キャンセル
              </button>
              <button
                className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded disabled:opacity-50"
                onClick={handleDeleteAccount}
                disabled={isDeleting}
              >
                {isDeleting ? '処理中...' : '退会する'}
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  )
}

export default dashboard
