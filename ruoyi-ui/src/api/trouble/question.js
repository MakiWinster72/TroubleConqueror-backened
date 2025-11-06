import request from '@/utils/request'

// 查询错题列表
export function listQuestion(query) {
  return request({
    url: '/trouble/question/list',
    method: 'get',
    params: query
  })
}

// 查询错题详细
export function getQuestion(questionId) {
  return request({
    url: '/trouble/question/' + questionId,
    method: 'get'
  })
}

// 新增错题
export function addQuestion(data) {
  return request({
    url: '/trouble/question',
    method: 'post',
    data: data
  })
}

// 修改错题
export function updateQuestion(data) {
  return request({
    url: '/trouble/question',
    method: 'put',
    data: data
  })
}

// 删除错题
export function delQuestion(questionId) {
  return request({
    url: '/trouble/question/' + questionId,
    method: 'delete'
  })
}

// 导出错题
export function exportQuestion(query) {
  return request({
    url: '/trouble/question/export',
    method: 'post',
    data: query
  })
}

// 收藏错题
export function favoriteQuestion(questionId) {
  return request({
    url: '/trouble/question/favorite/' + questionId,
    method: 'post'
  })
}

// 取消收藏错题
export function unfavoriteQuestion(questionId) {
  return request({
    url: '/trouble/question/unfavorite/' + questionId,
    method: 'post'
  })
}

// 查询收藏的错题列表
export function listFavoriteQuestion(query) {
  return request({
    url: '/trouble/question/favorite/list',
    method: 'get',
    params: query
  })
}

// 更新错题重要性
export function updateImportance(questionId, importance) {
  return request({
    url: '/trouble/question/importance/' + questionId,
    method: 'put',
    params: { importance }
  })
}