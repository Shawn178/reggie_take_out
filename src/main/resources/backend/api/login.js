// 3. api/login.js - 登录API函数定义
// api/login.js 第3-9行
function loginApi(data) {
  return $axios({  // 使用request.js中定义的$axios
    'url': '/employee/login',
    'method': 'post',
    data
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}
