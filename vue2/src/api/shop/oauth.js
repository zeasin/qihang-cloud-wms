import request from '@/utils/request'

// 拼多多授权回调
export function pddOauthLogin(data) {
  return request({
    url: '/api/sys-api/oauth/pdd_callback',
    method: 'post',
    data: data
  })
}
