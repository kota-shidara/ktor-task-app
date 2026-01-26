import client from '../api/client';
import { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Link } from 'react-router-dom';

const RegisterPage: React.FC = () => {
  const [name, setName] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const { login } = useAuth();

  const handleRegister = async () => {
    try {
      const res = await client.post('/api/auth/register', { name, email, password })
      if (res.data.token) {
        login(res.data.token, res.data.name)
        alert(`新規登録に成功 ${res.data.name}`)
      }
    } catch (e: any) {
      console.error(e);
      alert(`新規登録に失敗 ${e.response?.data}`)
    }
  }
  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="p-8 rounded shadow-md bg-white w-96">
        <h1 className="text-2xl fond-bold mb-4">
          新規登録
        </h1>
        <input
          className="w-full border p-2 mb-4 rouded"
          placeholder="name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          className="w-full border p-2 mb-4 rouded"
          placeholder="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          className="w-full border p-2 mb-4 rouded"
          placeholder="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button
          className="w-full bg-green-500 text-white p-2 rounded mb-2 hover:bg-green-600"
          onClick={handleRegister}
        >
          新規登録
        </button>
        <div>
          <Link to="/login" className="text-blue-500 hover:underline">
            既にアカウントをお持ちであればログイン
          </Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage
