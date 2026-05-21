import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../hooks/useAuth';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Card from '../components/common/Card';

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [phone, setPhone] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await register(name, email, password, phone || undefined);
      toast.success('Account created! Let\'s set up your business.');
      navigate('/dashboard');
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <Card className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-primary-600">Vyapaar Buddy</h1>
          <p className="text-gray-500 mt-2">Create your account</p>
        </div>
        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-2 text-sm">{error}</div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Full Name" type="text" value={name} onChange={e => setName(e.target.value)} placeholder="Enter your full name" required />
          <Input label="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Enter your email" required />
          <Input label="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Minimum 6 characters" required minLength={6} />
          <Input label="Phone (optional)" type="tel" value={phone} onChange={e => setPhone(e.target.value)} placeholder="10-digit mobile number" />
          <Button type="submit" className="w-full" isLoading={loading}>Sign Up</Button>
        </form>
        <p className="text-center mt-6 text-gray-600">
          Already have an account?{' '}
          <a href="/login" className="text-primary-600 hover:underline">Sign in</a>
        </p>
      </Card>
    </div>
  );
};

export default RegisterPage;
