// 2. js/request.js - HTTP请求拦截器配置
// axios实例创建：3-12行
(function (win) {
  axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
  // 创建axios实例
  const service = axios.create({
    // axios中请求配置有baseURL选项，表示请求URL公共部分
    baseURL: '/',
    // 超时
    timeout: 1000000
  })

  // request拦截器：请求拦截器逻辑 --> 13-45行
  service.interceptors.request.use(config => {
    // 是否需要设置 token
    // const isToken = (config.headers || {}).isToken === false
    // if (getToken() && !isToken) {
    //   config.headers['Authorization'] = 'Bearer ' + getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
    // }
    // get请求映射params参数
    if (config.method === 'get' && config.params) {
      let url = config.url + '?';
      // 遍历params对象的每个属性
      for (const propName of Object.keys(config.params)) {
        const value = config.params[propName];
        var part = encodeURIComponent(propName) + "=";
        if (value !== null && typeof(value) !== "undefined") {
          if (typeof value === 'object') {
            // 处理嵌套对象参数，如 params[key]=value
            for (const key of Object.keys(value)) {
              let params = propName + '[' + key + ']';
              var subPart = encodeURIComponent(params) + "=";
              url += subPart + encodeURIComponent(value[key]) + "&";
            }
          } else {
            // 处理普通参数
            url += part + encodeURIComponent(value) + "&";
          }
        }
      }
      url = url.slice(0, -1);  // 移除最后的&
      config.params = {};      // 清空params
      config.url = url;        // 使用拼接后的URL
    }
    return config
  }, error => {
      console.log(error)
      Promise.reject(error)
  })

  // 响应拦截器：响应拦截器逻辑 --> 51-60行
  service.interceptors.response.use(res => {
      if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 检测到未登录状态，返回登录页面
        console.log('---/backend/page/login/login.html---')
        localStorage.removeItem('userInfo')  // 清除本地用户信息
        window.top.location.href = '/backend/page/login/login.html'  // 跳转登录页
      } else {
        return res.data  // 返回响应数据     
      }
    },
    error => {
      console.log('err' + error)
      let { message } = error;
      if (message == "Network Error") {
        message = "后端接口连接异常";
      }
      else if (message.includes("timeout")) {
        message = "系统接口请求超时";
      }
      else if (message.includes("Request failed with status code")) {
        message = "系统接口" + message.substr(message.length - 3) + "异常";
      }
      window.ELEMENT.Message({
        message: message,
        type: 'error',
        duration: 5 * 1000
      })
      return Promise.reject(error)
    }
  )

  // 全局axios实例暴露：
  win.$axios = service
})(window);
