<template>
  <div id="app">
    <img class="logo" alt="octocat logo" src="./assets/octocat.jpg">
    <RepoSearch title="Github Repo Search"/>
  </div>
</template>

<script>
import RepoSearch from './components/RepoSearch.vue'

export default {
  name: 'app',
  components: {
    RepoSearch
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
.logo {
  width: 5%;
  height: 5%;  
}
</style>
