import { AcGameObject } from "./AcGameObject";
import { Wall } from "./Wall";
import { Snake } from "./Snake";

export class GameMap extends AcGameObject {
    constructor(ctx, parent, store){
        super();

        this.ctx = ctx;
        this.parent = parent;
        this.store = store;
        this.L = 0;

        this.rows = 13;
        this.cols = 14;

        this.inner_walls_count = 20;
        this.walls = [];

        this.Snakes = [
            new Snake({id:0, color: "#4876ec", r: this.rows-2, c:1}, this),
            new Snake({id:1, color: "#f94848", r:1, c: this.cols-2}, this)
        ]
    }

    // check_connectivity(g, sx, sy, tx, ty){
    //     if(sx == tx && sy == ty) return true;
    //     g[sx][sy] = true;

    //     let dx = [-1, 0, 1, 0], dy = [0, 1, 0, -1];
    //     for(let i=0; i<4; i++){
    //         let x = sx + dx[i], y = sy + dy[i];
    //         if(!g[x][y] && this.check_connectivity(g, x, y, tx, ty))
    //             return true;
    //     }
    //     return false;
    // }


    // crete_walls(){
    //     const g = [];
    //     for(let r=0; r<this.rows; r++){
    //         g[r] = [];
    //         for(let c=0; c<this.cols; c++){
    //             g[r][c] = false;
    //         }
    //     }

    //     //给四周加上障碍物
    //     for(let r=0; r<this.rows; r++){
    //         g[r][0] = g[r][this.cols-1] = true;
    //     }

    //     for(let c=0; c<this.cols; c++){
    //         g[0][c] = g[this.rows-1][c] = true;
    //     }

    //     //创建随机障碍物
    //     for(let i=0; i<this.inner_walls_count/2; i++){
    //         for(let j=0; j<1000; j++){
    //             let r = parseInt(Math.random()*this.rows);
    //             let c = parseInt(Math.random()*this.cols);
    //             if(g[r][c] || g[this.rows - 1 - r][this.cols - 1 -c]) continue;
    //             if(r == this.rows-2 && c == 1 || r == 1 && c == this.cols -2)
    //                 continue;

    //             g[r][c] = g[this.rows - 1 - r][this.cols - 1 -c] = true;
    //             break;
    //         }
    //     }

    //     const copy_g = JSON.parse(JSON.stringify(g));

    //     if(!this.check_connectivity(copy_g, this.rows-2, 1, 1, this.cols-2))
    //         return false;
        
    //     for(let r =0; r<this.rows; r++){
    //         for(let c = 0; c<this.cols; c++){
    //             if(g[r][c]){
    //                 this.walls.push(new Wall(r, c, this))
    //             }
    //         }
    //     }

    //     return true;
    // }

    crete_walls(){
        //直接将地图取出--从后端传过来
        // console.log(this.store)
        const g = this.store.state.pk.gamemap;
        console.log('dddd')
        console.log(g)
        //创建障碍物对象，并添加到this.walls数组
        for(let r = 0; r<this.rows; r++){
            for(let c = 0; c<this.cols; c++){
                
                if(g[r][c]){
                    this.walls.push(new Wall(r,c,this));
                }
            }
        }
    }

    add_listening_events(){
        console.log("gg");
        if(this.store.state.record.is_record){
            let k = 0;
            const a_steps = this.store.state.record.a_steps;
            const b_steps = this.store.state.record.b_steps;
            const loser = this.store.state.record.record_loser;
            const [snake0, snake1] = this.Snakes;
            const interval_id = setInterval(() => {
                if(k>= a_steps.length-1){
                    if(loser === 'all' || loser === 'A'){
                        snake0.status = 'die';
                    }
                    if(loser === 'all' || loser === 'B'){
                        snake1.status = 'die';
                    }
                    clearInterval(interval_id); //结束后取消掉循环
                }else{
                    snake0.set_direction(parseInt(a_steps[k]));
                    snake1.set_direction(parseInt(b_steps[k]));
                }
                k++;
                console.log("hh");
            }, 300); // 300ms执行一次
        }else{
            this.ctx.canvas.focus(); //聚焦
            this.ctx.canvas.addEventListener("keydown", e=>{
                console.log(e.key);
                let d = -1;
                if(e.key === 'w') d = 0;
                else if(e.key === 'd') d = 1;
                else if(e.key === 's') d = 2;
                else if(e.key === 'a') d = 3;
                if(d>=0){//有效输入
                    this.store.state.pk.socket.send(JSON.stringify({
                        event:"move",
                        direction:d
                    }))
                }
            });
        }
    }

    check_valid(cell){
        for(const wall of this.walls){
            if(wall.r === cell.r && wall.c === cell.c)
                return false; 
        }
 
        for(const snake of this.Snakes){
            let k = snake.cells.length;
            if(!snake.check_tail_increasing()){
                k--;
            }

            for(let i=0; i<k; i++){
                if(snake.cells[i].r === cell.r && snake.cells[i].c === cell.c)
                    return false;
            }
        }
        return true;
    }

    start(){
        // for(let i=0; i<1000; i++){
        //     if(this.crete_walls())
        //         break;
        // }
        console.log("start")
        this.crete_walls();//不用循环1000次了，直接接收后端生成的
        this.add_listening_events();
    }

    update_size(){
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L*this.cols;
        this.ctx.canvas.height = this.L*this.rows;
    }

    check_ready(){ // 判断两条蛇是否都准备好下一回合了
        for(const snake of this.Snakes){
            if(snake.status !== "idle") return false;
            if(snake.direction === -1) return false;
        }
        return true;
    }

    next_step(){
        for(const snake of this.Snakes){
            snake.next_step();
        }
    }

    update(){
        this.update_size();
        if(this.check_ready()){
            this.next_step();
        }
        this.render();
    }

    render(){
        const color_even = "#AAD751", color_odd = "#A2D149";
        for(let r=0; r<this.rows; r++){
            for(let c =0; c<this.cols; c++){
                if((r+c)%2==0){
                    this.ctx.fillStyle = color_even;
                }else{
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(c*this.L, r*this.L, this.L, this.L);
            }
        }
    }
}