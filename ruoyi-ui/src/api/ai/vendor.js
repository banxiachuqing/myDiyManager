import request from '@/utils/request'

export function listVendor(query) {
  return request({ url: '/ai/vendor/list', method: 'get', params: query })
}
export function getVendor(vendorId) {
  return request({ url: '/ai/vendor/' + vendorId, method: 'get' })
}
export function addVendor(data) {
  return request({ url: '/ai/vendor', method: 'post', data })
}
export function updateVendor(data) {
  return request({ url: '/ai/vendor', method: 'put', data })
}
export function delVendor(vendorIds) {
  return request({ url: '/ai/vendor/' + vendorIds, method: 'delete' })
}
export function setDefaultVendor(vendorId) {
  return request({ url: '/ai/vendor/' + vendorId + '/default', method: 'put' })
}
export function testVendor(data) {
  return request({ url: '/ai/vendor/test', method: 'post', data })
}
export function testAndSaveVendor(vendorId) {
  return request({ url: '/ai/vendor/' + vendorId + '/test', method: 'post' })
}
export function getDefaultVendor() {
  return request({ url: '/ai/vendor/default', method: 'get' })
}
