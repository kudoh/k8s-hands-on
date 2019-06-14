<template>
  <div class="container">
    <div class="row">
      <div class="col">
        {{ title }}
      </div>
    </div>
    <div class="row">
      <div class="col">
        <div class="input-group mb-3">
          <input v-model.lazy="searchWord" type="text" class="form-control" placeholder="検索ワードを入力してください。" aria-label="検索ワード">
          <div class="input-group-append">
            <button @click="search" class="btn btn-outline-secondary" type="button" id="button-search">Search</button>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col">
        <table class="table table-striped table-hover">
          <thead class="thead-dark">
            <tr>
              <th scope="col">#</th>
              <th scope="col">リポジトリ</th>
              <th scope="col">言語</th>
              <th scope="col">説明</th>
              <th scope="col">Lisence</th>
              <th scope="col">オーナー</th>
              <th scope="col">Star</th>
              <th scope="col">Open Issue</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(repo, index) of repos" :key="index">
              <th scope="row">{{ index + 1 }}</th>
              <td class="text-left"><a target="_blank" rel="noopener noreferrer" :href="repo.html_url">{{ repo.full_name }}</a></td>
              <td class="text-left">{{ repo.language }}</td>
              <td class="text-left">{{ repo.description | truncate(20)}}</td>
              <td class="text-left"><span v-if="repo.license != 'Unknown'">{{ repo.license }}</span></td>
              <td class="text-left">{{ repo.owner }}</td>
              <td>{{ repo.watchers_count}}</td>
              <td>{{ repo.open_issues_count }}</td>
            </tr>  
          </tbody>
        </table>
      </div>
    </div>  
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RepoSearch',
  props: {
    title: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      searchWord: '',  
      repos: []
    }  
  },
  methods: {
    search() {
      axios.get(`http://localhost:3000/api/v1/repos?query=${this.searchWord}`)
        .then(res => {
          this.repos = res.data
        })
        .catch(e => {
          console.error(e)
        })

    }
  },
  filters: {
    truncate(value, n) {
      if (value.length <= n + 1) return value
      return value.substring(0, n + 1) + '...'
    }
  }
}
</script>

<style scoped>
</style>