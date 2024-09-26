import request from '@/utils/request'

// 查询小米步数配置列表
export function listStepConfig(query) {
  return request({
    url: '/step/stepConfig/list',
    method: 'get',
    params: query
  })
}

// 查询小米步数配置详细
export function getStepConfig(id) {
  return request({
    url: '/step/stepConfig/' + id,
    method: 'get'
  })
}

// 新增小米步数配置
export function addStepConfig(data) {
  return request({
    url: '/step/stepConfig',
    method: 'post',
    data: data
  })
}

// 修改小米步数配置
export function updateStepConfig(data) {
  return request({
    url: '/step/stepConfig',
    method: 'put',
    data: data
  })
}

// 删除小米步数配置
export function delStepConfig(id) {
  return request({
    url: '/step/stepConfig/' + id,
    method: 'delete'
  })
}
