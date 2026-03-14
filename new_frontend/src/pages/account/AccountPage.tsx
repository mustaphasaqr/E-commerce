import { useEffect, useState } from 'react';
import axios from 'axios';

interface UserProfile {
  id: string;
  email: string;
  username: string;
  emailVerified: boolean;
  createdAt: string;
  // Add more fields as needed
}

export default function AccountPage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    axios.get('/api/v1/users/me')
      .then(res => {
        setProfile(res.data);
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to load account info');
        setLoading(false);
      });
  }, []);

  if (loading) return <div className="p-8">Loading account info...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;
  if (!profile) return null;

  return (
    <div className="max-w-xl mx-auto p-8">
      <h2 className="text-2xl font-bold mb-4">My Account</h2>
      <div className="bg-white rounded shadow p-6 space-y-4">
        <div><strong>Email:</strong> {profile.email}</div>
        <div><strong>Username:</strong> {profile.username}</div>
        <div><strong>Email Verified:</strong> {profile.emailVerified ? 'Yes' : 'No'}</div>
        <div><strong>Joined:</strong> {new Date(profile.createdAt).toLocaleDateString()}</div>
      </div>
    </div>
  );
}
