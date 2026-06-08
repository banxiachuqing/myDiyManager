import request from '@/utils/request'

export function aiCmdDecision(data) {
  return request({ url: '/ai/chat/cmd/decision', method: 'post', data })
}
