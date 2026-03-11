import styles from "./index.module.scss";
import { useNavigate } from "react-router";
import { useAuth } from "../../auth/hooks/useAuth";
import { LoginForm } from "../../auth/components/LoginForm";
import Spinner from "../../components/components/Spinner";

const LoginPage = () => {
  const { user, isLoading, error, login } = useAuth();
  const navigate = useNavigate();

  const handleLoginSubmit = async (credentials: any) => {
    try {
      await login(credentials);
      navigate("/");
    } catch (err) {
      console.error("Login failed:", err);
    }
  };

  const { logout } = useAuth();
  const logoutHandler = async () => {
    await logout();
    navigate("/");
  };

  if (isLoading) return <Spinner />;

  return (
    <section className={styles.section}>
      <div className={`${styles.container} main-container`}>
        {user !== null ? (
          <div className={styles.profile}>
            <div className={styles.profileHeader}>
              <div className={styles.profileInfo}>
                <span className={styles.name}>
                  Name: {user?.username || 'User'}
                </span>
                <span>
                  Email: <strong>{user?.email || 'N/A'}</strong>
                </span>
              </div>
            </div>
            <button
              type="button"
              className={styles.logoutBtn}
              onClick={() => logoutHandler()}
            >
              Logout
            </button>
          </div>
        ) : (
          <div className={styles.loginContainer}>
            <h2>Login</h2>
            <LoginForm 
              onSubmit={handleLoginSubmit}
              isLoading={isLoading}
              error={error}
              onLoginSuccess={() => navigate("/")}
            />
            <div className={styles.signupLink}>
              Don't have an account? <a href="/register">Sign up here</a>
            </div>
          </div>
        )}
      </div>
    </section>
  );
};

export default LoginPage;
