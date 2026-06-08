// composables/useAiSse.js
// SSE: fetch + ReadableStream (EventSource doesn't support custom Headers)
// 统一错误处理：网络异常 / HTTP 错误 / 厂商错误码 全部走 onError

import { aiChatSend } from '@/api/ai/chat'

const ERROR_FALLBACK_MSG = '流式连接中断，请重试或切换厂商'

/**
 * @param {Object} ctx
 * @param {Object} ctx.body - ChatSendBo
 * @param {(d: any) => void} [ctx.onFirst]
 * @param {(delta: string) => void} [ctx.onChunk]
 * @param {(data: {messageId: string, commands: any[]}) => void} [ctx.onDone]
 * @param {(err: Error) => void} [ctx.onError]
 */
export function useAiSse() {
  let abortCtrl = null
  return {
    open(ctx) {
      abortCtrl = new AbortController()
      const { body, onFirst, onChunk, onDone, onError } = ctx
      const handleError = (e) => {
        const msg = e && e.message ? e.message : ERROR_FALLBACK_MSG
        onError?.(new Error(msg))
      }
      aiChatSend(body)
        .then(async resp => {
          if (!resp.ok) {
            let msg = 'HTTP ' + resp.status
            if (resp.status === 401 || resp.status === 403) msg += ' 鉴权失败'
            else if (resp.status === 404) msg += ' 厂商路径不存在，请检查 baseUrl'
            else if (resp.status === 429) msg += ' 调用频率超限'
            return handleError(new Error(msg))
          }
          const reader = resp.body.getReader()
          const decoder = new TextDecoder()
          let buffer = ''
          try {
            while (true) {
              const { done, value } = await reader.read()
              if (done) break
              buffer += decoder.decode(value, { stream: true })
              const frames = buffer.split('\n\n')
              buffer = frames.pop() || ''
              for (const frame of frames) {
                const e = parseSseFrame(frame)
                if (!e) continue
                if (e.event === 'first') onFirst?.(JSON.parse(e.data))
                else if (e.event === 'chunk') {
                  const d = JSON.parse(e.data)
                  onChunk?.(d.delta)
                } else if (e.event === 'done') onDone?.(JSON.parse(e.data))
                else if (e.event === 'error') onError?.(new Error(e.data))
              }
            }
          } catch (err) {
            handleError(err)
          }
        })
        .catch(handleError)
    },
    close() { abortCtrl?.abort(); abortCtrl = null }
  }
}

/**
 * @param {string} frame
 * @returns {{event: string, data: string} | null}
 */
function parseSseFrame(frame) {
  let event = 'message', data = ''
  for (const line of frame.split('\n')) {
    if (line.startsWith('event:')) event = line.substring(6).trim()
    else if (line.startsWith('data:')) data += (data ? '\n' : '') + line.substring(5).trim()
  }
  return data ? { event, data } : null
}
