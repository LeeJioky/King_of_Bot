<template>
    <div class="container">
            <div class="row">

            <div class="col-3">
                <div class="card">
                    <div class="card-body">
                        <img :src="$store.state.user.photo" style="width: 100%;">
                    </div>
                </div>
            </div>

            <div class="col-9">
                <div class="card">
                    <div class="card-header">
                        <span style="font-size: 130%;">我的Bot</span>
                        <button type="button" class="btn btn-primary float-end" data-bs-toggle="modal" data-bs-target="#add-bot-btn">创建</button>
                        
                        <!-- Modal -->
                        <div class="modal fade" id="add-bot-btn" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h1 class="modal-title fs-5" id="exampleModalLabel">创建Bot</h1>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <form>
                                            <div class="mb-3">
                                                <label for="add-bot-title" class="form-label">名称</label>
                                                <input v-model="botadd.title" type="text" class="form-control" id="add-bot-title" placeholder="请输入Bot的名称">
                                            </div>
                                            <div class="mb-3">
                                                <label for="add-bot-description" class="form-label">简介</label>
                                                <textarea v-model="botadd.description" type="text" class="form-control" id="add-bot-description" placeholder="请输入Bot简介"></textarea>
                                            </div>
                                            <div class="mb-3">
                                                <label for="add-bot-code" class="form-label">代码</label>
                                                <VAceEditor 
                                                    v-model:value="botadd.content" 
                                                    @init="editorInit" 
                                                    lang="c_cpp" 
                                                    theme="textmate" 
                                                    style="height: 300px;"
                                                    :options="{
                                                        enableBasicAutocompletion: true, //启用基本自动完成
                                                        enableSnippets: true, // 启用代码段
                                                        enableLiveAutocompletion: true, // 启用实时自动完成
                                                        fontSize: 18, //设置字号
                                                        tabSize: 4, // 标签大小
                                                        showPrintMargin: false, //去除编辑器里的竖线
                                                        highlightActiveLine: true,
                                                    }"
                                                />
                                            </div>
                                        </form>
                                    </div>
                                    <div class="modal-footer">
                                        <div class="error_message">{{ botadd.error_message }}</div>
                                        <button type="button" class="btn btn-primary" @click="add_bot">创建</button>
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="card-body">
                        <table class="table table-striped table-hover">
                            <thead>
                                <tr>
                                    <th scope="col">名称</th>
                                    <th scope="col">描述</th>
                                    <th scope="col">创建时间</th>
                                    <th scope="col">操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="bot in bots" :key="bot.id">
                                    <td>{{ bot.title }}</td>
                                    <td>{{ bot.description }}</td>
                                    <td>{{ bot.createtime }}</td>
                                    <td>
                                        <button type="button" class="btn btn-secondary" data-bs-toggle="modal" :data-bs-target="'#update-bot-btn-'+bot.id" style="margin-right: 10px;">修改</button>

                                        <!-- Modal -->
                                        <div class="modal fade" :id="'update-bot-btn-'+bot.id" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                            <div class="modal-dialog">
                                                <div class="modal-content">
                                                    <div class="modal-header">
                                                        <h1 class="modal-title fs-5" id="exampleModalLabel">修改Bot</h1>
                                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                    </div>
                                                    <div class="modal-body">
                                                        <form>
                                                            <div class="mb-3">
                                                                <label for="update-bot-title" class="form-label">名称</label>
                                                                <input v-model="bot.title" type="text" class="form-control" id="update-bot-title" placeholder="请输入Bot的名称">
                                                            </div>
                                                            <div class="mb-3">
                                                                <label for="update-bot-description" class="form-label">简介</label>
                                                                <textarea v-model="bot.description" type="text" class="form-control" id="update-bot-description" placeholder="请输入Bot简介"></textarea>
                                                            </div>
                                                            <div class="mb-3">
                                                                <label for="update-bot-code" class="form-label">代码</label>
                                                                <VAceEditor 
                                                                    v-model:value="bot.content" 
                                                                    @init="editorInit" 
                                                                    lang="c_cpp" 
                                                                    theme="textmate" 
                                                                    style="height: 300px;"
                                                                    :options="{
                                                                        enableBasicAutocompletion: true, //启用基本自动完成
                                                                        enableSnippets: true, // 启用代码段
                                                                        enableLiveAutocompletion: true, // 启用实时自动完成
                                                                        fontSize: 18, //设置字号
                                                                        tabSize: 4, // 标签大小
                                                                        showPrintMargin: false, //去除编辑器里的竖线
                                                                        highlightActiveLine: true,
                                                                    }"
                                                                />
                                                            </div>
                                                        </form>
                                                    </div>
                                                    <div class="modal-footer">
                                                        <div class="error_message">{{ bot.error_message }}</div>
                                                        <button type="button" class="btn btn-primary" @click="update_bot(bot)">保存修改</button>
                                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <button type="button" class="btn btn-danger" @click="remove_bot(bot)">删除</button>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>
    </div>
    
