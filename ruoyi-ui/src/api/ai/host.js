import request from '@/utils/request'

export function listHost(query) {
  return request({ url: '/ai/host/list', method: 'get', params: query })
}
export function listAllHost() {
  return request({ url: '/ai/host/all', method: 'get' })
}
export function getHost(hostId) {
  return request({ url: '/ai/host/' + hostId, method: 'get' })
}
export function addHost(data) {
  return request({ url: '/ai/host', method: 'post', data })
}
export function updateHost(data) {
  return request({ url: '/ai/host', method: 'put', data })
}
export function delHost(hostIds) {
  return request({ url: '/ai/host/' + hostIds, method: 'delete' })
}
export function testHost(data) {
  return request({ url: '/ai/host/test', method: 'post', data })
}
export function testAndSaveHost(hostId) {
  return request({ url: '/ai/host/' + hostId + '/test', method: 'post' })
}
