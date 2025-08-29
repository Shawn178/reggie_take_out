/*
7. api/member.js - 员工管理API函数
  api/member.js 第7-13行
  api/member.js 第15-22行
  api/member.js 第24-31行
*/
function getMemberList (params) {
  return $axios({  // 使用request.js中的$axios实例
    url: '/employee/page',  // 后端接口路径
    method: 'get',
    params  // 查询参数
  })
}

// 修改---启用禁用接口
function enableOrDisableEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}

// 新增---添加员工
function addEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'post',
    data: { ...params }
  })
}

// 修改---添加员工
function editEmployee (params) {
  return $axios({
    url: '/employee',
    method: 'put',
    data: { ...params }
  })
}

// 修改页面反查详情接口
function queryEmployeeById (id) {
  return $axios({
    url: `/employee/${id}`,
    method: 'get'
  })
}