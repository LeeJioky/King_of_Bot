export default ({
  state: {
    status:"matching",//matching表示匹配界面 playing 表示对战界面
    socket:null,//存储前后端建立的connection
    opponent_username:"",//对手名
    opponent_photo:"",//对手头像
    gamemap:null,
    a_id:0,
    a_sx:0,
    a_sy:0,
    b_id:0,
    b_sx:0,
    b_sy:0,
    gameObject:null,
    loser:"none"
  },
  mutations: {
    //在成功创建连接之后，需要将连接信息，存储到全局变量中，需要实现几个辅助函数
    updateSocket(state, socket){
      state.socket = socket;
    },
    updateOpponent(state, opponent){
      state.opponent_username = opponent.username;
      state.opponent_photo = opponent.photo;
    },
    updateStatus(state, status){
      state.status = status;
    },
    updateGame(state, game){
      state.a_id = game.a_id;
      state.a_sx = game.a_sx;
      state.a_sy = game.a_sy;
      state.b_id = game.b_id;
      state.b_sx = game.b_sx;
      state.b_sy = game.b_sy;
      state.gamemap = game.gamemap;
    },
    updateGameObject(state, gameObject){
      state.gameObject = gameObject;
      console.log(gameObject)
    },
    updateLoser(state, loser){
      state.loser = loser;
    }
  },
  actions: {
  },
  modules: {
  }
})
