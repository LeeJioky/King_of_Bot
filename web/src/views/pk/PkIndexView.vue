<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'"/>
    <MatchGround v-if="$store.state.pk.status === 'matching'"/>
    <ResultBoard v-if="$store.state.pk.loser!='none'"/>
</template>
<script>
import PlayGround from '@/components/PlayGround.vue';
import MatchGround from '@/components/MatchGround.vue';
import ResultBoard from '@/components/ResultBoard.vue';
import { useStore } from 'vuex';
import { onMounted, onUnmounted } from 'vue';
export default {
    components: {
    PlayGround,
    MatchGround,
    ResultBoard
},
    setup(){
        const store = useStore();
        const socketUrl = `ws://localhost:3000/websocket/${store.state.user.token}`;
        let socket = null;
        onMounted(() => {
            store.commit("updateOpponent",{
                username:"我的对手",
                photo:"https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            })
            socket = new WebSocket(socketUrl);
            socket.onopen = () => {//如果连接成功，将socket存储到全局变量中
                console.log("connected");
                store.commit('updateSocket', socket);
            }
            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                console.log(data);
                if(data.event === "start-matching"){
                    console.log("start-matching")
                    store.commit("updateOpponent",{
                        username:data.opponent_username,
                        photo:data.opponent_photo,
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                        // console.log("playing")
                    }, 200);//延迟2秒
                    // console.log("==========")
                    // console.log(data.game);
                    store.commit("updateGame", data.game)
                }else if(data.event === "move"){
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    console.log(game);
                    const [snake0, snake1] = game.Snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);

                }else if(data.event === 'result'){
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    console.log(game)
                    const [snake0, snake1] = game.Snakes;

                    if(data.loser == "all" || data.loser == "A"){
                        snake0.status = "die";
                    }
                    if(data.loser == "all" || data.loser == "B"){
                        snake1.status = "die";
                    }
                    store.commit("updateLoser", data.loser);
                }
            }
            socket.onclose = () =>{
                console.log("disconnected");
            }
        });

        onUnmounted(() => {
            socket.close();
            store.commit("updateStatus", "matching");
        })
    }
}
</script>
<style scoped>
    
</style>