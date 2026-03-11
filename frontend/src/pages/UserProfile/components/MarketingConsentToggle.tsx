import React, { useState } from 'react';
import { userService } from '@shared/services';
import type { User } from '@auth/types';
import styles from './MarketingConsentToggle/index.module.scss';

interface MarketingConsentToggleProps {
  user: User;
  onConsentChanged: (granted: boolean) => void;
}

/**
 * Marketing Consent Toggle Component
 * Integrates with:
 * - POST /api/v1/users/me/marketing/grant
 * - DELETE /api/v1/users/me/marketing
 */
const MarketingConsentToggle: React.FC<MarketingConsentToggleProps> = ({ user, onConsentChanged }) => {
  const [toggling, setToggling] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const handleToggle = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const granted = e.target.checked;
    
    try {
      setToggling(true);
      setError(null);
      
      if (granted) {
        await userService.grantMarketingConsent();
        setMessage('You will now receive marketing emails');
      } else {
        await userService.revokeMarketingConsent();
        setMessage('You have unsubscribed from marketing emails');
      }
      
      onConsentChanged(granted);
      setTimeout(() => setMessage(null), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update preferences');
      console.error('Consent update error:', err);
    } finally {
      setToggling(false);
    }
  };

  return (
    <div className={styles.marketingConsentToggle}>
      <h3>Marketing Preferences</h3>
      
      {error && <div className={styles.errorAlert}>{error}</div>}
      {message && <div className={styles.successAlert}>{message}</div>}
      
      <div className={styles.consentCard}>
        <div className={styles.consentInfo}>
          <h4>Promotional Emails</h4>
          <p>Receive exclusive offers, discounts, and product updates</p>
        </div>
        
        <label className={styles.toggleSwitch}>
          <input
            type="checkbox"
            checked={user.marketingConsent || false}
            onChange={handleToggle}
            disabled={toggling}
          />
          <span className={styles.slider}></span>
        </label>
      </div>

      <div className={styles.consentDetails}>
        <p className={styles.currentStatus}>
          Current status: <strong>{user.marketingConsent ? 'Opted In' : 'Opted Out'}</strong>
        </p>
        
        {user.marketingConsent ? (
          <p className={styles.infoText}>
            ✓ You'll receive our monthly newsletter with exclusive offers and new product announcements
          </p>
        ) : (
          <p className={styles.infoText}>
            You won't receive marketing emails, but we'll still send important account notifications
          </p>
        )}
      </div>

      <div className={styles.consentNotice}>
        <p>
          <strong>Note:</strong> This preference only affects marketing emails. 
          We'll always send you important account and order notifications.
        </p>
      </div>
    </div>
  );
};

export default MarketingConsentToggle;
