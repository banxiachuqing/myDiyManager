import request from '@/utils/request'

// 查询小米刷步数运行日志列表
export function listLog(query) {
  return request({
    url: '/step/log/list',
    method: 'get',
    params: query
  })
}

// 查询小米刷步数运行日志详细
export function getLog(id) {
  return request({
    url: '/step/log/' + id,
    method: 'get'
  })
}

// 新增小米刷步数运行日志
export function addLog(data) {
  return request({
    url: '/step/log',
    method: 'post',
    data: data
  })
}

// 修改小米刷步数运行日志
export function updateLog(data) {
  return request({
    url: '/step/log',
    method: 'put',
    data: data
  })
}

// 删除小米刷步数运行日志
export function delLog(id) {
  return request({
    url: '/step/log/' + id,
    method: 'delete'
  })
}
