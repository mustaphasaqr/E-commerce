import React, { useEffect, useState } from 'react';
import { userService } from '@shared/services';
import type { User } from '@auth/types';
import ProfileOverview from './components/ProfileOverview';
import ChangeEmailForm from './components/ChangeEmailForm';
import ChangePasswordForm from './components/ChangePasswordForm';
import MarketingConsentToggle from './components/MarketingConsentToggle';
import styles from './index.module.scss';

/**
 * User Profile Management Page
 * Integrates with endpoints:
 * - GET /api/v1/users/me (get current user)
 * - PUT /api/v1/users/me/email (change email)
 * - PUT /api/v1/users/me/password (change password)
 * - POST /api/v1/users/me/marketing/grant (grant marketing consent)
 * - DELETE /api/v1/users/me/marketing (revoke marketing consent)
 */
export const UserProfilePage: React.FC = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'email' | 'password' | 'marketing'>('overview');

  useEffect(() => {
    loadUserProfile();
  }, []);

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      const currentUser = await userService.getCurrentUser();
      setUser(currentUser as User);
      setError(null);
    } catch (err) {
      setError('Failed to load profile. Please try again.');
      console.error('Profile load error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleEmailChanged = (newEmail: string) => {
    if (user) {
      setUser({ ...user, email: newEmail });
    }
  };

  const handleConsentChanged = (granted: boolean) => {
    if (user) {
      setUser({ ...user, marketingConsent: granted });
    }
  };

  if (loading) {
    return <section className={styles.section}><div className={`${styles.container} main-container`}>Loading profile...</div></section>;
  }

  if (!user) {
    return <section className={styles.section}><div className={`${styles.container} main-container`}>Failed to load user profile</div></section>;
  }

  return (
    <section className={styles.section}>
      <div className={`${styles.container} main-container`}>
        <h1 className={styles.pageTitle}>Account Settings</h1>
        
        {error && <div className={styles.errorBanner}>{error}</div>}

        <div className={styles.tabsContainer}>
          <div className={styles.tabsNavigation}>
            <button
              className={`${styles.tabBtn} ${activeTab === 'overview' ? styles.active : ''}`}
              onClick={() => setActiveTab('overview')}
            >
              Profile
            </button>
            <button
              className={`${styles.tabBtn} ${activeTab === 'email' ? styles.active : ''}`}
              onClick={() => setActiveTab('email')}
            >
              Email
            </button>
            <button
              className={`${styles.tabBtn} ${activeTab === 'password' ? styles.active : ''}`}
              onClick={() => setActiveTab('password')}
            >
              Password
            </button>
            <button
              className={`${styles.tabBtn} ${activeTab === 'marketing' ? styles.active : ''}`}
              onClick={() => setActiveTab('marketing')}
            >
              Preferences
            </button>
          </div>

          <div className={styles.tabsContent}>
            {activeTab === 'overview' && <ProfileOverview user={user} onRefresh={loadUserProfile} />}
            {activeTab === 'email' && <ChangeEmailForm user={user} onEmailChanged={handleEmailChanged} />}
            {activeTab === 'password' && <ChangePasswordForm />}
            {activeTab === 'marketing' && (
              <MarketingConsentToggle user={user} onConsentChanged={handleConsentChanged} />
            )}
          </div>
        </div>
      </div>
    </section>
  );
};

export default UserProfilePage;