</template>
<script>
import { ref, reactive } from "vue";
import $ from "jquery"
import { useStore } from 'vuex';
import { Modal } from "bootstrap/dist/js/bootstrap";

import { VAceEditor } from 'vue3-ace-editor';
import ace from 'ace-builds';

import 'ace-builds/src-noconflict/mode-c_cpp';
import 'ace-builds/src-noconflict/mode-json';
import 'ace-builds/src-noconflict/theme-chrome';
import 'ace-builds/src-noconflict/ext-language_tools';

export default {
    components:{
        VAceEditor
    },
    setup(){
        ace.config.set(
            "basePath", 
            "http://cdn.jsdelivr.net/npm/ace-builds@" + require('ace-builds').version + "/src-noconflict/")

        const store = useStore();

        let bots = ref([]); //bots用于保存getlist返回的bot列表

        const botadd = reactive({ //对象
            title:"",
            description:"", 
            content:"",
            error_message:""
        });

        const refresh_bots = () => {
            $.ajax({
                url:"http://47.115.210.235/api/user/bot/getlist/",
                type:"get",
                headers:{
                    Authorization:"Bearer "+store.state.user.token
                },
                success(resp){
                    // console.log(resp);
                    bots.value = resp;
                }
            })
        }
        refresh_bots();//调用函数

        const add_bot = () => {
            $.ajax({
                url:"http://47.115.210.235/api/user/bot/add/",
                type:"post",
                data:{
                    title:botadd.title,
                    description: botadd.description,
                    content:botadd.content,
                },
                headers:{
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp){
                    if(resp.error_message == "success"){
                        botadd.title = "",
                        botadd.description = "",
                        botadd.content = "",
                        Modal.getInstance("#add-bot-btn").hide();
                        refresh_bots();
                    }else{
                        botadd.error_message = resp.error_message;
                    }
                }
            })
        }

        const remove_bot = (bot) => {
            $.ajax({
                url:"http://47.115.210.235/api/user/bot/remove/",
                type:"post",
                headers:{
                    Authorization: "Bearer " + store.state.user.token,
                },
                data:{
                    bot_id: bot.id
                },
                success(resp){
                    if(resp.error_message == "success"){
                        console.log("delete successfully~");
                        refresh_bots();
                    }else{
                        console.log(resp);
                        alert(resp.error_message)
                    }
                }
            })
        }

        const update_bot = (bot) => {
            bot.error_message = "";
            $.ajax({
                url:"http://47.115.210.235/api/user/bot/update/",
                type:"post",
                data:{
                    bot_id: bot.id,
                    title: bot.title,
                    description: bot.description,
                    content: bot.content,
                },
                headers:{
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp){
                    if(resp.error_message == "success"){
                        Modal.getInstance("#update-bot-btn-"+bot.id).hide();
                        alert("修改成功~");
                        refresh_bots();
                    }else{
                        console.log(resp);
                        bot.error_message = resp.error_message;
                    }
                }
            })
        }



        return{
            bots,
            botadd,
            add_bot,
            remove_bot,
            update_bot
        }
    }
}
</script>
<style scoped>
div.card{
    margin-top: 20px;
}

div.error_message{
    color: red;
}
</style>