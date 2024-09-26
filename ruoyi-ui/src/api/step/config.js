import request from '@/utils/request'

// 查询小米步数配置列表
export function listConfig(query) {
  return request({
    url: '/step/config/list',
    method: 'get',
    params: query
  })
}

// 查询小米步数配置详细
export function getConfig(id) {
  return request({
    url: '/step/config/' + id,
    method: 'get'
  })
}

// 新增小米步数配置
export function addConfig(data) {
  return request({
    url: '/step/config',
    method: 'post',
    data: data
  })
}

// 修改小米步数配置
export function updateConfig(data) {
  return request({
    url: '/step/config',
    method: 'put',
    data: data
  })
}

// 删除小米步数配置
export function delConfig(id) {
  return request({
    url: '/step/config/' + id,
    method: 'delete'
  })
}
