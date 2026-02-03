package com.mustapha.ecommerce.user.auth.infrastructure.persistence;

import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Login Session Repository Implementation
 * Responsibility: Persist LoginSession aggregate to Redis
 * Pattern: Adapter (Domain Repository → Redis)
 * 
 * Scope: AUTH subdomain only
 * Technology: Spring Data Redis
 * Storage: Redis (TTL: 24 hours)
 * 
 * Design:
 * - Key: login_session:{sessionId}
 * - Value: Serialized LoginSession (JSON via RedisTemplate)
 * - TTL: 24 hours (auto-expire)
 * - Secondary index: user_sessions:{userId} → Set<sessionId> for bulk operations
 */
@Repository
public class LoginSessionRepositoryImpl implements LoginSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String SESSION_KEY_PREFIX = "login_session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final long TTL_HOURS = 24;

    public LoginSessionRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public LoginSession save(LoginSession session) {
        String sessionKey = SESSION_KEY_PREFIX + session.getSessionId();
        String userSessionsKey = USER_SESSIONS_PREFIX + session.getUserId();
        
        // Save session with TTL
        redisTemplate.opsForValue().set(sessionKey, session, TTL_HOURS, TimeUnit.HOURS);
        
        // Add to secondary index (user's sessions)
        redisTemplate.opsForSet().add(userSessionsKey, session.getSessionId());
        
        return session;
    }

    @Override
    public Optional<LoginSession> findBySessionId(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        
        // Handle both direct LoginSession objects and deserialized maps
        if (value instanceof LoginSession) {
            return Optional.of((LoginSession) value);
        }
        
        // If Redis returns a LinkedHashMap due to serialization issues, convert it
        if (value instanceof java.util.Map) {
            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) value;
                
                // Reconstruct LoginSession from map
                return Optional.of(LoginSession.reconstitute(
                    (String) map.get("sessionId"),
                    (String) map.get("userId"),
                    java.time.LocalDateTime.parse((String) map.get("createdAt")),
                    java.time.LocalDateTime.parse((String) map.get("expiresAt")),
                    (Boolean) map.get("active"),
                    java.time.LocalDateTime.parse((String) map.get("lastAccessedAt")),
                    (String) map.get("ipAddress"),
                    (String) map.get("userAgent")
                ));
            } catch (Exception e) {
                // If reconstruction fails, return empty
                return Optional.empty();
            }
        }
        
        return Optional.empty();
    }

    @Override
    public List<LoginSession> findActiveSessionsByUserId(UserId userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId.getValue();
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionIds == null || sessionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Fetch all sessions
        List<String> sessionKeys = sessionIds.stream()
            .map(Object::toString)
            .map(id -> SESSION_KEY_PREFIX + id)
            .collect(Collectors.toList());
        
        List<Object> sessions = redisTemplate.opsForValue().multiGet(sessionKeys);
        
        // Filter out nulls (expired sessions) and convert
        return sessions.stream()
            .filter(java.util.Objects::nonNull)
            .map(v -> (LoginSession) v)
            .filter(LoginSession::isActive)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String sessionId) {
        // Fetch session to get userId for secondary index cleanup
        Optional<LoginSession> session = findBySessionId(sessionId);
        
        // Delete session
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(sessionKey);
        
        // Remove from secondary index
        session.ifPresent(s -> {
            String userSessionsKey = USER_SESSIONS_PREFIX + s.getUserId();
            redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        });
    }

    @Override
    public void deleteAllByUserId(UserId userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId.getValue();
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionIds != null && !sessionIds.isEmpty()) {
            // Delete all session keys
            List<String> sessionKeys = sessionIds.stream()
                .map(Object::toString)
                .map(id -> SESSION_KEY_PREFIX + id)
                .collect(Collectors.toList());
            
            redisTemplate.delete(sessionKeys);
            
            // Clean up secondary index
            redisTemplate.delete(userSessionsKey);
        }
    }

    @Override
    public void deleteAllByUserIdExcept(UserId userId, String currentSessionId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId.getValue();
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionIds != null && !sessionIds.isEmpty()) {
            // Filter out current session
            List<String> sessionsToDelete = sessionIds.stream()
                .map(Object::toString)
                .filter(id -> !id.equals(currentSessionId))
                .map(id -> SESSION_KEY_PREFIX + id)
                .collect(Collectors.toList());
            
            if (!sessionsToDelete.isEmpty()) {
                redisTemplate.delete(sessionsToDelete);
                
                // Remove from secondary index
                sessionIds.stream()
                    .map(Object::toString)
                    .filter(id -> !id.equals(currentSessionId))
                    .forEach(id -> redisTemplate.opsForSet().remove(userSessionsKey, id));
            }
        }
    }
}
