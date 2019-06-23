<template>
  <div class="container">
    <div class="row">
      <div class="col">
        {{ title }}
      </div>
    </div>
    <div class="row">
      <div class="col">
        <input v-model="searchWord" @keyup="search" type="text" class="form-control" placeholder="3文字以上で検索ワードを入力してください。" aria-label="検索ワード">
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
              <th scope="col">License</th>
              <th scope="col">オーナー</th>
              <th scope="col">Star</th>
              <th scope="col">Open Issue</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(repo, index) in repos" :key="index">
              <th scope="row">{{ toRowIndex(index) }}</th>
              <td class="text-left"><a target="_blank" rel="noopener noreferrer" :href="repo.html_url">{{ repo.full_name }}</a></td>
              <td class="text-left">{{ repo.language }}</td>
              <td class="text-left">{{ repo.description | truncate(40)}}</td>
              <td class="text-left"><span v-if="repo.license != 'Unknown'">{{ repo.license }}</span></td>
              <td class="text-left"><p>{{ repo.owner }}</p><img :src="repo.avatar_url" class="avatar" /></td>
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

const BASE_URL = __API_URL__

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
      if (this.searchWord.length < 3) {
        this.repos = []
        return
      }
      axios.get(`${BASE_URL}?query=${this.searchWord}`)
        .then(res => {
          this.repos = res.data
        })
        .catch(e => {
          console.error(e)
        })
    },
    toRowIndex(index) {
      return parseInt(index) + 1;
    }
  },
  filters: {
    truncate(value, n) {
      if (!value) return ''
      if (value.length <= n + 1) return value
      return value.substring(0, n + 1) + '...'
    }
  }
}
</script>

<style scoped>
.avatar {
  width: 50px;
  height: 50px;
}
</style>