/**
 * Redux Logger Middleware
 * Log all Redux actions and state changes in development
 */

import { Middleware } from 'redux';

/**
 * Logger middleware for Redux
 */
export const loggerMiddleware: Middleware = (store) => (next) => (action) => {
  const prevState = store.getState();

  if (import.meta.env.DEV) {
    console.group(`Action: ${String(action.type)}`);
    console.log('Payload:', action.payload);
    console.log('Previous State:', prevState);
  }

  const result = next(action);

  if (import.meta.env.DEV) {
    console.log('Next State:', store.getState());
    console.groupEnd();
  }

  return result;
};
