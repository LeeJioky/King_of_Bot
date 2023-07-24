<template>
    <ContentField>
        <div class="row justify-content-md-center">
            <div class="col-3">
                <form @submit.prevent = "login">
                    <div class="mb-3">
                        <label for="username" class="form-label">用户名</label>
                        <input v-model="username" type="text" class="form-control" id="username" placeholder="请输入用户名">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">密码</label>
                        <input v-model="password" type="password" class="form-control" id="password" placeholder="请输入密码">
                    </div>
                    <div class="error-message">{{ error_message }}</div>
                    <button type="submit" class="btn btn-primary">登录</button>
                </form>
            </div>
        </div>
    </ContentField>
</template>
<script>
import { useStore } from 'vuex';
import ContentField from '../../../components/ContentField'
import { ref } from 'vue';
import router from '@/router';

export default {
    components:{
        ContentField
    },
    setup(){
        const store = useStore();//取出全局变量
        let username = ref("");//定义username初始为空
        let password = ref("");//定义password
        let error_message = ref("");//表示登录是否成功

        const login = () => { //定义 login 函数， 当页面提交时触发
            error_message.value = "";
            store.dispatch("login",{ //使用dispatch函数来调用store\user.js中action函数
                username: username.value,//ref变量取值使用value
                password: password.value,
                success(){
                    // console.log(resp);
                    store.dispatch("getinfo",{
                        success(){
                            router.push({name:'home'});
                            console.log(store.state.user);//这里的user就是在store/index中导入的user: ModuleUser
                        }
                    })
                },
                error(){
                    error_message.value = "用户名或密码错误";
                }
            })
        }
        return{
            username,
            password,
            error_message,
            login
        }
    }
}
</script>
<style scoped>
button{
    width: 100%;
}
div.error-message{
    color: red;
}
</style>