import request from '@/utils/request'
import { getToken } from '@/utils/auth'

// SSE 走 fetch，不用 axios；token 从 RuoYi 的 Cookie 取（utils/auth）
export function aiChatSend(body) {
  return fetch('/dev-api/ai/chat/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + (getToken() || '')
    },
    body: JSON.stringify(body)
  })
}
export function aiSessionNew() { return request({ url: '/ai/chat/session/new', method: 'post' }) }
export function aiSessionCurrent() { return request({ url: '/ai/chat/session/current', method: 'get' }) }
export function aiListMessages(sessionId) { return request({ url: '/ai/chat/session/' + sessionId + '/messages', method: 'get' }) }
export function listSessions(query) { return request({ url: '/ai/chat/sessions', method: 'get', params: query }) }
export function deleteSession(sessionId) { return request({ url: '/ai/chat/session/' + sessionId, method: 'delete' }) }
export function listCommands(query) { return request({ url: '/ai/chat/cmd/list', method: 'get', params: query }) }
