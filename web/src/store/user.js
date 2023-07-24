import $ from 'jquery'

export default ({
  state: {
    id:"",
    username:"",
    photo:"",
    token:"",
    is_login:false
  },
  getters: {
  },
  mutations: {
    updateUser(state, user){
        state.id = user.id;
        state.username = user.username;
        state.photo = user.photo;
        state.is_login = user.is_login;
    },
    updateToken(state, token){
        state.token = token;
    },
    logout(state){
        state.id = "";
        state.username = "";
        state.photo = "";
        state.token = "";
        state.is_login = false;
    }
  },
  actions: {
    login(context, data){
        $.ajax({
            url: "http://localhost:3000/user/account/token/",
            type: "post",
            data: {
              username: data.username,
              password: data.password
            },
            success(resp){
              if(resp.error_message === "success"){
                context.commit("updateToken", resp.token);//调用mutations里面的函数
                data.success(resp);
              }else{
                data.error(resp);
              }
            },
            error(resp){
              data.error(resp);
            }
          })
    },
    getinfo(context, data){
        $.ajax({
            url: "http://localhost:3000/user/account/info/",
            type: "get",
            headers: {
              Authorization: "Bearer " + context.state.token,
            },
            success(resp){
            //   console.log(resp)
                if(resp.error_message == "success"){
                    context.commit("updateUser",{//调用mutations里面的函数
                        ...resp, // 解析resp的内容
                        is_login:true
                    });
                    data.success(resp);
                }else{
                    data.error(resp);
                }
            }
          })
    },
    logout(context){
        context.commit("logout");
    }
  },
  modules: {
  }
})
