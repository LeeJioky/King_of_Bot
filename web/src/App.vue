<template>
  <NavBar></NavBar>
  <router-view/>
</template>

<script>
import NavBar from '@/components/NavBar.vue'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap/dist/js/bootstrap"

// import $ from 'jquery'
export default{
  components:{
    NavBar
  },
  setup(){
    // $.ajax({
    //   url: "http://localhost:3000/user/account/token/",
    //   type: "post",
    //   data: {
    //     username: "lihui",
    //     password: "plihui"
    //   },
    //   success(resp){
    //     console.log(resp);
    //   },
    //   error(resp){
    //     console.log(resp);
    //   }
    // }),
    // $.ajax({
    //   url: "http://localhost:3000/user/account/info/",
    //   type: "get",
    //   headers: {
    //     Authorization: "Bearer " + "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwZDIxZTRkZTBkZmY0ZTBhOTMzMGViYjAzMmRlMDIwYyIsInN1YiI6IjEiLCJpc3MiOiJzZyIsImlhdCI6MTY5MDE3MzM4NiwiZXhwIjoxNjkxMzgyOTg2fQ.I9GGAMErMjpVd1h-cwT13y8FIHtYEkax1bffLHrtpGw"
    //   },
    //   success(resp){
    //     console.log(resp)
    //   },
    //   error(resp){
    //     console.log(resp)
    //   }
    // }),
    // $.ajax({
    //   url: "http://localhost:3000/user/account/register/",
    //   type: "post",
    //   data: {
    //     username: "zhanghua",
    //     password: "pzh",
    //     confirmedPassword: "pzh"
    //   },
    //   success(resp){
    //     console.log(resp);
    //   },
    //   error(resp){
    //     console.log(resp);
    //   }
    // })
  }
}
</script>

<style>
body {
  background-image: url("@/assets/images/background.png");
  background-size: cover;
}
</style>
