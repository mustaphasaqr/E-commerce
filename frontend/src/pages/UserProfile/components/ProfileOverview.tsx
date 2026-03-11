import React, { useState } from 'react';
// import { userService } from '@shared/services';
import type { User } from '@auth/types';
import styles from './ProfileOverview/index.module.scss';

interface ProfileOverviewProps {
  user: User;
  onRefresh: () => void;
}

/**
 * Profile Overview Component
 * Integrates with GET /api/v1/users/me
 */
const ProfileOverview: React.FC<ProfileOverviewProps> = ({ user, onRefresh }) => {
  const [loading, setLoading] = useState(false);

  const handleRefresh = async () => {
    setLoading(true);
    try {
      await onRefresh();
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  return (
    <div className={styles.profileOverview}>
      <h3>Your Profile</h3>
      
      <div className={styles.profileCard}>
        <div className={styles.profileHeader}>
          <div className={styles.avatar}>{user.username?.charAt(0).toUpperCase() || 'U'}</div>
          <div className={styles.profileInfo}>
            <h2>{user.username}</h2>
            <p className={styles.email}>{user.email}</p>
          </div>
        </div>

        <div className={styles.profileDetails}>
          <div className={styles.detailSection}>
            <h4>Account Information</h4>
            <div className={styles.detailRow}>
              <span className={styles.label}>User ID:</span>
              <span className={styles.value}>{user.id}</span>
            </div>
            <div className={styles.detailRow}>
              <span className={styles.label}>Email:</span>
              <span className={styles.value}>{user.email}</span>
            </div>
            <div className={styles.detailRow}>
              <span className={styles.label}>Username:</span>
              <span className={styles.value}>{user.username}</span>
            </div>
          </div>

          <div className={styles.detailSection}>
            <h4>Account Status</h4>
            <div className={styles.detailRow}>
              <span className={styles.label}>Email Verified:</span>
              <span className={`${styles.value} ${styles.status} ${styles[user.emailVerified ? 'verified' : 'unverified']}`}>
                {user.emailVerified ? '✓ Verified' : '✗ Unverified'}
              </span>
            </div>
            <div className={styles.detailRow}>
              <span className={styles.label}>Account Status:</span>
              <span className={`${styles.value} ${styles.status} ${styles[user.status?.toLowerCase() || 'default']}`}>
                {user.status}
              </span>
            </div>
            <div className={styles.detailRow}>
              <span className={styles.label}>Account Created:</span>
              <span className={styles.value}>{formatDate(user.createdAt)}</span>
            </div>
            <div className={styles.detailRow}>
              <span className={styles.label}>Last Updated:</span>
              <span className={styles.value}>{formatDate(user.updatedAt)}</span>
            </div>
          </div>

          <div className={styles.detailSection}>
            <h4>Preferences</h4>
            <div className={styles.detailRow}>
              <span className={styles.label}>Marketing Consent:</span>
              <span className={styles.value}>
                {user.marketingConsent ? '✓ Opted In' : '✗ Opted Out'}
              </span>
            </div>
          </div>
        </div>
      </div>

      <button 
        className={`${styles.btnSecondary}`}
        onClick={handleRefresh}
        disabled={loading}
      >
        {loading ? 'Refreshing...' : 'Refresh Profile'}
      </button>

      <div className={styles.profileFooter}>
        <p className={styles.note}>For additional account management options, use the tabs above.</p>
      </div>
    </div>
  );
};

export default ProfileOverview;
